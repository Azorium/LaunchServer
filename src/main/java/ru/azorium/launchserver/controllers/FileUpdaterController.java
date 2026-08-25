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
import ru.azorium.launchserver.utils.hash.Md5;
import ru.azorium.launchserver.utils.update.UpdateFiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ru.azorium.launchserver.LauncherApplication.rootPath;

@RestController
@RequestMapping("/update")
public class FileUpdaterController {
    static List<String> allowedDirs = new ArrayList<>();
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
    public ResponseEntity<Object> updateLauncherCheckNeed(@RequestParam String checkType, @RequestBody String json) {
        JsonObject reqObj = JsonParser.parseString(json).getAsJsonObject();
        String runtimeZipHash = reqObj.get("runtimeZipHash").getAsString();
        String launcherFileHash = reqObj.get("launcherFileHash").getAsString();

        boolean runtimeZipIsNeeded = !LauncherApplication.runtimeZipHash.equals(runtimeZipHash);
        boolean launcherFileIsNeeded = !LauncherApplication.launcherHashes.containsValue(launcherFileHash);

        if (checkType.equals("files")) {
            JsonObject filesCheckObj = new JsonObject();
            filesCheckObj.addProperty("runtimeZipIsNeeded", runtimeZipIsNeeded);
            filesCheckObj.addProperty("launcherFileIsNeeded", launcherFileIsNeeded);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(GlobalObjects.gson.toJson(filesCheckObj), headers, HttpStatus.OK);
        }

        return ResponseEntity.ok(runtimeZipIsNeeded || launcherFileIsNeeded);
    }

    @PostMapping("/launcherguard")
    public ResponseEntity<Resource> updateLauncherGuard(@RequestBody String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String fileExtension = jsonObject.get("fileExtension").getAsString();
            String reqFileHash = jsonObject.get("hash").getAsString();

            Path guardPath = LauncherApplication.launcherComponentsPath.resolve("LauncherGuard" + fileExtension);
            String serverFileHash = Md5.getFileMd5(guardPath);

            if (serverFileHash.equals(reqFileHash)) {
                return ResponseEntity.status(403).build();
            }

            Resource resource = new UrlResource(guardPath.toUri());
            String contentType = Files.probeContentType(guardPath);
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

    @PostMapping("/authlib")
    public ResponseEntity<Resource> updateAuthlib(@RequestBody String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String reqFileHash = jsonObject.get("hash").getAsString();

            Path guardPath = LauncherApplication.launcherComponentsPath.resolve("authlib-injector.jar");
            String serverFileHash = Md5.getFileMd5(guardPath);

            if (serverFileHash.equals(reqFileHash)) {
                return ResponseEntity.status(403).build();
            }

            Resource resource = new UrlResource(guardPath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

}