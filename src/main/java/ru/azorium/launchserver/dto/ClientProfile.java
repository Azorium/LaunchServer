package ru.azorium.launchserver.dto;
import java.util.List;

public class ClientProfile {
    public int id;
    public String mcVersion;
    public String loaderType;
    public String loaderVersion;
    public Frontend frontend;
    public Backend backend;
    public List<OptionalMod> optionalMods;

    public static class Frontend {
        public String name;
        public String description;
        public String image;
    }

    public static class Backend {
        public String ip;
        public String port;
        public String folderName;
        public String javaVersion;
        public String mainClass;
        public Arguments arguments;
        public Classpaths classpaths;
    }

    public static class Arguments {
        public List<String> game;
        public List<String> jvm;
    }

    public static class Classpaths {
        public String windows;
        public String linux;
        public String macos;
    }

    public class OptionalMod {
        public String name;
        public String description;
        public String fileName;
        public boolean value;
    }
}


