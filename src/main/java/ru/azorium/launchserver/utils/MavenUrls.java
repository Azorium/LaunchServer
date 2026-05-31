package ru.azorium.launchserver.utils;

public class MavenUrls {

    public static String mavenToPath(String coordinates) {
        String[] parts = coordinates.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid Maven coordinates. Expected format: groupId:artifactId:version[:classifier]");
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        String classifier = parts.length > 3 ? "-" + parts[3] : "";

        String groupPath = groupId.replace('.', '/');

        return String.join("/",
                groupPath,
                artifactId,
                version,
                artifactId + "-" + version + classifier + ".jar"
        );
    }

}
