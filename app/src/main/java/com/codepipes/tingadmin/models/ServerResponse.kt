package com.codepipes.tingadmin.models

class ServerResponse (
    val type: String,
    val message: String,
    val status: Int,
    val redirect: String?,
    val user: Administrator?,
    val msgs: List<Any>?
){}

class MapPin(
    val id: Int,
    val pin: String
){}