package ru.azorium.launchserver.dto;

import jakarta.persistence.Column;
import ru.azorium.launchserver.entity.User;

public class UserInfoResponse {
    private String username;
    private String uuid;
    private String groupName;

    public UserInfoResponse(User user) {
        this.username = user.getLogin();
        this.uuid= user.getUuid();
        this.groupName = user.getGroupName();
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

}
