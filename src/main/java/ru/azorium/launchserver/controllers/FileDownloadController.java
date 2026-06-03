package ru.azorium.launchserver.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.azorium.launchserver.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ru.azorium.launchserver.LauncherApplication.rootPath;

@RestController
@RequestMapping("/download")
public class FileDownloadController {

    static final Path fileStorageLocation = rootPath.toAbsolutePath().normalize();
    static List<String> allowedDirs = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

    static {
        allowedDirs.add("profiles");
        allowedDirs.add("hashes");
        allowedDirs.add("caches");
        allowedDirs.add("updates");
    }

    @GetMapping("/client/cached")
    public ResponseEntity<byte[]> downloadCachedClient(@RequestBody String json) {
        JsonObject requestObj = JsonParser.parseString(json).getAsJsonObject();
        byte[] zipBytes = ZipUtils.removeModsFromClientArchive(requestObj);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(zipBytes.length);

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            String fullPath = request.getRequestURI().replace("/download/", "");
            if (fullPath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (fullPath.contains("..")) {
                logger.info("Недопустимый путь к файлу: {}", fullPath);
                return ResponseEntity.status(403).build();
            }

            Path targetPath = fileStorageLocation.resolve(fullPath).normalize();
            if (Files.isDirectory(targetPath)) {
                return ResponseEntity.notFound().build();
            }

            String targetDir = targetPath.toString().split(Pattern.quote(rootPath.toString()))[1].split(Pattern.quote(File.separator))[1];
            if (!targetPath.startsWith(fileStorageLocation) || !allowedDirs.contains(targetDir)) {
                logger.info("Недопустимый путь к файлу: {}", targetPath);
                return ResponseEntity.status(403).build();
            }

            Resource resource = new UrlResource(targetPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(targetPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

}
