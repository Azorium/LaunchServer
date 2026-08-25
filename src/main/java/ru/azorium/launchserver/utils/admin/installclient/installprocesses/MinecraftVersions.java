package ru.azorium.launchserver.utils.admin.installclient.installprocesses;

public class MinecraftVersions {

    public static int getMinorNum(String[] parts) {
        return Integer.parseInt(parts[1]);
    }

    public static String[] getParts(String target){
        return target.split("\\.");
    }

}
