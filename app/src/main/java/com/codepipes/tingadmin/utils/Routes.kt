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

        //GLOBALS
        const val permissionsAll: String                = "${END_POINT}adm/g/permissions/all/"

        //ADMINISTRATORS
        const val administratorsAll: String             = "${END_POINT}adm/administrators/all/"
        const val updateAdminProfile: String            = "${END_POINT}adm/admin/profile/update/profile/"
        const val updateAdminImage: String              = "${END_POINT}adm/admin/profile/update/image/"
        const val addNewAdmin: String                   = "${END_POINT}adm/administrators/add/"
        const val updateAdminPassword: String           = "${END_POINT}adm/admin/security/update/password/"
        const val disableAdministratorToggle: String    = "${END_POINT}adm/admin/profile/disable/toggle/"
        const val updateAdminPermissions: String        = "${END_POINT}adm/admin/permissions/update/"

        //BRANCHES
        const val branchesAll: String                   = "${END_POINT}adm/branches/all/"

        //CATEGORIES
        const val categoriesAll: String                 = "${END_POINT}adm/categories/all/"
        const val deleteCategory: String                = "${END_POINT}adm/categories/delete/"
        const val updateCategory: String                = "${END_POINT}adm/categories/update/"
        const val addNewCategory: String                = "${END_POINT}adm/categories/add/new/"
    }
}