package com.codepipes.tingadmin.providers


import com.codepipes.tingadmin.models.*
import com.codepipes.tingadmin.utils.Routes
import io.reactivex.Observable
import retrofit2.http.*

public interface TingService {

    @GET("api/v1/usr/g/restaurants/get/{branch}/menus/top/")
    public fun getRestaurantTopMenus(
        @Path("branch") branch: Int
    ) : Observable<MutableList<RestaurantMenu>>
}