package com.codepipes.tingadmin.models

import com.codepipes.tingadmin.utils.Routes

class User (
    val id: Int,
    val token: String?,
    val name: String,
    val username: String,
    val email: String,
    val image: String,
    val phone: String,
    val dob: String?,
    val gender: String?,
    val country: String,
    val town: String,
    val channel: String,
    val createdAt: String,
    val updatedAt: String
){
    public fun imageURL(): String = "${Routes.UPLOAD_END_POINT}${this.image}"
}

class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val image: String
) {
    public fun imageURL(): String = "${Routes.UPLOAD_END_POINT}${this.image}"
}

class Address (
    val id: Int,
    var type: String,
    var address: String,
    var latitude: Double,
    var longitude: Double,
    val createdAt: String,
    val updatedAt: String
){}

class UserRestaurants (
    val count: Int,
    val restaurants: List<UserRestaurant>?
){}

class UserAddresses (
    val count: Int,
    val addresses: List<Address>
){}

class UserUrls (
    val loadRestaurants: String,
    val loadReservations: String,
    val apiGet: String,
    val apiGetAuth: String,
    val apiRestaurants: String,
    val apiReservations: String,
    val apiMoments: String,
    val apiOrders: String
){}

class UserRestaurantReviews (
    val count: Int,
    val reviews: List<RestaurantReview>?
){}