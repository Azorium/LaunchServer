package ru.azorium.launchserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.azorium.launchserver.dto.JoinRequest;
import ru.azorium.launchserver.entity.User;
import ru.azorium.launchserver.repository.UserRepository;
import ru.azorium.launchserver.utils.JwtUtils;
import ru.azorium.launchserver.utils.Yggdrasil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/yggdrasil")
public class YggdrasilAuthController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Value("${app.skin_api_url}")
    private String skinApiUrl;

    @GetMapping
    public Map<String, Object> metadata() {
        return Map.of(
                "meta", Map.of(
                        "serverName", "AzoriumLauncher LaunchServer",
                        "implementationName", "Spring Yggdrasil",
                        "implementationVersion", "1.0"
                ),
                "skinDomains", List.of("localhost"),
                "feature.non_email_login", true
        );
    }

    @GetMapping("/sessionserver/session/minecraft/hasJoined")
    public ResponseEntity<?> hasJoined(@RequestParam String username, @RequestParam String serverId) {
        User user = userRepository
                .findByEmailOrLogin(null, username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!serverId.equals(user.getServerID())) {
            return ResponseEntity.notFound().build();
        }

        String profileId = user.getUuid().replace("-", "");
        String texturesJson = """
                {
                  "timestamp": %d,
                  "profileId": "%s",
                  "profileName": "%s",
                  "textures": {
                    "SKIN": {
                      "url": "%s"
                    }
                  }
                }
                """.formatted(
                System.currentTimeMillis(),
                profileId,
                username,
                skinApiUrl + username + ".png"
        );

        String encoded = Base64.getEncoder().encodeToString(texturesJson.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok(Map.of(
                "id", profileId,
                "name", user.getLogin(),
                "properties", List.of(
                        Map.of(
                                "name", "textures",
                                "value", encoded
                        )
                )
        ));
    }

    @PostMapping("/sessionserver/session/minecraft/join")
    public ResponseEntity<?> join(@RequestBody JoinRequest req) {
        if (!jwtUtils.validateJwtToken(req.getAccessToken())) {
            return ResponseEntity.status(403).build();
        }

        String username = jwtUtils.getUserNameFromJwtToken(req.getAccessToken());
        User user = userRepository
                .findByEmailOrLogin(null, username)
                .orElseThrow();

        user.setServerID(req.getServerId());
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessionserver/session/minecraft/profile/{profileId}")
    public ResponseEntity<?> profile(@PathVariable String profileId) {
        User user = userRepository.findByUuid(Yggdrasil.profileIdToUuid(profileId))
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String texturesJson = """
                {
                  "timestamp": %d,
                  "profileId": "%s",
                  "profileName": "%s",
                  "textures": {
                    "SKIN": {
                      "url": "%s"
                    }
                  }
                }
                """.formatted(
                System.currentTimeMillis(),
                profileId,
                user.getLogin(),
                "http://localhost:3001/uploads/skins/" + user.getLogin() + ".png"
        );

        String encoded = Base64.getEncoder().encodeToString(texturesJson.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok(
                Map.of(
                        "id", profileId,
                        "name", user.getLogin(),
                        "properties", List.of(
                                Map.of(
                                        "name", "textures",
                                        "value", encoded
                                )
                        )
                )
        );
    }

}