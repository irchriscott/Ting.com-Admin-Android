package com.codepipes.tingadmin.utils


class Routes {

    companion object {

        public const val HOST_END_POINT: String         = "http://172.20.10.9:8000"
        private const val END_POINT: String             = "${HOST_END_POINT}/api/v1/"
        public const val UPLOAD_END_POINT: String       = "${HOST_END_POINT}/tinguploads/"
        public const val API_HOST_PREFIX: String        = "/api/v1/"

        //SIGN UP & AUTH ROUTES
        const val submitGoogleSignUp: String            = "${END_POINT}adm/signup/google/"
        const val authLoginUser: String                 = "${END_POINT}adm/auth/login/"
        const val authResetPassword: String             = "${END_POINT}adm/auth/password/reset/"
        const val authGetSession: String                = "${END_POINT}adm/auth/session/"

        //RESTAURANT
        const val updateRestaurantProfile: String       = "${END_POINT}adm/restaurant/update/profile/"
        const val updateRestaurantLogo: String          = "${END_POINT}adm/restaurant/update/logo/"
        const val updateRestaurantCategories: String    = "${END_POINT}adm/restaurant/update/categories/"
        const val updateBranchProfile: String           = "${END_POINT}adm/restaurant/update/branch/profile/"
        const val updateRestaurantConfig: String        = "${END_POINT}adm/restaurant/update/config/"

        //GLOBALS
        const val permissionsAll: String                = "${END_POINT}adm/g/permissions/all/"
        const val restaurantCategoriesAll: String       = "${END_POINT}adm/g/categories/all/"

        //ADMINISTRATORS
        const val administratorsAll: String             = "${END_POINT}adm/administrators/all/"
        const val administratorsWaiter: String          = "${END_POINT}adm/administrators/waiters/"
        const val updateAdminProfile: String            = "${END_POINT}adm/admin/profile/update/profile/"
        const val updateAdminImage: String              = "${END_POINT}adm/admin/profile/update/image/"
        const val addNewAdmin: String                   = "${END_POINT}adm/administrators/add/"
        const val updateAdminPassword: String           = "${END_POINT}adm/admin/security/update/password/"
        const val disableAdministratorToggle: String    = "${END_POINT}adm/admin/profile/disable/toggle/"
        const val updateAdminPermissions: String        = "${END_POINT}adm/admin/permissions/update/"

        //BRANCHES
        const val branchesAll: String                   = "${END_POINT}adm/branches/all/"
        const val addNewBranch: String                  = "${END_POINT}adm/branches/add/"
        const val availBranchToggle: String             = "${END_POINT}adm/branches/avail/toggle/"
        const val updateBranch: String                  = "${END_POINT}adm/branches/update/"

        //CATEGORIES
        const val categoriesAll: String                 = "${END_POINT}adm/categories/all/"
        const val deleteCategory: String                = "${END_POINT}adm/categories/delete/"
        const val updateCategory: String                = "${END_POINT}adm/categories/update/"
        const val addNewCategory: String                = "${END_POINT}adm/categories/add/new/"

        //TABLES
        const val tablesAll: String                     = "${END_POINT}adm/tables/all/"
        const val addNewTable: String                   = "${END_POINT}adm/tables/add/"
        const val availTableToggle: String              = "${END_POINT}adm/tables/avail/toggle/"
        const val updateTable: String                   = "${END_POINT}adm/tables/update/"
        const val assignWaiterTable: String             = "${END_POINT}adm/tables/waiter/assign/"
        const val removeWaiterTable: String             = "${END_POINT}adm/tables/waiter/remove/"

        //RESERVATION
        const val reservationsDate: String              = "${END_POINT}adm/reservations/date/"
        const val reservationsNew: String               = "${END_POINT}adm/reservations/new/"
        const val reservationAccept: String             = "${END_POINT}adm/reservations/accept/"
        const val reservationDecline: String            = "${END_POINT}adm/reservations/decline/"

        // MENU ALL
        const val menusAll: String                      = "${END_POINT}adm/menus/all/"

        //MENU FOOD
        const val menusFoodAll: String                  = "${END_POINT}adm/menu/food/all/"
        const val menusFoodAddNew: String               = "${END_POINT}adm/menu/food/add/new/"
        const val menusFoodAvailToggle: String          = "${END_POINT}adm/menu/food/avail/toggle/"
        const val menusFoodMoveToType: String           = "${END_POINT}adm/menu/food/move/type/"
        const val menusFoodMoveToCategory: String       = "${END_POINT}adm/menu/food/move/category/"
        const val menusFoodMoveToCuisine: String        = "${END_POINT}adm/menu/food/move/cuisine/"
        const val menusFoodUpdate: String               = "${END_POINT}adm/menu/food/update/"
        const val menusFoodDelete: String               = "${END_POINT}adm/menu/food/delete/"

        //MENU DRINK
        const val menusDrinkAll: String                 = "${END_POINT}adm/menu/drink/all/"
        const val menusDrinkAddNew: String              = "${END_POINT}adm/menu/drink/add/new/"
        const val menusDrinkAvailToggle: String         = "${END_POINT}adm/menu/drink/avail/toggle/"
        const val menusDrinkMoveToType: String          = "${END_POINT}adm/menu/drink/move/type/"
        const val menusDrinkUpdate: String              = "${END_POINT}adm/menu/drink/update/"
        const val menusDrinkDelete: String              = "${END_POINT}adm/menu/drink/delete/"

        //MENU DISH
        const val menusDishAll: String                  = "${END_POINT}adm/menu/dish/all/"
        const val menusDishAddNew: String               = "${END_POINT}adm/menu/dish/add/new/"
        const val menusDishAvailToggle: String          = "${END_POINT}adm/menu/dish/avail/toggle/"
        const val menusDishMoveToType: String           = "${END_POINT}adm/menu/dish/move/type/"
        const val menusDishMoveToCategory: String       = "${END_POINT}adm/menu/dish/move/category/"
        const val menusDishMoveToCuisine: String        = "${END_POINT}adm/menu/dish/move/cuisine/"
        const val menusDishUpdate: String               = "${END_POINT}adm/menu/dish/update/"
        const val menusDishDelete: String               = "${END_POINT}adm/menu/dish/delete/"
        const val menusDishFoodUpdate: String           = "${END_POINT}adm/menu/dish/food/%d/update/"

        //PROMOTIONS
        const val promotionsAll: String                 = "${END_POINT}adm/promotions/all/"
        const val promotionAdd: String                  = "${END_POINT}adm/promotions/add/"
        const val promotionUpdate: String               = "${END_POINT}adm/promotions/update/%d/"
        const val promotionDelete: String               = "${END_POINT}adm/promotions/delete/%d/"
        const val promotionAvailToggle: String          = "${END_POINT}adm/promotions/avail/toggle/%d/"

        //PLACEMENTS & ORDERS
        const val placementsAll: String                 = "${END_POINT}adm/placements/all/"
        const val placementGet: String                  = "${END_POINT}adm/placements/%s/get/"
        const val placementEnd: String                  = "${END_POINT}adm/placements/%s/done/"
        const val placementAssignWaiter: String         = "${END_POINT}adm/placements/%s/assign/waiter/%d/"
        const val ordersPlacementGet: String            = "${END_POINT}adm/orders/placement/%s/all/"
        const val ordersAccept: String                  = "${END_POINT}adm/orders/%d/accept/"
        const val ordersDecline: String                 = "${END_POINT}adm/orders/%d/decline/"
        const val ordersAddExtra: String                = "${END_POINT}adm/orders/extras/%d/add/"
        const val ordersDeleteExtra: String             = "${END_POINT}adm/orders/extras/%d/delete/"
    }
}