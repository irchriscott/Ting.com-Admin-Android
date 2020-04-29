package com.codepipes.tingadmin.utils

import com.codepipes.tingadmin.models.BranchSpecial

class Constants {

    public val genders      = arrayOf("Male", "Female")
    public val addressType  = arrayOf("Home", "Work", "School", "Other")

    public val toastDefaultImage        = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAflBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACCtoPsAAAAKXRSTlMA6PsIvDob+OapavVhWRYPrIry2MxGQ97czsOzpJaMcE0qJQOwVtKjfxCVFeIAAAI3SURBVFjDlJPZsoIwEETnCiGyb8q+qmjl/3/wFmGKwjBROS9QWbtnOqDDGPq4MdMkSc0m7gcDDhF4NRdv8NoL4EcMpzoJglPl/KTDz4WW3IdvXEvxkfIKn7BMZb1bFK4yZFqghZ03jk0nG8N5NBwzx9xU5cxAg8fXi20/hDdC316lcA8o7t16eRuQvW1XGd2d2P8QSHQDDbdIII/9CR3lUF+lbucfJy4WfMS64EJPORnrZxtfc2pjJdnbuags3l04TTtJMXrdTph4Pyg4XAjugAJqMDf5Rf+oXx2/qi4u6nipakIi7CsgiuMSEF9IGKg8heQJKkxIfFSUU/egWSwNrS1fPDtLfon8sZOcYUQml1Qv9a3kfwsEUyJEMgFBKzdV8o3Iw9yAjg1jdLQCV4qbd3no8yD2GugaC3oMbF0NYHCpJYSDhNI5N2DAWB4F4z9Aj/04Cna/x7eVAQ17vRjQZPh+G/kddYv0h49yY4NWNDWMMOMUIRYvlTECmrN8pUAjo5RCMn8KoPmbJ/+Appgnk//Sy90GYBCGgm7IAskQ7D9hFKW4ApB1ei3FSYD9PjGAKygAV+ARFYBH5BsVgG9kkBSAQWKUFYBRZpkUgGVinRWAdUZQDABBQdIcAElDVBUAUUXWHQBZx1gMAGMprM0AsLbVXHsA5trZe93/wp3svQ0YNb/jWV3AIOLsMtlznSNOH7JqjOpDVh7z8qCZR10ftvO4nxeOvPLkpSuvfXnxzKtvXr7j+v8C5ii0e71At7cAAAAASUVORK5CYII="
    public val toastSuccessImage        = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABABAMAAABYR2ztAAAAIVBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABt0UjBAAAACnRSTlMApAPhIFn82wgGv8mVtwAAAKVJREFUSMft0LEJAkEARNFFFEw1NFJb8CKjAy1AEOzAxNw+bEEEg6nyFjbY4LOzcBwX7S/gwUxoTdIn+Jbv4Lv8bx446+kB6VsBtK0B+wbMCKxrwL33wOrVeeChX28n7KTOTjgoEu6DRSYAgAAAAkAmAIAAAAIACQIkMkACAAgAIACAyECBKAOJuCagTJwSUCaUAEMAABEBRwAAEQFLbCJgO4bW+AZKGnktR+jAFAAAAABJRU5ErkJggg=="
    public val toastWarningImage        = "iVBORw0KGgoAAAANSUhEUgAAAEQAAABECAMAAAAPzWOAAAAAkFBMVEUAAAAAAAABAAIAAAABAAIAAAMAAAABAAIBAAIBAAIAAAIAAAABAAIAAAABAAICAAICAAIAAAIAAAAAAAAAAAABAAIBAAIAAAMAAAABAAIBAAMBAAECAAIAAAIAAAIAAAABAAIBAAIBAAMBAAIBAAEAAAIAAAMAAAAAAAABAAECAAICAAIAAAIAAAMAAAQAAAE05yNAAAAAL3RSTlMAB+kD7V8Q+PXicwv7I9iYhkAzJxnx01IV5cmnk2xmHfzexsK4eEw5L7Gei39aRw640awAAAHQSURBVFjD7ZfJdoJAEEWJgCiI4oDiPM8m7///LidErRO7sHrY5u7YXLr7vKqu9kTC0HPmo9n8cJbEQOzqqAdAUHeUZACQuTkGDQBoDJwkHZR0XBz9FkpafXuHP0SJ09mGeJLZ5wwlTmcbA0THPmdEK7XPGTG1zxmInn3OiJ19zkB0jSVTKExMHT0wjAwlWzC0fSPHF1gWRpIhWMYm7fYTFcQGlbemf4dFfdTGg0B/KXM8qBU/3wntbq7rSGqvJ9kla6IpueFJet8fxfem5yhykjyOgNaWF1qSGd5JMNNxpNF7SZQaVh5JzLrTCZIEJ1GyEyVyd+pClMjdaSJK5O40giSRu5PfFiVyd1pAksjdKRnrSsbVdbiHrgT7yss315fkVQPLFQrL+4FHeOXKO5YRFEKv5AiFaMlKLlBpJuVCJlC5sJfvCgztru/3NmBYccPgGTxRAzxn1XGEMUf58pXZvjoOsOCgjL08+b53mtfAM/SVsZcjKLtysQZPqIy9HPP3m/3zKItRwT0LyQo8sTr26tcO83DIUMWIJjierHLsJda/tbNBFY0BP/bKtcM8HNIWCK3aYR4OMzgxo5w5EFLOLKDExXAm9gI4E3iAO94/Ct/lKWuM2LMGbgAAAABJRU5ErkJggg=="
    public val toastErrorImage          = "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAeFBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVyEiIAAAAJ3RSTlMA3BsB98QV8uSyWVUFz7+kcWMM2LuZioBpTUVBNcq2qaibj4d1azLZZYABAAACZElEQVRYw7WX25KCMAyGAxUoFDkpiohnV97/DXeGBtoOUprZ2dyo1K82fxKbwJJVp+KQZ7so2mX5oThVQLKwjDe9YZu4DF3ptAn6rxY0qQPOEq9fNC9ha3y77a22ba24v+9Xbe8v8x03dPOC2/NdvB6xeSreLfGJpnx0TyotKqLm2s7Jd/WO6ivXNp0tCy02R/aFz5VQ5wUPlUL5fIfj5KIlVGU0nWHm/5QtoTVMWY8mzIVu1K9O7XH2JiU/xnOOT39gnUfj+lFHddx4tFjL3/H8jjzaFCy2Rf0c/fdQyQszI8BDR973IyMSKa4krjxAiW/lkRvMP+bKK9WbYS1ASQg8dKjaUGlYPwRe/WoIkz8tiQchH5QAEMv6T0k8MD4mUyWr4E7jAWqZ+xWcMIYkXvlwggJ3IvFK+wIOcpXAo8n8P0COAaXyKH4OsjBuZB4ew0IGu+H1SebhNazsQBbWm8yj+hFuUJB5eMsN0IUXmYendAFFfJB5uEkRMYwxmcd6zDGRtmQePEykAgubymMRFmMxCSIPCRbTuFNN5OGORTjmNGc0Po0m8Uv0gcCry6xUhR2QeLii9tofbEfhz/qvNti+OfPqNm2Mq6105FUMvdT4GPmufMiV8PqBMkc+DdT1bjYYbjzU/ew23VP4n3mLAz4n8Jtv/Ui3ceTT2mzz5o1mZt0gnBpmsdjqRqVlmplcPdqa7X23kL9brdm2t/uBYDPn2+tyu48mtIGD10JTuUrukVrbCFiwDzcHrPjxKt7PW+AZQyT/WESO+1WL7f3o+WLHL2dYMSZsg6dg/z360ofvP4//v1NPzgs28WlWAAAAAElFTkSuQmCC"

    companion object {
        public const val PUBNUB_SUBSCRIBE_KEY                 = "sub-c-6597d23e-1b1d-11ea-b79a-866798696d74"
        public const val PUBNUB_PUBLISH_KEY                   = "pub-c-62f722d6-c307-4dd9-89dc-e598a9164424"

        public const val SOCKET_REQUEST_RESTO_TABLE           = "request_resto_table"
        public const val SOCKET_REQUEST_ASSIGN_WAITER         = "request_assign_waiter"
        public const val SOCKET_REQUEST_NOTIFY_ORDER          = "request_notify_order"
        public const val SOCKET_REQUEST_W_NOTIFY_ORDER        = "request_w_notify_order"


        public const val SOCKET_RESPONSE_ERROR                = "response_error"
        public const val SOCKET_RESPONSE_PLACEMENT_DONE       = "response_resto_placement_done"
        public const val SOCKET_RESPONSE_TABLE_WAITER         = "response_resto_table_waiter"
        public const val SOCKET_RESPONSE_RESTO_TABLE          = "response_resto_table"
        public const val SOCKET_RESPONSE_RESTO_BILL_PAID      = "response_resto_bill_paid"

        public const val CONFIRM_TITLE_KEY                    = "title"
        public const val CONFIRM_MESSAGE_KEY                  = "message"

        public const val ADMIN_KEY                            = "administrator"
        public const val CATEGORY_KEY                         = "category"
        public const val TABLE_KEY                            = "table"
        public const val BRANCH_KEY                           = "branch"

        public const val NORMAL_TABLE_VIEW_CELL               = 0
        public const val IMAGE_TABLE_VIEW_CELL                = 1
        public const val ACTIONS_TABLE_VIEW_CELL              = 2
        public const val WAITER_TABLE_VIEW_CELL               = 3


        public val ADMIN_TYPE = hashMapOf<Int, String>(1 to "Administrator", 2 to "Supervisor", 3 to "Chef", 4 to "Waiter", 5 to "Accountant")

        public val TABLE_LOCATION = hashMapOf<Int, String>(1 to "Inside", 2 to "Outside", 3 to "Balcony", 4 to "Rooftop")

        public val CHAIR_TYPE = hashMapOf<Int, String>(1 to "Iron", 2 to "Wooden", 3 to "Plastic", 4 to "Couch", 5 to "Mixture")

        public val MENU_TYPE = hashMapOf<Int, String>(1 to "Food", 2 to "Drink", 3 to "Dish")

        public val FOOD_TYPE = hashMapOf<Int, String>(1 to "Appetizer", 2 to "Meal", 3 to "Desert", 4 to "Sauce")

        public val DRINK_TYPE = hashMapOf<Int, String>(
            1 to "Water", 2 to "Hot Beverage", 3 to "Beer",
            4 to "Alcohol", 5 to "Soda", 6 to "Juice", 7 to "Smoothie",
            8 to "Cocktail", 9 to "Wine", 10 to "Other"
        )

        public val DISH_TIME = hashMapOf<Int, String>(
            1 to "Breakfast", 2 to "Lunch", 3 to "Dinner",
            4 to "Supper", 5 to "Brunch", 6 to "Snack", 7 to "Other"
        )

        public val PAID_BY =  hashMapOf<Int, String>(1 to "Cash", 2 to "Mobile Money", 3 to "Credit Card")

        public val CURRENCIES = hashMapOf<String, String>(
            "USD" to "United State Dollar",
            "UGX" to "Ugandan Shillings",
            "GBP" to "Grand Britain Pounds"
        )

        public val PROMOTION_MENU = hashMapOf<String, String>(
            "00" to "All Menus",
            "01" to "Food Menus",
            "02" to "Drink Menus",
            "03" to "Dish Menus",
            "04" to "Specific Menu",
            "05" to "Specific Category"
        )

        public val PROMOTION_PERIOD = hashMapOf<Int, String>(
            1 to "Every Day",
            2 to "Every Monday",
            3 to "Every Tuesday",
            4 to "Every Wednesday",
            5 to "Every Thursday",
            6 to "Every Friday",
            7 to "Every Saturday",
            8 to "Every Weekend"
        )

        public val USER_ADDRESS_TYPE = hashMapOf<Int, String>(1 to "Home", 2 to "Work", 3 to "School", 4 to "Other")

        public val USER_ADDRESS_TYPE_LIST = arrayListOf<String>("Home", "Work", "School", "Other")

        public val RESTAURANT_AVAILABILITY = mutableListOf<BranchSpecial>(
            BranchSpecial(1, "Not Available", ""),
            BranchSpecial(2, "Opened", ""),
            BranchSpecial(3, "Closed", "")
        )
        
        public val RESTAURANT_RATINGS = mutableListOf<BranchSpecial>(
            BranchSpecial(1, "1 Star", ""),
            BranchSpecial(2, "2 Stars", ""),
            BranchSpecial(3, "3 Stars", ""),
            BranchSpecial(4, "4 Stars", ""),
            BranchSpecial(5, "5 Stars", "")
        )

        public val RESTAURANT_SPECIALS = mutableListOf<BranchSpecial>(
            BranchSpecial(1, "Wi-Fi","wifi"),
            BranchSpecial(2, "Phone Booth","phone"),
            BranchSpecial(3, "TV", "tv"),
            BranchSpecial(4, "Parking","car"),
            BranchSpecial(5, "Karaoke","microphone"),
            BranchSpecial(6, "Jazz","music"),
            BranchSpecial(7, "Bar","glass martini"),
            BranchSpecial(8, "Guards", "shield alternate")
        )

        public val RESTAURANT_SERVICES = mutableListOf<BranchSpecial>(
            BranchSpecial(1, "Hotel","building"),
            BranchSpecial(2, "Spa","smile"),
            BranchSpecial(3, "Bar","glass martini"),
            BranchSpecial(4, "Meeting Space","bullhorn"),
            BranchSpecial(5, "Parties","birthday cake")
        )

        public val BOOKING_PAYEMENT_MODE = hashMapOf<Int, String>(
            1 to "Online (Credit Card)",
            2 to "Cash (On Site)",
            3 to "Both (Online / Cash)"
        )

        public val BOOKING_STATUSES =  hashMapOf<Int, String>(
            1 to "Pending", 2 to "Declined", 3 to "Accepted", 4 to "Paid",
            5 to "Completed", 6 to "Refunded", 7 to "Canceled"
        )

        public val RESTAURANT_TYPES = mutableListOf<BranchSpecial>(
            BranchSpecial(1, "Restaurant", ""),
            BranchSpecial(2, "Outlet", ""),
            BranchSpecial(3, "Resto & Bar", ""),
            BranchSpecial(4, "Bar", ""),
            BranchSpecial(5, "Coffee Shop", ""),
            BranchSpecial(6, "Supermarket", ""),
            BranchSpecial(7, "Food Truck", ""),
            BranchSpecial(8, "Bakery", "")
        )
    }
}