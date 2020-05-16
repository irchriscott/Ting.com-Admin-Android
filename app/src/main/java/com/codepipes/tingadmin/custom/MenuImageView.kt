package com.codepipes.tingadmin.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.interfaces.DeleteImageListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_menu_image.view.*
import java.io.File

class MenuImageView : RelativeLayout {

    public lateinit var view: View
    private lateinit var deleteImageListener: DeleteImageListener

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    @SuppressLint("InflateParams")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.view_menu_image, null, false)
        view.button_delete.setOnClickListener { deleteImageListener.onDeleteImage() }
        addView(view)
    }

    public fun setImage(image: String) {
        Picasso.get().load(image).into(view.menu_image)
    }

    public fun setImage(image: File) {
        Picasso.get().load(image).into(view.menu_image)
    }

    public fun setOnDeleteImage(listener: DeleteImageListener) {
        this.deleteImageListener = listener
    }
}