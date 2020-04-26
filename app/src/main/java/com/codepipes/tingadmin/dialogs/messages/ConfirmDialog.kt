package com.codepipes.tingadmin.dialogs.messages

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.ConfirmDialogListener
import com.codepipes.tingadmin.utils.Constants
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.fragment_confirm_dialog.view.*

class ConfirmDialog : DialogFragment() {

    private var confirmDialogListener: ConfirmDialogListener? = null

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.fragment_confirm_dialog, null, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        val title = arguments?.getString(Constants.CONFIRM_TITLE_KEY)
        val message = arguments?.getString(Constants.CONFIRM_MESSAGE_KEY)

        view.dialog_title.text = title
        if(message != null) { view.dialog_text.text = message } else { view.dialog_text.visibility = View.GONE }
        view.dialog_yes.setOnClickListener {
            if(confirmDialogListener != null) { confirmDialogListener?.onAccept()
            } else {
                TingToast(context!!, "State Changed. Please, Try Again", TingToastType.DEFAULT).showToast(
                    Toast.LENGTH_LONG)
                dialog?.dismiss()
            }
        }
        view.dialog_cancel.setOnClickListener {
            if(confirmDialogListener != null) {
                confirmDialogListener?.onCancel()
            } else { dialog?.dismiss() }
        }

        return view
    }

    public fun onDialogListener(listener: ConfirmDialogListener) {
        this.confirmDialogListener = listener
    }
}