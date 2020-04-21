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
    }
}