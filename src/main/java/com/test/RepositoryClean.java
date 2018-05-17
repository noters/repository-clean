package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class RepositoryClean {

    private static String[] env = {"M2_HOME", "MAVEN_HOME"};
    // maven配置文件目录
    private static String[] xml = {"conf/settings.xml", ".m2/settings.xml"};
    // maven配置文件里仓库标签
    private static String tab = "localRepository";
    private static String[] flagPath = {"/com", "/org"};

    private String getPath() {
        URL url = RepositoryClean.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = null;
        try {
            filePath = URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filePath != null) {
            if (filePath.endsWith(".jar")) {
                filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            }
            File file = new File(filePath);
            filePath = file.getAbsolutePath();
        }
        return filePath;
    }

    private List<String> getMavenPath() {
        List<String> list = new ArrayList<>();
        for (String key : env) {
            // 获取环境变量
            String value = System.getenv(key);
            addList(list, value);
            // 查找maven配置文件，并获取仓库目录
            List<String> xmlList = getXml(value, xml[0]);
            for (String str : xmlList) {
                addList(list, str);
            }
        }
        // 获取用户目录
        Properties properties = System.getProperties();
        String home = properties.getProperty("user.home");
        addList(list, home);
        List<String> xmlList = getXml(home, xml[1]);
        for (String str : xmlList) {
            addList(list, str);
        }
        addList(list, properties.getProperty("user.dir"));
        addList(list, getPath());
        return list;
    }

    private List<String> getXml(String path, String filePath) {
        // 读取配置文件，找到maven仓库目录
        List<String> list = new ArrayList<>();
        String fileName = path + "/" + filePath;
        File file = new File(fileName);
        if (file.exists()) {
            try {
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String str;
                while ((str = br.readLine()) != null) {
                    int index = str.indexOf(tab);
                    if (index >= 0) {
                        int startIndex = index + tab.length() + 1;
                        int endIndex = str.indexOf(tab, startIndex) - 2;
                        if (startIndex < str.length() && endIndex < str.length() && endIndex >= 0) {
                            String value = str.substring(startIndex, endIndex);
                            list.add(value);
                        }
                    }
                }
                br.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private void addList(List<String> list, String addValue) {
        if (addValue != null && !"".equals(addValue)) {
            boolean flag = false;
            for (String value : list) {
                if (addValue.equals(value)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                list.add(addValue);
            }
        }
    }

    private String getSelect(List<String> list, boolean flag) {
        System.out.println("请选择路径");
        for (int i = 0; i < list.size(); i ++) {
            String path = list.get(i);
            System.out.println((i + 1) + ". " + path);
        }
        System.out.println((list.size() + 1) + ". 退出");
        System.out.println();
        String inPath = null;
        if (flag) {
            // 如果目录下有com及org目录，就默认选择
            for (String path : list) {
                boolean flagTemp = false;
                for (String pathString : flagPath) {
                    File file = new File(path + pathString);
                    if (!file.exists()) {
                        flagTemp = true;
                    }
                }
                if (!flagTemp) {
                    inPath = path;
                }
            }
            System.out.println("默认选择: " + inPath);
        } else {
            Scanner scanner = new Scanner(System.in);
            int in = scanner.nextInt();
            if (in <= list.size()) {
                inPath = list.get(in - 1);
            } else if (in == list.size() + 1) {
                System.exit(0);
            }
        }
        System.out.println();
        return inPath;
    }

    private void deletePath(String path) {
        File file = new File(path);
        if (file.exists()) {
            List<String> deletePath = new ArrayList<>();
            // 获取所有的子文件，不包含目录
            List<String> childPath = getAllFilePaths(file);
            // 文件扩展名最后是.lastUpdated则保存此目录
            for (String fileName : childPath) {
                if (fileName.endsWith(".lastUpdated")) {
                    fileName = fileName.replaceAll("\\\\", "/");
                    String filePath = fileName.substring(0, fileName.lastIndexOf("/"));
                    addList(deletePath, filePath);
                }
            }
            System.out.println("删除目录");
            for (String delete : deletePath) {
                System.out.println(delete);
            }
            System.out.println();
            for (String deleteName : deletePath) {
                File fileName = new File(deleteName);
                File[] files = fileName.listFiles();
                for (File fileTemp : files) {
                    fileTemp.delete();
                }
                fileName.delete();
            }
        }
    }

    private static List<String> getAllFilePaths(File filePath){
        List<String> filePaths = new ArrayList<>();
        File[] files = filePath.listFiles();
        if(files == null){
            return filePaths;
        }
        for(File file : files){
            if(file.isDirectory()){
                //filePaths.add(file.getPath());
                List<String> childList = getAllFilePaths(file);
                filePaths.addAll(childList);
            }else{
                filePaths.add(file.getPath());
            }
        }
        return filePaths;
     }

    public void start(String in) {
        List<String> list = getMavenPath();
        String path = null;
        // 启动不输入参数就自动选择目录，选择失败就手动选择
        if (in == null) {
            path = getSelect(list, true);
        }
        while (path == null || "".equals(path)) {
            path = getSelect(list, false);
        }
        deletePath(path);
    }

    public static void main(String ... args) {
        RepositoryClean repositoryClean = new RepositoryClean();
        repositoryClean.start(args.length > 0 ? args[0] : null);
    }
}
