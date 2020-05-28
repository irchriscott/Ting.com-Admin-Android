package com.codepipes.tingadmin.models

class MenuPromotion (
    val id: Int,
    val restaurant: Restaurant?,
    val branch: Branch?,
    val occasionEvent: String,
    val uuid: String,
    val uuidUrl: String,
    val promotionItem: PromotionItem,
    val reduction: PromotionReduction,
    val supplement: PromotionSupplement,
    val period: PromotionPeriod,
    val description: String,
    val posterImage: String,
    val isOn: Boolean,
    val isOnToday: Boolean,
    val createdAt: String,
    val updatedAt: String
){}

class PromotionItem (
    val type: PromotionItemType,
    val category: FoodCategory?,
    val menu: RestaurantMenu?
){}

class PromotionItemType (
    val id: Int,
    val name: String
){}

class PromotionReduction (
    val hasReduction: Boolean,
    val amount: Int,
    val reductionType: String
){}

class PromotionSupplement (
    val hasSupplement: Boolean,
    val minQuantity: Int,
    val isSame: Boolean,
    val supplement: RestaurantMenu?,
    val quantity: Int
){}

class PromotionInterest (
    val id: Int,
    val user: User,
    val isInterested: Boolean,
    val createdAt: String
){}

class PromotionPeriod(
    val isSpecial: Boolean,
    val startDate: String?,
    val endDate: String?,
    val periods: List<Int>
){}

class PromotionDataString(
    val id: Int,
    val occasionEvent: String,
    val posterImage: String,
    val supplement: String?,
    val reduction: String?
) {}