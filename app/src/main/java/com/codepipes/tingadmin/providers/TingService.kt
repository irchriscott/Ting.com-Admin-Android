package com.codepipes.tingadmin.providers


import com.codepipes.tingadmin.models.*
import com.codepipes.tingadmin.utils.Routes
import io.reactivex.Observable
import retrofit2.http.*

public interface TingService {

    @GET("api/v1/adm/menu/food/update/{food}/image/delete/{image}/")
    public fun deleteFoodImage(
        @Path("food") food: Int,
        @Path("image") image: Int
    ) : Observable<ServerResponse>

    @GET("api/v1/adm/menu/drink/update/{drink}/image/delete/{image}/")
    public fun deleteDrinkImage(
        @Path("drink") drink: Int,
        @Path("image") image: Int
    ) : Observable<ServerResponse>

    @GET("api/v1/adm/menu/dish/update/{dish}/image/delete/{image}/")
    public fun deleteDishImage(
        @Path("dish") dish: Int,
        @Path("image") image: Int
    ) : Observable<ServerResponse>
}