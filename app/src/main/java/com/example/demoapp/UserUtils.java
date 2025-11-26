package com.example.demoapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserUtils {
    // 获取 UID（测试阶段使用已有 Firebase 文档 ID）
    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid(); // 已登录用户
        }
        // 测试阶段使用 Firebase 里已有 ID
        return "49ESOyBKI5PcfGN33PxR";
    }
}
