package com.creditease.ns4.gear.watchdog.common;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author outman
 * @description Jar辅助类
 * @date 2019/1/15
 */
public class JarUtil {

    /**
     * 提取资源为Properties
     *
     * @param jarFilePath
     * @param resourcePath
     * @return
     */
    public static Properties getResourceAsProperties(String jarFilePath, String resourcePath) {

        InputStream is = getResourceAsStream(jarFilePath, resourcePath);

        Properties props = new Properties();
        try {
            props.load(is);
            return props;
        } catch (IOException e) {
            // ignore
        } finally {

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return null;
    }

    /**
     * 提取资源为InputStream
     *
     * @param jarFilePath
     * @param resourcePath
     * @return
     */
    public static InputStream getResourceAsStream(String jarFilePath, String resourcePath) {

        File file = new File(jarFilePath);

        if (!file.exists()) {
            return null;
        }

        try {
            @SuppressWarnings("resource")
            URLClassLoader cl = new URLClassLoader(new URL[]{file.toURI().toURL()});

            InputStream is = cl.getResourceAsStream(resourcePath);

            return is;

        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    public static URL[] loadJars(String... libPaths) {

        List<File> files = new ArrayList<File>();

        for (String libPath : libPaths) {

            File plusRoot = new File(libPath);

            if (!plusRoot.exists()) {
                continue;
            }

            if (plusRoot.isFile() && libPath.endsWith(".jar")) {
                files.add(plusRoot);
                continue;
            } else if (plusRoot.isDirectory()) {
                File[] jarFiles = plusRoot.listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(File dir, String name) {

                        if (name.endsWith(".jar")) {
                            return true;
                        }

                        return false;
                    }

                });

                if (jarFiles == null || jarFiles.length == 0) {
                    continue;
                }

                files.addAll(Arrays.asList(jarFiles));
            }

        }

        List<URL> jarURLs = new ArrayList<URL>();

        for (File jar : files) {
            try {
                jarURLs.add(jar.toURI().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }

        URL[] jarURLArray = new URL[jarURLs.size()];
        jarURLs.toArray(jarURLArray);
        return jarURLArray;
    }
}
