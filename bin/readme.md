>备注(包含watchdog启动脚本示例)

1.修改watchdog.properties中应用对应的配置
  
  ns4.app.jvm.opts=-Dconfigfile=nscontroller.xml -Dfile.encoding=utf-8 -Duser.timezone=GMT+8 -server -Xms1024m -Xmx1024m -XX:MaxPermSize=128m -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=5 -XX:+PrintGC -XX:+PrintGCTimeStamps  -XX:+PrintGCDetails  -XX:+PrintGCApplicationStoppedTime -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps -Xloggc:log/controllerGC.log
  
  ns4.app.server.main=NSTransporter -->NS4应用服务入口(NSDispatcher, NSTransporter)
  
  ns4.app.server.name=controller_app -->NS4应用服务名称默认:default-app
  
2.启动watchdog脚本
