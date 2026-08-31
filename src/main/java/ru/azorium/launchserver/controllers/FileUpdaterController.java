package ru.azorium.launchserver.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.azorium.launchserver.AppConstants;
import ru.azorium.launchserver.LauncherApplication;
import ru.azorium.launchserver.config.AppHashes;
import ru.azorium.launchserver.config.AppPaths;
import ru.azorium.launchserver.utils.hash.Md5;
import ru.azorium.launchserver.utils.update.UpdateFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/update")
public class FileUpdaterController {
    private final AppPaths appPaths;
    private final AppHashes appHashes;
    private final List<String> allowedDirs = new ArrayList<>();
    private final UpdateFiles updateFiles;

    public FileUpdaterController(AppPaths appPaths, AppHashes appHashes, UpdateFiles updateFiles) {
        this.appPaths = appPaths;
        this.appHashes = appHashes;
        this.updateFiles = updateFiles;
        allowedDirs.add("updates");
    }

    @PostMapping("/game_files")
    public ResponseEntity<byte[]> updateGameFiles(@RequestBody String json) {
        try {
            JsonObject objFromRequest = JsonParser.parseString(json).getAsJsonObject();
            String date = objFromRequest.get("date").getAsString();
            String updateTarget = objFromRequest.get("updateTarget").getAsString();

            byte[] zipBytes = updateTarget.equals("client") ? updateFiles.updateClient(objFromRequest, date) : updateFiles.updateAssets(objFromRequest, date);

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
        byte[] zipBytes = updateFiles.updateLauncher(json);

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

        boolean runtimeZipIsNeeded = !appHashes.getRuntimeZipHash().equals(runtimeZipHash);
        boolean launcherFileIsNeeded = !appHashes.getLauncherHashes().containsValue(launcherFileHash);

        if (checkType.equals("files")) {
            JsonObject filesCheckObj = new JsonObject();
            filesCheckObj.addProperty("runtimeZipIsNeeded", runtimeZipIsNeeded);
            filesCheckObj.addProperty("launcherFileIsNeeded", launcherFileIsNeeded);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(AppConstants.gson.toJson(filesCheckObj), headers, HttpStatus.OK);
        }

        return ResponseEntity.ok(runtimeZipIsNeeded || launcherFileIsNeeded);
    }

    @PostMapping("/launcherguard")
    public ResponseEntity<Resource> updateLauncherGuard(@RequestBody String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            String fileExtension = jsonObject.get("fileExtension").getAsString();
            String reqFileHash = jsonObject.get("hash").getAsString();

            Path guardPath = appPaths.getLauncherComponentsPath().resolve("LauncherGuard" + fileExtension);
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

            Path guardPath = appPaths.getLauncherComponentsPath().resolve("authlib-injector.jar");
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