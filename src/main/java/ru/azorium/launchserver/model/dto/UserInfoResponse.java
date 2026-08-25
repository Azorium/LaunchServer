package ru.azorium.launchserver.model.dto;

import ru.azorium.launchserver.model.entity.User;

public class UserInfoResponse {
    private final String username;
    private final String uuid;
    private final String capeHash;
    private final String skinHash;
    private final String role;

    public UserInfoResponse(User user) {
        this.username = user.getUsername();
        this.uuid = user.getUuid();
        this.skinHash = user.getSkinHash();
        this.capeHash = user.getCapeHash();
        this.role = user.getRole().name();
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCapeHash() {
        return capeHash;
    }

    public String getSkinHash() {
        return skinHash;
    }

    public String getRole() {
        return role;
    }
}
