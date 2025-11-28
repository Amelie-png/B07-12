package com.example.demoapp.models;

import java.util.*;

public class Child {
    private String uid;
    private String firestoreId;
    private String username;
    private String dob;
    private String parentId;
    private String notes;
    private Map<String, Boolean> sharing = new HashMap<>();
    private Set<String> providerIds = new HashSet<>();
    private Map<String, ShareCode> shareCodes = new HashMap<>();
    private Map<String, String> providerBindings = new HashMap<>(); // providerId → code
    private String passwordHash;
    private boolean hasSeenOnboardingChild;
    private double pb;

    public Child() {
        this.sharing = new HashMap<>();
        this.providerIds = new HashSet<>();
        this.shareCodes = new HashMap<>();
        this.providerBindings = new HashMap<>();
        this.hasSeenOnboardingChild = false;
        this.pb = 0.0;

        // 默认权限
        this.sharing.put("symptoms", false);
        this.sharing.put("medicines", false);
        this.sharing.put("pef", false);
        this.sharing.put("triage", false);
    }

    public Child(String username, String dob, String parentId, String notes) {
        this();
        this.username = username;
        this.dob = dob;
        this.parentId = parentId;
        this.notes = notes;
    }

    // ------------------------
    // Getters / Setters
    // ------------------------
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Map<String, Boolean> getSharing() { return sharing; }
    public void setSharing(Map<String, Boolean> sharing) { this.sharing = sharing; }
    public Set<String> getProviderIds() { return providerIds; }
    public void setProviderIds(Set<String> providerIds) { this.providerIds = providerIds; }
    public Map<String, ShareCode> getShareCodes() { return shareCodes; }
    public void setShareCodes(Map<String, ShareCode> shareCodes) { this.shareCodes = shareCodes; }
    public Map<String, String> getProviderBindings() { return providerBindings; }
    public void setProviderBindings(Map<String, String> providerBindings) { this.providerBindings = providerBindings; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isHasSeenOnboardingChild() { return hasSeenOnboardingChild; }
    public void setHasSeenOnboardingChild(boolean hasSeenOnboardingChild) { this.hasSeenOnboardingChild = hasSeenOnboardingChild; }
    public double getPb() { return pb; }
    public void setPb(double pb) { this.pb = pb; }

    // ------------------------
    // ShareCode 操作
    // ------------------------

    public String generateOneTimeShareCode(Map<String, Boolean> defaultPermissions) {
        String code = "SC-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        ShareCode sc = new ShareCode(code, null, defaultPermissions);
        shareCodes.put(code, sc);
        return code;
    }

    public String generateOneTimeShareCode() {
        return generateOneTimeShareCode(this.sharing);
    }

    public void updateOneTimeShareCodePermissions(String code, Map<String, Boolean> perms) {
        ShareCode sc = shareCodes.get(code);
        if (sc != null && sc.getProviderId() == null && !sc.isRevoked()) {
            sc.setPermissions(perms);
        }
    }

    public boolean bindProviderWithShareCode(String providerId, String code) {
        ShareCode sc = shareCodes.get(code);
        if (sc != null && !sc.isRevoked() && sc.getProviderId() == null &&
                System.currentTimeMillis() - sc.getTimestamp() <= 7L * 24 * 60 * 60 * 1000) {
            sc.setProviderId(providerId);
            providerIds.add(providerId);
            providerBindings.put(providerId, code);
            return true;
        }
        return false;
    }

    public boolean verifyShareCode(String providerId, String code) {
        ShareCode sc = shareCodes.get(code);
        return sc != null &&
                !sc.isRevoked() &&
                providerId.equals(sc.getProviderId()) &&
                System.currentTimeMillis() - sc.getTimestamp() <= 7L * 24 * 60 * 60 * 1000;
    }

    public void revokeProvider(String providerId) {
        for (ShareCode sc : shareCodes.values()) {
            if (providerId.equals(sc.getProviderId())) sc.revoke();
        }
        providerBindings.remove(providerId);
        providerIds.remove(providerId);
    }

    public void updateProviderPermissions(String providerId, Map<String, Boolean> newPermissions) {
        for (ShareCode sc : shareCodes.values()) {
            if (providerId.equals(sc.getProviderId()) && !sc.isRevoked()) {
                sc.setPermissions(newPermissions);
            }
        }
    }

    // ------------------------
    // ShareCode 类
    // ------------------------
    public static class ShareCode {
        private String code;
        private long timestamp;
        private boolean revoked;
        private Map<String, Boolean> permissions;
        private String providerId;

        public ShareCode() {}

        public ShareCode(String code, String providerId, Map<String, Boolean> permissions) {
            this.code = code;
            this.providerId = providerId;
            this.timestamp = System.currentTimeMillis();
            this.revoked = false;
            this.permissions = new HashMap<>(permissions);
        }

        // ✅ 完整 Getter / Setter
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public boolean isRevoked() { return revoked; }
        public void setRevoked(boolean revoked) { this.revoked = revoked; }
        public void revoke() { this.revoked = true; }
        public Map<String, Boolean> getPermissions() { return permissions; }
        public void setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; }
        public String getProviderId() { return providerId; }
        public void setProviderId(String providerId) { this.providerId = providerId; }
    }
}
