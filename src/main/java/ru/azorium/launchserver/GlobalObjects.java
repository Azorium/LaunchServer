package ru.azorium.launchserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GlobalObjects {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

}
