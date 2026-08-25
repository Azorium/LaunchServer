package ru.azorium.launchserver.model.dto.starter;

import java.util.List;

public class Neoforge {

    public static class Manifest {
        public Arguments arguments;
        public List<Library> libraries;
    }

    public static class Arguments {
        public List<String> jvm;
    }


    public static class Library {
        public Downloads downloads;
    }

    public static class Downloads {
        public Artifact artifact;
    }

    public static class Artifact {
        public String path;
    }

}
