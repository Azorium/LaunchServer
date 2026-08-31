package ru.azorium.launchserver.config;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppHashes {
    private Map<String, String> launcherHashes = new HashMap<>();
    private String runtimeZipHash;

    public Map<String, String> getLauncherHashes() {
        return launcherHashes;
    }

    public void setLauncherHashes(Map<String, String> launcherHashes) {
        this.launcherHashes = launcherHashes;
    }

    public String getRuntimeZipHash() {
        return runtimeZipHash;
    }

    public void setRuntimeZipHash(String runtimeZipHash) {
        this.runtimeZipHash = runtimeZipHash;
    }
}
