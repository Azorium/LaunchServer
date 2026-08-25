package ru.azorium.launchserver.utils.admin.installclient.installprocesses;

import com.google.gson.Gson;
import ru.azorium.launchserver.model.dto.starter.Fabric;
import ru.azorium.launchserver.model.dto.starter.Forge;
import ru.azorium.launchserver.model.dto.starter.Minecraft;
import ru.azorium.launchserver.model.dto.starter.Neoforge;
import ru.azorium.launchserver.utils.MavenUrls;
import ru.azorium.launchserver.utils.gson.JsonHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClasspathBuilder {
    Path minecraftManifestPath;
    Minecraft.Manifest minecraftManifest;

    Path loaderManifestPath;
    Forge.Manifest forgeManifest;
    Neoforge.Manifest neoforgeManifest;
    Fabric.Manifest fabricManifest;

    Path clientPath;
    String mcVersion;
    Gson gson;
    List<String> libs;
    String loader;
    String loaderVersion;
    Path librariesPath;
    String os;

    int minorNum;

    /**
     * Библиотеки Minecraft, которые конфликтуют с библиотеками Forge (forge зависит от этих же библиотек версий выше и качает их)
     **/
    static List<String> forgeConflictLibs = new ArrayList<>();


    static {
        // Forge 1.7.10
        forgeConflictLibs.add("com/google/guava/guava/15.0/guava-15.0.jar");

        // Forge 1.16.5
        forgeConflictLibs.add("org/apache/logging/log4j/log4j-api/2.8.1/log4j-api-2.8.1.jar");
        forgeConflictLibs.add("org/apache/logging/log4j/log4j-core/2.8.1/log4j-core-2.8.1.jar");
    }

    public ClasspathBuilder(Path clientPath, String mcVersion, Gson gson, String loaderVersion, String loaderType, String os) {
        this.clientPath = clientPath;
        this.mcVersion = mcVersion;
        this.gson = gson;
        this.loaderVersion = loaderVersion;
        this.loader = loaderType;
        this.os = os;

        this.librariesPath = clientPath.resolve("libraries");
        this.minorNum = MinecraftVersions.getMinorNum(MinecraftVersions.getParts(mcVersion));
        this.libs = new ArrayList<>();
    }

    public String build() throws IOException {
        getVersionsFiles();
        minecraftManifest = JsonHelper.objectDeserializerFromPath(minecraftManifestPath, Minecraft.Manifest.class);
        buildMinecraftClasspath();

        switch (loader) {
            case "forge" -> {
                forgeManifest = JsonHelper.objectDeserializerFromPath(loaderManifestPath, Forge.Manifest.class);
                buildForgeClasspath();
            }
            case "fabric" -> {
                fabricManifest = JsonHelper.objectDeserializerFromPath(loaderManifestPath, Fabric.Manifest.class);
                buildFabricClasspath();
            }
            case "neoforge" -> {
                neoforgeManifest = JsonHelper.objectDeserializerFromPath(loaderManifestPath, Neoforge.Manifest.class);
                buildNeoforgeClasspath();
            }
            case "vanilla" -> {
            }
        }

        return String.join("${classpath_separator}", libs);
    }

    private void getVersionsFiles() throws IOException {
        Files.walk(clientPath.resolve("versions"))
                .filter(path -> !path.toFile().isDirectory())
                .forEach(path -> {
                    if (path.toString().endsWith(".json")) {
                        if (path.toFile().getName().equals(mcVersion + ".json")) {
                            minecraftManifestPath = path;
                        } else {
                            loaderManifestPath = path;
                        }
                    } else {
                        // Forge подсасывает патченный инсталлером клиент майна, оригинальный вызывет несовместимость
                        if (loader == null) {
                            libs.add("${client_path}" + path.toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
                        } else {
                            if (loader.contains("forge")) {
                                if (minorNum < 18) {
                                    libs.add("${client_path}" + path.toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
                                }
                            } else {
                                libs.add("${client_path}" + path.toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
                            }
                        }
                    }
                });
    }

    private void buildMinecraftClasspath() {
        minecraftManifest.libraries.forEach(lib -> {
            if (lib.downloads.artifact != null) {
                if (lib.rules == null) {
                    if (loader != null && forgeConflictLibs.contains(lib.downloads.artifact.path)) {
                        return;
                    }
                    libs.add("${client_path}" + clientPath.resolve("libraries").resolve(lib.downloads.artifact.path).toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
                }

                if (lib.rules != null && minorNum > 7) {
                    lib.rules.forEach(rule -> {
                        if (rule.action.equals("allow") && rule.os == null) {
                            libs.add("${client_path}" + clientPath.resolve("libraries").resolve(lib.downloads.artifact.path).toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
                        } else if (rule.action.equals("allow")) {
                            if (os.equals("macos")) {
                                libs.add("${client_path}" + clientPath.resolve("libraries").resolve(lib.downloads.artifact.path).toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
                            }
                        }
                    });
                }
            }
        });
    }

    private void buildFabricClasspath() {
        fabricManifest.libraries.forEach(lib -> {
            String path = MavenUrls.mavenToPath(lib.name);
            libs.add("${client_path}" + librariesPath.resolve(path).toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
        });
    }

    private void buildForgeClasspath() {
        if (forgeManifest.versionInfo != null) {
            forgeManifest.versionInfo.libraries.forEach(library -> {
                String libPath = MavenUrls.mavenToPath(library.name);

                if (MinecraftVersions.getMinorNum(MinecraftVersions.getParts(mcVersion)) == 7 && libPath.contains("net/minecraftforge/forge")) {
                    String[] parts = libPath.split("\\.jar", 2);
                    libPath = parts[0] + "-universal" + ".jar";
                }

                libs.add("${client_path}" + librariesPath.resolve(libPath).toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
            });
        }
        // От 1.12.2
        else {
            forgeManifest.libraries.forEach(library -> {
                libs.add("${client_path}" + librariesPath.resolve(library.downloads.artifact.path).toString().replace(clientPath.toString(), "").replace(File.separator, "/"));
            });
        }

    }

    private void buildNeoforgeClasspath() {
        String securityJvmArg = neoforgeManifest.arguments.jvm.stream().filter(arg -> arg.contains("bootstraplauncher")).collect(Collectors.joining());

        neoforgeManifest.libraries.forEach(library -> {
            String fullLibPath = librariesPath.resolve(library.downloads.artifact.path).toString();
            String formattedLibPath = "${client_path}" + fullLibPath.replace(clientPath.toString(), "").replace(File.separator, "/");

            if (!libs.contains(formattedLibPath) && !securityJvmArg.contains(library.downloads.artifact.path)) {
                libs.add(formattedLibPath);
            }
        });
    }

}
