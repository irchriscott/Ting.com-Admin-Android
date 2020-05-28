package com.codepipes.tingadmin.models

class RestaurantMenu (
    val id: Int,
    val type: MenuType,
    val menu: MenuAbout
){}

class RestaurantAbout (
   val name: String,
   val logo: String
){}

class MenuType (
    val id: Int,
    val name: String
){}

class MenuUrls (
    val url: String,
    val like: String,
    val loadReviews: String,
    val addReview: String,
    val apiGet: String,
    val apiLike: String,
    val apiReviews: String,
    val apiAddReview: String
){}

class Menu (
    val id: Int,
    val restaurant: Restaurant?,
    val branch: Branch?,
    val name: String,
    val category: FoodCategory?,
    val cuisine: RestaurantCategory?,
    val menu: Int,
    val type: Int,
    val dishTime: Int?,
    val foodType: Int?,
    val drinkType: Int?,
    val description: String,
    val ingredients: String,
    val showIngredients: Boolean,
    val price: Double,
    val lastPrice: Double,
    val currency: String,
    val isCountable: Boolean,
    val isAvailable: Boolean,
    val quantity: Int,
    val hasDrink: Boolean?,
    val drink: Menu?,
    val foods: MenuFoods?,
    val images: MenuImages,
    val createdAt: String,
    val updatedAt: String
){}

class MenuAbout (
    val id: Int,
    val name: String,
    val dishTime: Int?,
    val foodType: Int?,
    val drinkType: Int?,
    val price: Double,
    val currency: String,
    val isAvailable: Boolean,
    val quantity: Int,
    val images: MenuImages,
    val createdAt: String,
    val updatedAt: String
){}

class MenuPromotions (
    val count: Int,
    val todayPromotion: PromotionDataString?,
    val promotions: List<MenuPromotion>?
){}

class MenuReviews (
    val count: Int,
    val average: Float,
    val percents: List<Int>,
    val reviews: List<MenuReview>?
){}

class MenuLikes (
    val count: Int,
    val likes: List<Int>?
){}

class MenuFoods (
    val count: Int,
    val foods: List<DishFood>
){}

class DishFood(
   val id: Int,
   val food: Int,
   val menu: Int,
   val isCountable: Boolean,
   val quantity: Int,
   val createdAt: String,
   val updatedAt: String
){}

class MenuImage (
    val id: Int,
    val image: String,
    val createdAt: String
){}

class MenuImages (
    val count: Int,
    val images: List<MenuImage>
){}

