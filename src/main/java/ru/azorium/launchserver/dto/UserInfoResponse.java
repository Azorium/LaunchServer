package ru.azorium.launchserver.dto;

import jakarta.persistence.Column;
import ru.azorium.launchserver.entity.User;

public class UserInfoResponse {
    private String username;
    private String uuid;
    private String groupName;
    private String capeHash;
    private String skinHash;

    public UserInfoResponse(User user) {
        this.username = user.getLogin();
        this.uuid = user.getUuid();
        this.groupName = user.getGroupName();
        this.skinHash = user.getSkinHash();
        this.capeHash = user.getCapeHash();
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCapeHash() {
        return capeHash;
    }

    public String getSkinHash() {
        return skinHash;
    }

}
