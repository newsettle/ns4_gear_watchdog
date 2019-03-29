package com.creditease.ns4.gear.watchdog.monitor.process;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.ProcessUtil;
import com.creditease.ns4.gear.watchdog.common.PropertiesUtil;
import com.creditease.ns4.gear.watchdog.common.factory.ThreadPoolManager;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.common.template.TemplateName;
import com.creditease.ns4.gear.watchdog.monitor.collect.jvm.ProcessMonitor;
import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;
import com.creditease.ns4.gear.watchdog.monitor.notify.NotifyRoute;
import com.creditease.ns4.gear.watchdog.monitor.notify.constant.MESSAGE;
import com.creditease.ns4.gear.watchdog.monitor.process.constant.ExitCode;
import com.creditease.ns4.gear.watchdog.monitor.process.constant.PROCES;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author outman
 * @description watchdog子进程类，记录子进程状态信息
 * 1.创建socket服务<br>
 * 2.创建子进进程，并记录时间和状态<br>
 * 3.子进程启动成功后建立socket连接<br>
 * 4.监听子进程socket<br>
 * 5.处理子进程销毁操作<br>
 * @date 2019/1/15
 */
public class WatchdogChildProcess {

    private static final NsLog logger = NsLogger.getWatchdogLogger();
    /**
     * 读取inputStream流线程池
     */
    private ExecutorService inputStreamPool = null;

    /**
     * 进程信息
     */
    private ProcessInfo processInfo;

    public WatchdogChildProcess(ProcessInfo processInfo) {
        this.processInfo = processInfo;
        try {
            this.inputStreamPool = ThreadPoolManager.getInstance().newExecutorService("input-stream-pool-%d", 2, 2, 0L, TimeUnit.MILLISECONDS, 2);
        } catch (Exception e) {
            logger.error("创建读取子进程重定向输入流线程池异常：{}", e.getMessage());
        }
    }

    /**
     * 子进程信息
     */
    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    /**
     * 子进程输出流
     */
    private OutputStream stdOs;

    /**
     * 进程元子操作
     */
    private AtomicReference<Process> processRef = new AtomicReference<Process>();

    /**
     * 行号
     */
    private long lineNumber;

    /**
     * 创建子进程
     */
    public void create() {
        //一个socket服务用于ping
        ServerSocket ss = null;
        Socket s = null;
        try {
            //启动中
            this.processInfo.setStatus(PROCES.STATUS.STARTING);
            //启动时间
            this.processInfo.setStartTime(System.currentTimeMillis());
            ss = new ServerSocket(0, 5, InetAddress.getByName("127.0.0.1"));
            //ping使用的socket端口
            this.processInfo.setPingPort(ss.getLocalPort());
            //创建进程
            Process process = this.createProcessDemo();
            logger.info("检测子进程是否启动成功...");
            this.processRef.compareAndSet(null, process);
            //获取子进程id,根据子进程（ping端口加app name）
            StringBuilder processIdent = new StringBuilder("-ping").append(" ").append(this.processInfo.getPingPort());
            processIdent.append(" app-name:").append(this.processInfo.getNs4AppConf().getAppName());
            int childProcessID = ProcessUtil.getProcess(processIdent.toString());
            //子进程ID
            this.processInfo.setPid(childProcessID);
            //父进程ID
            this.processInfo.setPpid(ProcessUtil.getProcessID());
            //注意子进程的【输入】和【输出】流如果有写入就要有取出操作，否则会发现缓存写满锁死子进程，主进程会进入等待状态
            //获得子进程输出流
            this.stdOs = process.getOutputStream();
            //获得子进程输入流信息
            final InputStream in = process.getInputStream();
            this.inputStreamPool.execute(new Runnable() {
                @Override
                public void run() {
                    readInputStream(in, 0);
                }
            });
            //获得子进程错误流信息
            final InputStream inErr = process.getErrorStream();
            this.inputStreamPool.execute(new Runnable() {
                @Override
                public void run() {
                    readInputStream(inErr, 1);
                }
            });
            //启动状态 -> 启动成功
            this.processInfo.setStatus(PROCES.STATUS.SUCCESS);
            //进程状态 -> 启动成功
            this.processInfo.setProcessStatus(PROCES.STATUS.SUCCESS.getCode());
            s = this.connectToChild(ss);
            logger.info("子进程启动成功,PING SOCKET连接成功,进程信息[PID:{}, PPID:{}, SERVER-MAIN:{}, PING:{}, APP-AGENT_NAME:{}]",
                    this.processInfo.getPid(), this.processInfo.getPpid(), this.processInfo.getNs4AppConf().getServerMain(),
                    this.processInfo.getPingPort(), this.processInfo.getNs4AppConf().getAppName());
            //通知JMX - 已经启动成功的进程
            Jmx.getInstance().startChildJmx(this.processInfo.getPid(), this.processInfo.getNs4AppConf().getAppName());
            //执行各通知
            NotifyRoute.send(TemplateName.TEMP_NAME_CHAT_PROCESS_STATUS, MESSAGE.TYPE.START, this.processInfo, null);
            //采集进程信息并发送通知
            ProcessMonitor.newProcessMonitor().collection();
            int processStatus = process.waitFor();
            logger.info("进程状态是 : {}", processStatus);
            this.logStatus(processStatus);
            //进程退出状态 -> process.waitFor()
            if (this.processInfo != null) {
                this.processInfo.setProcessStatus(processStatus);
                //启动状态 停止
                this.processInfo.setStatus(PROCES.STATUS.STOP);
            }
        } catch (IOException e) {
            logger.error("IOException:{} {}", e.getMessage(), e);
            NotifyRoute.send(TemplateName.TEMP_NAME_CHAT_PROCESS_STATUS, MESSAGE.TYPE.ERROR, this.processInfo, e.getMessage());
        } catch (InterruptedException e) {
            logger.error("InterruptedException:{} {}", e.getMessage(), e);
            NotifyRoute.send(TemplateName.TEMP_NAME_CHAT_PROCESS_STATUS, MESSAGE.TYPE.ERROR, this.processInfo, e.getMessage());
        } finally {
            close(ss, s);
        }
    }

    /**
     * 创建子进程 - 非织入方式
     *
     * @return 返回进程
     */
    private Process createProcess() throws IOException {
        logger.info("开始创建子进程 .....");
        boolean isWindows = ProcessUtil.isWindows();
        List<String> command = new ArrayList<>();
        command.add("java");
        //应用根目录
        String appRootPath = System.getProperty("user.dir");
        logger.info("APP ROOT PATH ==> {}", appRootPath);
        //VM参数
        //plugin -D load
        String plugins = "-DNS_TT_PLUGINS=" + PropertiesUtil.getValue("ns4.app.plugin.class");
        logger.info("VM OPTS ==> {}", plugins);
        command.add(plugins);
        String[] jvmOpts = this.getJvmOpts();
        for (String jos : jvmOpts) {
            if (jos != null && !"".equals(jos.trim())) {
                command.add(jos);
                logger.info("VM OPTS ==> {}", jos);
            }
        }

        command.add("-classpath");
        StringBuilder classPath = new StringBuilder(appRootPath);
        if (isWindows) {
            classPath.append(";");
        } else {
            classPath.append(":");
        }
        classPath.append(appRootPath).append(File.separator).append("config").append(File.separator);
        if (isWindows) {
            classPath.append(";");
        } else {
            classPath.append(":");
        }
        //plugin - agent jar
        classPath.append(appRootPath).append("/watchdog/lib/plugin/ns4_gear_watchdog-1.0-plugin.jar");
        if (isWindows) {
            classPath.append(";");
        } else {
            classPath.append(":");
        }
        classPath.append(getClassPath(appRootPath + File.separator + "lib"));

        command.add(classPath.toString());
        logger.info("CLASS PATH ==> {}", classPath);
        //服务启动入口
        command.add(this.processInfo.getAppMain());
        //ping端口
        command.add("-ping");
        command.add(String.valueOf(this.processInfo.getPingPort()));
        logger.info("父进程PING PORT为 ==> {}", this.processInfo.getPingPort());
        //app名称
        command.add("app-name:" + this.processInfo.getNs4AppConf().getAppName());
        StringBuilder cmdSb = new StringBuilder();
        for (String cmd : command) {
            cmdSb.append(cmd).append(" ");
        }
        logger.info("======================================");
        logger.info("启动子进程命令参数 ==> {}", cmdSb.toString());
        logger.info("======================================");
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.redirectErrorStream(true);
        logger.info("启动子进程命令准备完成，准备启动子进程");
        return builder.start();
    }

    /**
     * 创建NS4子进程示例方法，用于调式直接启动<br>
     * 用于单元测试类 WatchDog
     *
     * @return 返回进程
     */
    private Process createProcessDemo() throws IOException {
        logger.info("创建子进程 .....");
        boolean isWindows = ProcessUtil.isWindows();
        List<String> command = new ArrayList<>();
        //NS4应用目录
        //watchdog install target目录，agent.jar
        command.add("java");
        String[] jvmOpts = this.getJvmOpts();
        //plugin -D load
        String plugins = "-DNS_TT_PLUGINS=" + PropertiesUtil.getValue("ns4.app.plugin.class");
        logger.info("VM OPTS ==> {}", plugins);
        command.add(plugins);
        for (String jos : jvmOpts) {
            if (jos != null && !"".equals(jos.trim())) {
                command.add(jos);
            }
        }
        String basePath = System.getProperty("user.dir");
        logger.info("basePath---》{}", basePath);
        command.add("-classpath");


        //应用目录 Demo 1:NSTransporter Path
//        String nsAppPath = basePath + "/ns4_gear_idgen/target/ns4_gear_idgen-1.0-SNAPSHOT";
        String nsAppPath = basePath + "/ns4_gear/ns4_gear_idgen/target/ns4_gear_idgen-1.0-SNAPSHOT";
        //应用目录 Demo 1:NSTransporter Path
        //String nsAppPath = basePath + "/ns4_gear_controller/target/ns4_gear_controller-1.0-SNAPSHOT";
        //应用目录 Demo 2:NSDispatcher Path
        //String nsAppPath = basePath + "/ns4_gear_dispatcher/target/ns4_gear_dispatcher-1.0-SNAPSHOT";
        StringBuilder classPath = new StringBuilder(nsAppPath);
        if (isWindows) {
            classPath.append(";");
        } else {
            classPath.append(":");
        }
        classPath.append(nsAppPath).append(File.separator).append("config").append(File.separator);
        if (isWindows) {
            classPath.append(";");
        } else {
            classPath.append(":");
        }
        classPath.append(basePath).append("/ns4_gear/ns4_gear_watchdog/target/watchdog/lib/plugin/ns4_gear_watchdog-1.0-plugin.jar");
        if (isWindows) {
            classPath.append(";");
        } else {
            classPath.append(":");
        }
        classPath.append(getClassPath(nsAppPath + File.separator + "lib"));

        //plugin classpath

        command.add(classPath.toString());
        //服务启动入口
        command.add(this.processInfo.getAppMain());
        //ping端口
        command.add("-ping");
        command.add(String.valueOf(this.processInfo.getPingPort()));
        //app名称
        command.add("app-name:" + this.processInfo.getNs4AppConf().getAppName());
        StringBuilder cmdSb = new StringBuilder();
        for (String cmd : command) {
            cmdSb.append(cmd).append(" ");
        }
        logger.info("======================================");
        logger.info("启动子进程命令参数 ==> {}", cmdSb.toString());
        logger.info("======================================");
        System.out.println(cmdSb.toString());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.redirectErrorStream(true);
        return builder.start();

    }

    /**
     * 读取输入流
     *
     * @param in   输入流
     * @param type 类型 0正常信息，1错误信息
     */
    private void readInputStream(InputStream in, int type) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String msg;
            while ((msg = reader.readLine()) != null) {
                System.out.println(msg);
                //日志归集
                //collectLog(msg);
            }
        } catch (IOException e) {
            logger.error("读取输入流异常: {} {}", e.toString(), e);
        }
    }

    /**
     * 关闭资源
     */
    private void close(ServerSocket ss, Socket s) {
        if (this.stdOs != null) {
            try {
                logger.info("主进程 stdOs 关闭");
                this.stdOs.close();
            } catch (Exception e) {
                logger.error("关闭资源错误: {} {}", e.toString(), e);
            }
        }
        if (ss != null) {
            try {
                ss.close();
            } catch (Throwable e) {
                logger.error("关闭资源错误: {} {}", e.toString(), e);
            }
        }
        if (s != null) {
            try {
                s.close();
            } catch (Throwable e) {
                logger.error("关闭资源错误: {} {}", e.toString(), e);
            }
        }
    }


    /**
     * 获取NS4应用配置的jvm参数
     */
    private String[] getJvmOpts() {
        String jvmOpts = PropertiesUtil.getValue("ns4.app.jvm.opts");
        return jvmOpts.split(" ");
    }

    /**
     * 子进程连接ping
     */
    private Socket connectToChild(ServerSocket ss) throws IOException {
        Socket s = null;
        try {
            ss.setSoTimeout(60000);
            for (int i = 0; i < 120 && s == null; i++) {
                try {
                    //接收子进程ping消息
                    s = ss.accept();
                } catch (SocketTimeoutException e) {
                    //忽略
                    logger.warn("PING警告: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("PING 子进程异常: {} {}", e.toString(), e);
        } finally {
            ss.close();
        }
        return s;
    }

    /**
     * 进程退出状态
     */
    private void logStatus(int processStatus) {
        String type = "unknown";
        String code = " (exit code=" + processStatus + ")";
        ExitCode exitCode = ExitCode.UNKNOWN;
        if (processStatus == 0) {
            type = "normal exit";
            exitCode = ExitCode.OK;
        } else if (processStatus >= 0 && processStatus < ExitCode.values().length) {
            exitCode = ExitCode.values()[processStatus];
            type = exitCode.toString();
        } else if (processStatus >= 128 && processStatus < 128 + 32) {
            exitCode = ExitCode.SIGNAL;
            switch (processStatus - 128) {
                case 1:
                    type = "SIGHUP";
                    break;
                case 2:
                    type = "SIGINT";
                    break;
                case 3:
                    type = "SIGQUIT";
                    break;
                case 4:
                    type = "SIGILL";
                    break;
                case 5:
                    type = "SIGTRAP";
                    break;
                case 6:
                    type = "SIGABRT";
                    break;
                case 7:
                    type = "SIGBUS";
                    break;
                case 8:
                    type = "SIGFPE";
                    break;
                case 9:
                    type = "SIGKILL";
                    break;
                case 10:
                    type = "SIGUSR1";
                    break;
                case 11:
                    type = "SIGSEGV";
                    break;
                case 12:
                    type = "SIGUSR2";
                    break;
                case 13:
                    type = "SIGPIPE";
                    break;
                case 14:
                    type = "SIGALRM";
                    break;
                case 15:
                    type = "SIGTERM";
                    break;
                case 19:
                    type = "SIGSTOP";
                    break;
                default:
                    type = "signal=" + (processStatus - 128);
                    break;
            }
            code = " (signal=" + (processStatus - 128) + ")";
        }
        String msg = ("Watchdog 检测到NS4关闭，退出状态: " + type + " 退出码:" + code);
        if (this.processInfo != null) {
            //进程退出码
            this.processInfo.setExitCode(exitCode);
            //进程退出信息
            this.processInfo.setExitMessage(msg);
        }
    }

    void stop() {
        this.kill();
    }

    void kill() {
        //停止时间
        this.processInfo.setStopTime(System.currentTimeMillis());
        Process process = this.processRef.getAndSet(null);
        if (process != null) {
            try {
                process.destroy();
            } catch (Exception e) {
                logger.error("KILL destroy 异常: {} {}", e.toString(), e);
            }
        }
        OutputStream os = this.stdOs;
        this.stdOs = null;
        if (os != null) {
            try {
                os.close();
            } catch (Throwable e) {
                logger.error("KILL OutputStream close 异常: {} {}", e.toString(), e);
            }
        }
        if (process != null) {
            try {
                process.waitFor();
            } catch (Exception e) {
                logger.error("KILL process.waitFor 异常: {} {}", e.toString(), e);
            }
        }
    }

    /**
     * 获取classPath
     *
     * @param path 根目录
     */
    String getClassPath(String path) {
        boolean osFlag = ProcessUtil.isWindows();
        StringBuffer temp = new StringBuffer();
        File jarDir = new File(path);
        File[] jarFiles = jarDir.listFiles();
        int i = 0;
        for (File file : jarFiles) {
            if (i > 0) {
                if (osFlag) {
                    temp.append(";");
                } else {
                    temp.append(":");
                }
            }
            if (file.isFile() && file.getName().endsWith(".jar")) {
                temp.append(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                temp.append(getClassPath(file.getAbsolutePath()));
            }
            i++;
        }
        return (temp.toString());
    }

    /**
     * 清空 释放资源
     */
    void clear() {
        logger.info("清除 Watchdog Child Process 资源开始");
        this.stop();
        if (this.processInfo != null) {
            this.processInfo = null;
        }
        if (this.inputStreamPool != null) {
            this.inputStreamPool.shutdown();
            this.inputStreamPool = null;
        }
        if (this.stdOs != null) {
            try {
                this.stdOs.close();
            } catch (IOException e) {
                //忽略
            }
            this.stdOs = null;
        }
        this.processRef = null;
        logger.info("清除 Watchdog Child Process 资源结束");
    }
}