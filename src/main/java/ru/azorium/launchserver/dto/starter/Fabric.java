package ru.azorium.launchserver.dto.starter;

import java.util.List;

public class Fabric {

    public static class Manifest{
        public List<Library> libraries;
    }

    public static class Library{
        public String name;
    }

}
