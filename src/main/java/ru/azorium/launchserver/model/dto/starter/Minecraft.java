package ru.azorium.launchserver.model.dto.starter;

import java.util.List;
import java.util.Map;

public class Minecraft {
    public static class Manifest {
        public List<Library> libraries;
    }

    public static class Library {
        public Downloads downloads;
        public List<Rule> rules;
    }

    public static class Downloads {
        public Artifact artifact;
        public Map<String, Classifier> classifiers;
    }

    public static class Artifact {
        public String path;
    }

    public static class Classifier {
        public String path;
    }

    public static class Rule {
        public String action;
        public OS os;
    }

    public static class OS {
        public String name;
    }
}
