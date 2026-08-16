package ru.azorium.launchserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 16)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(unique = true, nullable = false)
    private String password;

    @Column()
    private String serverID;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "is_slim_skin", nullable = false)
    private boolean isSlimSkin;

    @Column(name = "skin_hash", nullable = false)
    private String skinHash;

    @Column(name = "cape_hash")
    private String capeHash;

    public User(String username, String email, String uuid, String password, String serverID, String groupName, boolean isSlimSkin, String skinHash, String capeHash) {
        this.username = username;
        this.email = email;
        this.uuid = uuid;
        this.password = password;
        this.serverID = serverID;
        this.groupName = groupName;
        this.isSlimSkin = isSlimSkin;
        this.skinHash = skinHash;
        this.capeHash = capeHash;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSlimSkin() {
        return isSlimSkin;
    }

    public void setSlimSkin(boolean slimSkin) {
        isSlimSkin = slimSkin;
    }

    public String getSkinHash() {
        return skinHash;
    }

    public void setSkinHash(String skinHash) {
        this.skinHash = skinHash;
    }

    public String getCapeHash() {
        return capeHash;
    }

    public void setCapeHash(String capeHash) {
        this.capeHash = capeHash;
    }
}
