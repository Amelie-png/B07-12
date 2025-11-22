package com.example.demoapp.models

data class Child(
    val parentId: String = "",
    val name: String = "",
    val dob: String = "",
    val sharing: Map<String, Boolean> = mapOf(
        "symptoms" to true,
        "medicines" to true,
        "pef" to true,
        "triage" to true
    )
)
