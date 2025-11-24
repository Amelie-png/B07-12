package com.example.demoapp.models;

import java.util.*;

public class Child {
    private String name;
    private String dob;
    private String parentId;
    private String notes;
    private Map<String, Boolean> sharing;
    private Set<String> providerIds;
    private Map<String, ShareCode> shareCodes;

    // 新增 Firestore 文档 ID
    private String firestoreId;

    public Child() {
        this.providerIds = new HashSet<>();
        this.sharing = new HashMap<>();
        this.shareCodes = new HashMap<>();
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

    // Getter & Setter for Firestore ID
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

    // 增加或移除 provider
    public void addProvider(String providerId) { this.providerIds.add(providerId); }
    public void removeProvider(String providerId) {
        this.providerIds.remove(providerId);
        this.shareCodes.remove(providerId);
    }

    // 一次性分享码对象
    public static class ShareCode {
        private String code;
        private long timestamp;  // 生成时间（毫秒）
        private boolean revoked;

        public ShareCode(String code) {
            this.code = code;
            this.timestamp = System.currentTimeMillis();
            this.revoked = false;
        }

        public String getCode() { return code; }
        public long getTimestamp() { return timestamp; }
        public boolean isRevoked() { return revoked; }
        public void revoke() { this.revoked = true; }
    }

    // 生成一次性分享码（有效期7天）
    public String generateOneTimeShareCode(String providerId) {
        String code = UUID.randomUUID().toString();
        ShareCode shareCode = new ShareCode(code);
        this.shareCodes.put(providerId, shareCode);
        this.providerIds.add(providerId);
        return code;
    }

    // 撤销分享码
    public void revokeShareCode(String providerId) {
        ShareCode sc = this.shareCodes.get(providerId);
        if (sc != null) sc.revoke();
    }

    // 验证分享码是否有效
    public boolean verifyShareCode(String providerId, String code) {
        ShareCode sc = this.shareCodes.get(providerId);
        if (sc == null) return false;
        // 检查是否被撤销
        if (sc.isRevoked()) return false;
        // 检查是否过期（7天 = 7 * 24 * 60 * 60 * 1000 ms）
        long now = System.currentTimeMillis();
        if (now - sc.getTimestamp() > 7L * 24 * 60 * 60 * 1000) return false;
        return code.equals(sc.getCode());
    }

}