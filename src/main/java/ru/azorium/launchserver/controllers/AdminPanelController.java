package ru.azorium.launchserver.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.azorium.launchserver.model.dto.AdminRequests;
import ru.azorium.launchserver.service.AdminControlPanelService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPanelController {
    private final AdminControlPanelService adminControlPanelService;

    public AdminPanelController(AdminControlPanelService adminControlPanelService) {
        this.adminControlPanelService = adminControlPanelService;
    }

    @PostMapping("/installclient")
    public ResponseEntity<?> installClient(Authentication authentication, @RequestBody AdminRequests.InstallClientRequest installClientRequest){
        adminControlPanelService.installClient(installClientRequest);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/sync")
    public ResponseEntity<?> sync(Authentication authentication, @RequestBody AdminRequests.SyncRequest syncRequest) {
        adminControlPanelService.sync(syncRequest);
        return ResponseEntity.ok(null);
    }


}
