package com.codepipes.tingadmin.models

class Booking (
    val id: Int,
    val user: User,
    val table: RestaurantTable?,
    val token: String,
    val people: Int,
    val date: String,
    val time: String,
    val location: Int,
    val status: Int,
    val createdAt: String,
    val updatedAt: String
){}