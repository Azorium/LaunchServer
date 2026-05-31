package ru.azorium.launchserver.dto;

public class JoinRequest {

    private String accessToken;
    private String selectedProfile;
    private String serverId;

    public void JoinRequest(String accessToken, String selectedProfile, String serverId){
        this.accessToken = accessToken;
        this.selectedProfile = selectedProfile;
        this.serverId = serverId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
