package com.codepipes.tingadmin.models

import com.codepipes.tingadmin.models.Administrator
import com.codepipes.tingadmin.models.Bill
import com.codepipes.tingadmin.models.Booking

class Placement (
    val id: Int,
    val user: User,
    val table: RestaurantTable,
    val booking: Booking?,
    val waiter: Waiter?,
    val bill: Bill?,
    val token: String,
    val billNumber: String?,
    val people: Int,
    val isDone: Boolean,
    val needSomeone: Boolean,
    val createdAt: String,
    val updatedAt: String
) {}