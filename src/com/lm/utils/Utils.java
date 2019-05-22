package com.lm.utils;

import com.intellij.openapi.ui.Messages;

public class Utils {

    public static boolean checkParams(String params, String errorMsg) {
        if (params == null || params.trim().length() == 0) {
            throw new IllegalArgumentException(errorMsg);
        }
        return true;
    }

    public static void showMessageDialog(String message) {
        Messages.showMessageDialog(message, "MESSAGE", Messages.getInformationIcon());
    }

    public static void showErrorDialog(String message) {
        Messages.showErrorDialog(message, "ERROR");
    }

    public static String buildUploadScript(String url, String userName, String password, String groupId, String artifactId, String version, String packaging, String description) {
        Utils.checkParams(url, "url == null");
        Utils.checkParams(userName, "userName == null");
        Utils.checkParams(password, "password == null");
        Utils.checkParams(groupId, "groupId == null");
        Utils.checkParams(artifactId, "artifactId == null");
        Utils.checkParams(version, "version == null");
        Utils.checkParams(packaging, "packaging == null");
        Utils.checkParams(description, "description == null");
        return new StringBuilder()
                .append("def nexus_maven = [:]\n")
                .append("nexus_maven.url = \"").append(url).append("\"\n")
                .append("nexus_maven.userName = \"").append(userName).append("\"\n")
                .append("nexus_maven.password = \"").append(password).append("\"\n")
                .append("nexus_maven.groupId = \"").append(groupId).append("\"\n")
                .append("nexus_maven.artifactId = \"").append(artifactId).append("\"\n")
                .append("nexus_maven.version = \"").append(version).append("\"\n")
                .append("nexus_maven.packaging = \"").append(packaging).append("\"\n")
                .append("nexus_maven.description = \"").append(description).append("\"\n")
                .append("\n")
                .append("apply plugin: 'maven'\n")
                .append("\n")
                .append("task androidJavadocs(type: Javadoc) {\n")
                .append("    options.encoding = \"UTF-8\"\n")
                .append("    source = android.sourceSets.main.java.srcDirs\n")
                .append("    classpath += project.files(android.getBootClasspath().join(File.pathSeparator))\n")
                .append("}\n")
                .append("\n")
                .append("task androidJavadocsJar(type: Jar, dependsOn: androidJavadocs) {\n")
                .append("    classifier = 'javadoc'\n")
                .append("    from androidJavadocs.destinationDir\n")
                .append("}\n")
                .append("\n")
                .append("task androidSourcesJar(type: Jar) {\n")
                .append("    classifier = 'sources'\n")
                .append("    from android.sourceSets.main.java.srcDirs\n")
                .append("}\n")
                .append("\n")
                .append("artifacts {\n")
                .append("    archives androidSourcesJar\n")
                .append("    archives androidJavadocsJar\n")
                .append("}\n")
                .append("\n")
                .append("uploadArchives {\n")
                .append("    repositories {\n")
                .append("        mavenDeployer {\n")
                .append("            repository(url: \"$nexus_maven.url\") {\n")
                .append("                authentication(userName: \"$nexus_maven.userName\", password: \"$nexus_maven.password\")\n")
                .append("            }\n")
                .append("            pom.project {\n")
                .append("                groupId \"$nexus_maven.groupId\"\n")
                .append("                artifactId \"$nexus_maven.artifactId\"\n")
                .append("                version \"$nexus_maven.version\"\n")
                .append("                packaging \"$nexus_maven.packaging\"\n")
                .append("                description \"$nexus_maven.description\"\n")
                .append("            }\n")
                .append("        }\n")
                .append("    }\n")
                .append("}")
                .toString();
    }
}
