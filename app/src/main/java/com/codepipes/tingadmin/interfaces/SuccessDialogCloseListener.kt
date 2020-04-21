package com.codepipes.tingadmin.interfaces

import android.content.DialogInterface


interface SuccessDialogCloseListener {
    fun handleDialogClose(dialog: DialogInterface?)
}