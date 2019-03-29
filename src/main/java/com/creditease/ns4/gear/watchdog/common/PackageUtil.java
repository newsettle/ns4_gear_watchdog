package com.creditease.ns4.gear.watchdog.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Description 包工具类
 * @Author karma
 * @Date 2019/1/18 上午11:28
 * @Version 1.0
 **/
public class PackageUtil {

    /**
     * 获取某包下（包括该包的所有子包）所有类
     *
     * @param packageName 包名
     * @return 类的完整名称
     * @throws UnsupportedEncodingException
     */
    public static List<String> getClassName(String packageName) throws IOException {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     *
     * @param packageName  包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     * @throws UnsupportedEncodingException
     */
    public static List<String> getClassName(String packageName, boolean childPackage) throws IOException {
        List<String> fileNames = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", File.separator);
        Enumeration<URL> urls = loader.getResources(packagePath);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url == null) {
                continue;
            }
            String type = url.getProtocol();
            String path = URLDecoder.decode(url.getPath(),"UTF-8");
            if ("file".equals(type)) {
                fileNames.addAll(getClassNameByFile(path, childPackage));
            } else if ("jar".equals(type)) {
                fileNames.addAll(getClassNameByJar(path, childPackage));
            }
        }
        //fileNames.addAll(getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage));
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     *
     * @param filePath     文件路径
     *                     类名集合
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     * @throws UnsupportedEncodingException
     */
    private static List<String> getClassNameByFile(String filePath, boolean childPackage) throws UnsupportedEncodingException {
        List<String> myClassName = new ArrayList<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (childFiles == null) {
            return myClassName;
        }
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), childPackage));
                }
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf(File.separator + "classes" + File.separator) + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace(File.separator, ".");
                    myClassName.add(childFilePath);
                }
            }
        }
        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath      jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     * @throws UnsupportedEncodingException
     */
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) throws UnsupportedEncodingException {
        List<String> myClassName = new ArrayList<String>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0];
        if (jarInfo[0].indexOf(File.separator) >= 0) {
            jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf(File.separator));
        }
        String packagePath = jarInfo[1].substring(1);
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace(File.separator, ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    } else {
                        int index = entryName.lastIndexOf(File.separator);
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace(File.separator, ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            //忽略
        }
        return myClassName;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     *
     * @param urls         URL集合
     * @param packagePath  包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     * @throws UnsupportedEncodingException
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) throws UnsupportedEncodingException {
        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes" + File.separator)) {
                    continue;
                }
                String jarPath = urlPath + "!" + File.separator + packagePath;
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }

}
