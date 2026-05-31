package ru.azorium.launchserver.utils;

public class Yggdrasil {
    public static String profileIdToUuid(String profileId) {
                return profileId.replaceFirst(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"
        );
    }
}
