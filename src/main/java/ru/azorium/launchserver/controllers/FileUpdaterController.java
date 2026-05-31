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
import ru.azorium.launchserver.GlobalObjects;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.utils.update.UpdateFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.azorium.launchserver.LauncherApplication.rootPath;

@RestController
@RequestMapping("/update")
public class FileUpdaterController {
    static final Path fileStorageLocation = rootPath.toAbsolutePath().normalize();
    static List<String> allowedDirs = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(FileUpdaterController.class);

    static {
        allowedDirs.add("updates");
    }

    @PostMapping("/game_files")
    public ResponseEntity<byte[]> updateGameFiles(@RequestBody String json) {
        try {
            JsonObject objFromRequest = JsonParser.parseString(json).getAsJsonObject();
            String date = objFromRequest.get("date").getAsString();
            String updateTarget = objFromRequest.get("updateTarget").getAsString();

            byte[] zipBytes = updateTarget.equals("client") ? UpdateFiles.updateClient(objFromRequest, date) : UpdateFiles.updateAssets(objFromRequest, date);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "files-" + date + ".zip");
            headers.setContentLength(zipBytes.length);

            return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/launcher")
    public ResponseEntity<byte[]> updateLauncher(@RequestBody String json) {
        byte[] zipBytes = UpdateFiles.updateLauncher(json);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "launcher.zip");
        headers.setContentLength(zipBytes.length);

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/launcher/check")
    public ResponseEntity<Boolean> updateLauncherCheck(@RequestBody String json) {
        return ResponseEntity.ok(!LauncherApplication.launcherHashes.containsValue(json));
    }

}