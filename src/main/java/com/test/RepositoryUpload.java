package com.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RepositoryUpload {

    /*
        mvn deploy:deploy-file -DgroupId=com.maven.api -DartifactId=maven-api -Dversion=1.0 -Dpackaging=jar -Dfile=本地jar包的地址 -DpomFile=本地pom包的地址 -Durl=上传到的私有仓库的地址 -DrepositoryId=nexus -DpomFile=D:\commons-lang-2.3.pom
        mvn deploy:deploy-file -DgroupId=fakepath -DartifactId=p-auth-client -Dversion=1.0.3 -Dpackaging=jar -Dfile=D:\maven_repository_bs\fakepath\p-auth-client\1.0.3\p-auth-client-1.0.3-RELEASE.jar -DpomFile=D:\maven_repository_bs\fakepath\p-auth-client\1.0.3\p-auth-client-1.0.3.pom -Durl=http://192.168.1.2:8081/repository/maven-releases/ -DrepositoryId=nexus-releases

        mvn deploy:deploy-file -DgroupId=com.xx.xx -DartifactId=xx -Dversion=1.0.0 -Dpackaging=jar -Dfile=xx.jar -Durl=仓库地址 -DrepositoryId=仓库名
        mvn deploy:deploy-file -DgroupId=com.xx.xx -DartifactId=xx -Dversion=1.0.0 -Dpackaging=jar -Dclassifier=sources -Dfile=xx-sources.jar -Durl=仓库地址 -DrepositoryId=仓库名
        mvn deploy:deploy-file -DgroupId=com.xx.xx -DartifactId=xx -Dversion=1.0.0 -Dpackaging=pom -Dfile=pom.xml -Durl=仓库地址 -DrepositoryId=仓库名
    */

    // maven配置文件
    private static String url = "http://192.168.1.2:8081/repository/maven-releases/";
    private static String repositoryId = "nexus-releases";
    private static String mvn = "mvn deploy:deploy-file";

    private static String path = "d:/maven_repository_tg";

    public String getShellString(String path) throws Exception {
        File file = new File(path);
        StringBuilder sb = new StringBuilder();
        if (file.exists() && file.isDirectory()) {
            List<JarObject> jarList = getJarFileList(file);
            for (JarObject jarObject : jarList) {
                sb.append(mvn);
                if (jarObject.getGroupId() != null && !"".equals(jarObject.getGroupId())) {
                    sb.append(" -DgroupId=").append(jarObject.getGroupId());
                }
                if (jarObject.getArtifactId() != null && !"".equals(jarObject.getArtifactId())) {
                    sb.append(" -DartifactId=").append(jarObject.getArtifactId());
                }
                if (jarObject.getVersion() != null && !"".equals(jarObject.getVersion())) {
                    sb.append(" -Dversion=").append(jarObject.getVersion());
                }
                if (jarObject.getPackaging() != null && !"".equals(jarObject.getPackaging())) {
                    sb.append(" -Dpackaging=").append(jarObject.getPackaging());
                }
                if (jarObject.getFile() != null && !"".equals(jarObject.getFile())) {
                    sb.append(" -Dfile=").append(jarObject.getFile());
                }
                if (jarObject.getPomFile() != null && !"".equals(jarObject.getPomFile())) {
                    sb.append(" -DpomFile=").append(jarObject.getPomFile());
                }
                sb.append(" -Durl=").append(url);
                sb.append(" -DrepositoryId=").append(repositoryId);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private List<JarObject> getJarFileList(File filePath) throws Exception {
        List<JarObject> jarList = new ArrayList<>();
        File[] files = filePath.listFiles();
        if(files == null){
            return jarList;
        }
        for(File file : files){
            if(file.isDirectory()){
                List<JarObject> childList = getJarFileList(file);
                jarList.addAll(childList);
            }else{
                JarObject jarObject = getJarObject(file, jarList);
                if (jarObject != null) {
                    jarList.add(jarObject);
                }
            }
        }
        return jarList;
    }

    private JarObject getJarObject(File file, List<JarObject> jarList) throws Exception {
        JarObject jarObject = null;
        String filePath = file.getCanonicalPath();
        if (filePath.toLowerCase().endsWith(".jar") &&
                !filePath.toLowerCase().endsWith("sources.jar") &&
                !filePath.toLowerCase().endsWith("tests.jar")) {
            // D:\maven_repository_bs\fakepath\p-auth-client\1.0.3\p-auth-client-1.0.3-RELEASE.jar
            // D:\maven_repository_bs\fakepath\p-auth-client\1.0.3\p-auth-client-1.0.3.pom
            // D:\maven_repository_bs\com\alibaba\dubbo\2.8.4\dubbo-2.8.4-sources.jar
            jarObject = getJarObject(filePath);
            String pomFile = getFilePath(file, ".pom");
            String sourcesFile = getFilePath(file, "sources.jar");
            jarObject.setPomFile(pomFile);
            jarObject.setPackaging("jar");
        }
        if (filePath.toLowerCase().endsWith(".pom")) {
            boolean flag = false;
            for (JarObject jarObject1 : jarList) {
                if (jarObject1.getFile().contains(filePath) || jarObject1.getPomFile().contains(filePath)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                jarObject = getJarObject(filePath);
                jarObject.setPackaging("pom");
            }
        }
        return jarObject;
    }

    private JarObject getJarObject(String filePath) {
        JarObject jarObject = new JarObject();
        String subPath = filePath.substring(path.length() + 1, filePath.lastIndexOf("\\"));
        String version = subPath.substring(subPath.lastIndexOf("\\") + 1);
        subPath = subPath.substring(0, subPath.lastIndexOf("\\"));
        String artifactId = subPath.substring(subPath.lastIndexOf("\\") + 1);
        String groupId = subPath.substring(0, subPath.lastIndexOf("\\"));
        groupId = groupId.replaceAll("\\\\", ".");
        jarObject.setGroupId(groupId);
        jarObject.setArtifactId(artifactId);
        jarObject.setVersion(version);
        jarObject.setFile(filePath);
        return jarObject;
    }

    private String getFilePath(File file, String ext) throws Exception {
        String pomFilePath = "";
        String fileDirectory = file.getCanonicalPath();
        String path = fileDirectory.substring(0, fileDirectory.lastIndexOf("\\"));
        File fileDir = new File(path);
        if (fileDir.exists() && fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            if(files == null){
                return pomFilePath;
            }
            for(File fileTemp : files){
                if(fileTemp.isFile()){
                    String fileTempPath = fileTemp.getCanonicalPath();
                    if (fileTempPath.toLowerCase().endsWith(ext)) {
                        pomFilePath = fileTempPath;
                        break;
                    }
                }
            }
        }
        return pomFilePath;
    }

    private class JarObject {
        private String groupId;
        private String artifactId;
        private String version;
        private String file;
        private String pomFile;
        private String sourcesFile;
        private String packaging;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getPomFile() {
            return pomFile;
        }

        public void setPomFile(String pomFile) {
            this.pomFile = pomFile;
        }

        public String getSourcesFile() {
            return sourcesFile;
        }

        public void setSourcesFile(String sourcesFile) {
            this.sourcesFile = sourcesFile;
        }

        public String getPackaging() {
            return packaging;
        }

        public void setPackaging(String packaging) {
            this.packaging = packaging;
        }
    }

    public static void main(String ... args) throws Exception {
        RepositoryUpload repositoryUpload = new RepositoryUpload();
        String shellString = repositoryUpload.getShellString(path);
        System.out.println(shellString);
    }

}
