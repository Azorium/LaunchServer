package ru.azorium.launchserver.model.dto;

public class AdminRequests {
    public static class InstallClientRequest{
        private String gameVersion;
        private String folderName;
        private String loaderVersion;
        private String loaderType;
        private int emitterKey;

        public InstallClientRequest(String gameVersion, String folderName, String loaderVersion, String loaderType, int emitterKey) {
            this.gameVersion = gameVersion;
            this.folderName = folderName;
            this.loaderVersion = loaderVersion;
            this.loaderType = loaderType;
            this.emitterKey = emitterKey;
        }

        public String getGameVersion() {
            return gameVersion;
        }

        public void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public String getFolderName() {
            return folderName;
        }

        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }

        public String getLoaderVersion() {
            return loaderVersion;
        }

        public void setLoaderVersion(String loaderVersion) {
            this.loaderVersion = loaderVersion;
        }

        public String getLoaderType() {
            return loaderType;
        }

        public void setLoaderType(String loaderType) {
            this.loaderType = loaderType;
        }

        public int getEmitterKey() {
            return emitterKey;
        }

        public void setEmitterKey(int emitterKey) {
            this.emitterKey = emitterKey;
        }
    }

    public static class SyncRequest{
       private String action;
       private String value;
       private int emitterKey;

        public SyncRequest(String action, String value, int emitterKey) {
            this.action = action;
            this.value = value;
            this.emitterKey = emitterKey;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getEmitterKey() {
            return emitterKey;
        }

        public void setEmitterKey(int emitterKey) {
            this.emitterKey = emitterKey;
        }
    }
}

