package com.codepipes.tingadmin.models

class RestaurantTable (
    val id: Int,
    val waiter: Waiter?,
    val uuid: String,
    val maxPeople: Int,
    val number: String,
    val location: Int,
    val chairType: Int,
    val description: String,
    val isAvailable: Boolean,
    val createdAt: String,
    val updatedAt: String
){}