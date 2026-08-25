package ru.azorium.launchserver.utils.admin.installclient.installprocesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.utils.admin.EventLogger;
import ru.azorium.launchserver.utils.admin.installclient.InstallContext;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NativeExtractor {
    private static final Logger logger = LoggerFactory.getLogger(NativeExtractor.class);
    static Path manifestPath;
    static Path librariesRoot;
    static Path nativesRoot;

    public static void start(InstallContext context) throws IOException {
        context.logger().info(new EventLogger.InstallEventMessage(null, "Извлечение нативов", "Извлечение нативов"));

        NativeExtractor.manifestPath = context.versionsPath().resolve(context.mcVersion()).resolve(context.mcVersion() + ".json");;
        NativeExtractor.librariesRoot = context.librariesPath();
        NativeExtractor.nativesRoot = context.nativesPath();

        Manifest manifest;
        try (Reader reader = Files.newBufferedReader(manifestPath)) {
            manifest = GlobalObjects.gson.fromJson(reader, Manifest.class);
        }

        List<ExtractionTask> tasks = new ArrayList<>();
        for (Library lib : manifest.libraries) {
            if (lib.downloads != null && lib.downloads.classifiers != null && !lib.downloads.classifiers.isEmpty()) {
                for (Map.Entry<String, Classifier> entry : lib.downloads.classifiers.entrySet()) {
                    String classifierName = entry.getKey();
                    Classifier classifier = entry.getValue();
                    String jarPath = classifier.path;
                    if (jarPath == null) continue;

                    Path sourceJar = librariesRoot.resolve(jarPath.replace('/', File.separatorChar));

                    OsArch osArch = parseClassifier(classifierName);
                    Path targetDir = nativesRoot.resolve(osArch.os + "-" + osArch.arch);
                    List<String> exclude = (lib.extract != null && lib.extract.exclude != null)
                            ? lib.extract.exclude
                            : Collections.emptyList();

                    tasks.add(new ExtractionTask(sourceJar, targetDir, exclude));
                }
            }

            else if (lib.downloads != null && lib.downloads.artifact != null && lib.name.contains(":")) {
                String[] parts = lib.name.split(":");
                if (parts.length >= 4) {
                    String classifierName = parts[3];
                    Artifact artifact = lib.downloads.artifact;
                    String jarPath = artifact.path;
                    if (jarPath == null) continue;

                    Path sourceJar = librariesRoot.resolve(jarPath.replace('/', File.separatorChar));

                    OsArch osArch = parseClassifier(classifierName);
                    Path targetDir = nativesRoot.resolve(osArch.os + "-" + osArch.arch);
                    List<String> exclude = (lib.extract != null && lib.extract.exclude != null)
                            ? lib.extract.exclude
                            : Collections.emptyList();

                    tasks.add(new ExtractionTask(sourceJar, targetDir, exclude));
                }
            }
        }

        Set<Path> processed = new HashSet<>();
        for (ExtractionTask task : tasks) {
            if (processed.add(task.sourceJar)) {
                extract(task.sourceJar, task.targetDir, task.exclude);
            }
        }
    }

    private static OsArch parseClassifier(String classifier) {
        String[] parts = classifier.split("-");
        if (parts.length < 2) {
            return new OsArch("unknown", "default");
        }

        String osPart = parts[1].toLowerCase();
        String os;
        switch (osPart) {
            case "linux":
                os = "linux";
                break;
            case "osx":
            case "macos":
                os = "osx";
                break;
            case "windows":
                os = "windows";
                break;
            default:
                os = osPart;
        }

        String arch = "default";
        if (parts.length > 2) {
            String archPart = String.join("-", Arrays.copyOfRange(parts, 2, parts.length)).toLowerCase();
            if (archPart.contains("64") || archPart.contains("aarch_64") || archPart.contains("x86_64") || archPart.contains("arm64")) {
                arch = "64";
            } else if (archPart.contains("86") || archPart.contains("32")) {
                arch = "32";
            }
        }

        return new OsArch(os, arch);
    }

    private static void extract(Path sourceJar, Path targetDir, List<String> exclude) throws IOException {
        if (!Files.exists(sourceJar)) return;

        Files.createDirectories(targetDir);

        try (ZipFile zip = new ZipFile(sourceJar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                boolean excluded = false;
                for (String pattern : exclude) {
                    if (entryName.startsWith(pattern)) {
                        excluded = true;
                        break;
                    }
                }
                if (excluded) continue;

                Path targetFile = targetDir.resolve(entryName);
                Files.createDirectories(targetFile.getParent());

                try (InputStream in = zip.getInputStream(entry)) {
                    Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }


    private static class Manifest {
        List<Library> libraries;
    }

    private static class Library {
        String name;
        Downloads downloads;
        Map<String, String> natives;
        Extract extract;
    }

    private static class Downloads {
        Artifact artifact;
        Map<String, Classifier> classifiers;
    }

    private static class Artifact {
        String path;
    }

    private static class Classifier {
        String path;
    }

    private static class Extract {
        List<String> exclude;
    }

    private static class OsArch {
        final String os;
        final String arch;

        OsArch(String os, String arch) {
            this.os = os;
            this.arch = arch;
        }
    }

    private static class ExtractionTask {
        final Path sourceJar;
        final Path targetDir;
        final List<String> exclude;

        ExtractionTask(Path sourceJar, Path targetDir, List<String> exclude) {
            this.sourceJar = sourceJar;
            this.targetDir = targetDir;
            this.exclude = exclude;
        }
    }
}