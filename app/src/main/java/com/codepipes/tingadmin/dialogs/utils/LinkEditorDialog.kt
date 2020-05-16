package com.codepipes.tingadmin.dialogs.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.EditorLinkListener
import com.livefront.bridge.Bridge
import kotlinx.android.synthetic.main.dialog_editor_link.view.*

class LinkEditorDialog : DialogFragment() {

    private lateinit var editorLinkListener: EditorLinkListener

    override fun getTheme(): Int = R.style.TransparentDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_editor_link, container, false)

        Bridge.restoreInstanceState(this, savedInstanceState)
        savedInstanceState?.clear()

        view.dialog_success.setOnClickListener {
            editorLinkListener.onLinkSet(view.dialog_link_url.text.toString(), view.dialog_link_title.text.toString())
        }
        view.dialog_close.setOnClickListener { dialog?.dismiss() }

        return view
    }

    public fun setOnLinkSet(listener: EditorLinkListener) {
        editorLinkListener = listener
    }
}