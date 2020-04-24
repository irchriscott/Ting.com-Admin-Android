package com.codepipes.tingadmin.models

class Administrator (
    val id: Int,
    val branch: Branch,
    val token: String,
    val name: String,
    val username: String,
    val type: String,
    val email: String,
    val phone: String,
    val image: String,
    val badgeNumber: String,
    val isDisabled: Boolean,
    val channel: String,
    val permissions: List<String>,
    val createdAt: String,
    val updatedAt: String
){}
