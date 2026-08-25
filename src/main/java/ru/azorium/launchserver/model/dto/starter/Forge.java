package ru.azorium.launchserver.model.dto.starter;

import java.util.List;

public class Forge {
    /**
     * Manifest.versionInfo - от 1.7.10 (возможен промежуток до 1.12.2, не проверял)
     * Manifest.libraries - как минимум от 1.12.2
     *
     **/
    public static class Manifest {
        public VersionInfo versionInfo;
        public List<Library> libraries;
    }

    public static class VersionInfo {
        public List<Library> libraries;
    }

    /**
     * Всё что после String name - для Forge от 1.12.2 (возможен промежуток 1.7.10-1.12.2, не проверял)
     *
     **/
    public static class Library {
        public String name;

        public Downloads downloads;
    }

    public static class Downloads {
        public Artifact artifact;
    }

    public static class Artifact {
        public String path;
    }


}
