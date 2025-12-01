package com.example.demoapp.models;

import java.security.SecureRandom;
import java.util.*;

public class Child {
    private String uid;
    private String firestoreId;
    private String username;
    private String firstName;
    private String lastName;
    private String dob;
    private String parentId;
    private String notes;
    private Map<String, Boolean> sharing = new HashMap<>();
    private List<String> providerIds = new ArrayList<>();
    private Map<String, ShareCode> shareCodes = new HashMap<>();
    private Map<String, String> providerBindings = new HashMap<>(); // providerId → code
    private String passwordHash;
    private boolean hasSeenOnboardingChild;
    private double pb;

    public Child() {
        this.sharing = new HashMap<>();
        this.providerIds = new ArrayList<>();
        this.shareCodes = new HashMap<>();
        this.providerBindings = new HashMap<>();
        this.hasSeenOnboardingChild = false;
        this.pb = 0.0;

        // 默认权限（与新布局对应）
        this.sharing.put("rescueLogs", false);
        this.sharing.put("controllerAdherence", false);
        this.sharing.put("symptoms", false);
        this.sharing.put("triggers", false);
        this.sharing.put("pef", false);
        this.sharing.put("triageIncidents", false);
        this.sharing.put("summaryCharts", false);
    }

    public Child(String username, String firstName, String lastName, String dob, String parentId, String notes) {
        this(); // 调用默认构造函数初始化 Map 等
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
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
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Map<String, Boolean> getSharing() { return sharing; }
    public void setSharing(Map<String, Boolean> sharing) { this.sharing = sharing; }
    public List<String> getProviderIds() { return providerIds; }
    public void setProviderIds(List<String> providerIds) { this.providerIds = providerIds; }
    public Set<String> getProviderIdsSet() { return new HashSet<>(providerIds); }
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

    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 避免易混字符
    private static final SecureRandom random = new SecureRandom();
    public String generateOneTimeShareCode(Map<String, Boolean> defaultPermissions) {
        String code = "SC-" + generateShortCode(8); // 8 位短码
        ShareCode sc = new ShareCode(code, null, defaultPermissions);
        shareCodes.put(code, sc);
        return code;
    }

    public String generateOneTimeShareCode() {
        return generateOneTimeShareCode(this.sharing);
    }

    // 生成指定长度短码
    private String generateShortCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(idx));
        }
        return sb.toString();
    }


    public void removeExpiredUnboundShareCodes() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<String, ShareCode>> iterator = shareCodes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ShareCode> entry = iterator.next();
            ShareCode sc = entry.getValue();

            boolean isBound = sc.getProviderId() != null && !sc.getProviderId().isEmpty();
            boolean isExpired = now - sc.getTimestamp() > 7L * 24 * 60 * 60 * 1000; // 7天

            if (!isBound && isExpired) {
                iterator.remove();
            }
        }
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
            if (!providerIds.contains(providerId)) {
                providerIds.add(providerId);
            }
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

        // 🔹 新增字段，用于记录 RecyclerView 是否展开权限开关
        private boolean expanded = false;

        public ShareCode() {
            this.permissions = new HashMap<>();
            // 初始化默认 7 个权限
            permissions.put("rescueLogs", false);
            permissions.put("controllerAdherence", false);
            permissions.put("symptoms", false);
            permissions.put("triggers", false);
            permissions.put("pef", false);
            permissions.put("triageIncidents", false);
            permissions.put("summaryCharts", false);
        }

        public ShareCode(String code, String providerId, Map<String, Boolean> permissions) {
            this.code = code;
            this.providerId = providerId;
            this.timestamp = System.currentTimeMillis();
            this.revoked = false;
            this.permissions = new HashMap<>(permissions);
            // 确保 7 个权限都有默认值
            this.permissions.putIfAbsent("rescueLogs", false);
            this.permissions.putIfAbsent("controllerAdherence", false);
            this.permissions.putIfAbsent("symptoms", false);
            this.permissions.putIfAbsent("triggers", false);
            this.permissions.putIfAbsent("pef", false);
            this.permissions.putIfAbsent("triageIncidents", false);
            this.permissions.putIfAbsent("summaryCharts", false);
        }

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

        // 🔹 新增 getter/setter
        public boolean isExpanded() { return expanded; }
        public void setExpanded(boolean expanded) { this.expanded = expanded; }
    }

}
