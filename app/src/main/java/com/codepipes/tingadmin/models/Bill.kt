package com.codepipes.tingadmin.models

import com.codepipes.tingadmin.models.PromotionDataString
import com.codepipes.tingadmin.models.RestaurantMenu

class Bill (
    val id: Int,
    val number: String,
    val token: String,
    val amount: Double,
    val discount: Double,
    val tips: Double,
    val extrasTotal: Double,
    val total: Double,
    val currency: String,
    val isRequested: Boolean,
    val isPaid: Boolean,
    val isComplete: Boolean,
    val paidBy: Int?,
    val orders: BillOrders?,
    val extras: MutableList<BillExtra>,
    val createdAt: String,
    val updatedAt: String
) {}


class BillOrders(
    val count: Int,
    val orders: List<OrderData>?
) {}


class Order (
    val id: Int,
    val menu: RestaurantMenu,
    val user: UserData?,
    val waiter: Waiter?,
    val token: String,
    val billNumber: String,
    val tableNumber: String,
    val quantity: Int,
    val price: Double,
    val currency: String,
    val conditions: String?,
    val isAccepted: Boolean,
    val isDeclined: Boolean,
    val isDelivered: Boolean,
    val people: Int,
    val reasons: String?,
    val hasPromotion: Boolean,
    val promotion: PromotionDataString?,
    val createdAt: String,
    val updatedAt: String
) {
    val total: Double
        get() = quantity * price
}


class OrderData (
    val id: Int,
    val menu: String,
    val token: String,
    val quantity: Int,
    val price: Double,
    val currency: String,
    val conditions: String?,
    val isAccepted: Boolean,
    val isDeclined: Boolean,
    val isDelivered: Boolean,
    val reasons: String?,
    val hasPromotion: Boolean,
    val promotion: PromotionDataString?,
    val createdAt: String,
    val updatedAt: String
) {
    val total: Double
        get() = quantity * price
}

class BillExtra(
    val id: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val total: Double,
    val createdAt: String,
    val updatedAt: String
) {}