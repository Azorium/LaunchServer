package ru.azorium.launchserver.dto;

import ru.azorium.launchserver.entity.User;

public class UserInfoResponse {
    private final String username;
    private final String uuid;
    private final String groupName;
    private final String capeHash;
    private final String skinHash;

    public UserInfoResponse(User user) {
        this.username = user.getUsername();
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
