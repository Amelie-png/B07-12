package com.example.demoapp.models;

import java.util.*;

public class Child {
    private String uid;                       // 唯一ID
    private String firestoreId;                // Firestore文档ID
    private String name;
    private String dob;
    private String parentId;
    private String notes;
    private Map<String, Boolean> sharing;     // 总体权限字段
    private Set<String> providerIds;          // 已绑定的 Provider
    private Map<String, ShareCode> shareCodes; // key: 一次性 code 或 providerId
    private String passwordHash;
    private boolean hasSeenOnboardingChild;   // 是否看过onboarding

    public Child() {
        this.uid = UUID.randomUUID().toString();
        this.sharing = new HashMap<>();
        this.providerIds = new HashSet<>();
        this.shareCodes = new HashMap<>();
        this.hasSeenOnboardingChild = false;
    }

    public Child(String name, String dob, String parentId, String notes) {
        this();
        this.name = name;
        this.dob = dob;
        this.parentId = parentId;
        this.notes = notes;
        this.sharing.put("symptoms", false);
        this.sharing.put("medicines", false);
        this.sharing.put("pef", false);
        this.sharing.put("triage", false);
    }

    // ------------------------
    // Getters / Setters
    // ------------------------
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isHasSeenOnboardingChild() { return hasSeenOnboardingChild; }
    public void setHasSeenOnboardingChild(boolean hasSeenOnboardingChild) { this.hasSeenOnboardingChild = hasSeenOnboardingChild; }

    // ------------------------
    // ShareCode 操作
    // ------------------------
    public String generateOneTimeShareCode() {
        String code = UUID.randomUUID().toString();
        ShareCode shareCode = new ShareCode(code);
        this.shareCodes.put(code, shareCode);
        return code;
    }

    public boolean bindProvider(String code, String providerId) {
        ShareCode sc = this.shareCodes.get(code);
        if (sc != null && !sc.isRevoked() &&
                System.currentTimeMillis() - sc.getTimestamp() <= 7L * 24 * 60 * 60 * 1000) {
            this.providerIds.add(providerId);
            this.shareCodes.put(providerId, sc);
            this.shareCodes.remove(code);
            return true;
        }
        return false;
    }

    public void revokeProvider(String providerId) {
        ShareCode sc = this.shareCodes.get(providerId);
        if (sc != null) sc.revoke();
    }

    public void updateProviderPermissions(String providerId, Map<String, Boolean> newPermissions) {
        ShareCode sc = this.shareCodes.get(providerId);
        if (sc != null && !sc.isRevoked()) {
            sc.setPermissions(newPermissions);
        }
    }

    public boolean verifyShareCode(String providerId, String code) {
        ShareCode sc = this.shareCodes.get(providerId);
        if (sc == null || sc.isRevoked()) return false;
        return System.currentTimeMillis() - sc.getTimestamp() <= 7L * 24 * 60 * 60 * 1000
                && code.equals(sc.getCode());
    }

    // ------------------------
    // ShareCode 类
    // ------------------------
    public static class ShareCode {
        private String code;
        private long timestamp;
        private boolean revoked;
        private Map<String, Boolean> permissions;

        public ShareCode() {}

        public ShareCode(String code) {
            this.code = code;
            this.timestamp = System.currentTimeMillis();
            this.revoked = false;
            this.permissions = new HashMap<>();
        }

        public String getCode() { return code; }
        public long getTimestamp() { return timestamp; }
        public boolean isRevoked() { return revoked; }
        public void revoke() { this.revoked = true; }
        public Map<String, Boolean> getPermissions() { return permissions; }
        public void setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; }
    }
}
