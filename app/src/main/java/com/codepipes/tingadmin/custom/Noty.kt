package com.codepipes.tingadmin.custom


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import com.codepipes.tingadmin.R
import com.codepipes.tingadmin.utils.Routes
import com.squareup.picasso.Picasso

@SuppressLint("InflateParams", "ClickableViewAccessibility")
class Noty(
    private val context: Context,
    image: String?,
    title: String,
    message: String,
    private val parentLayout: ViewGroup,
    private val style: NotyStyle
) {
    private lateinit var notyView: RelativeLayout
    private lateinit var contentFrame: View
    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var imageView: RoundedCornerImageView

    private lateinit var notyViewParams: RelativeLayout.LayoutParams

    private var notyBoxPosition                     = NotyPosition.TOP

    private var topLeftRadius                       = 2f
    private var topRightRadius                      = 2f
    private var bottomLeftRadius                    = 2f
    private var bottomRightRadius                   = 2f
    private var shadowRadius                        = 3f
    private var shadowOffsetX                       = 0f
    private var shadowOffsetY                       = 2f

    private var notyInsetLeft                       = 3f
    private var notyInsetUp                         = 3f
    private var notyInsetRight                      = 3f
    private var notyInsetBottom                     = 3f

    private var notyBoxBgColor                      = "#00000000"
    private var tappedColor                         = "#00000000"
    private var shadowColor                         = "#B4B4B4"
    private var hasShadow                           = true

    private var revealTime                          = 200
    private var dismissTime                         = 200
    private var tapToDismiss                        = true
    private val revealAnim                          = RevealAnim.SLIDE_UP
    private val dismissAnim                         = DismissAnim.BACK_TO_BOTTOM

    private var shapeDrawable: ShapeDrawable?       = null
    private var layerDrawable: LayerDrawable?       = null

    private var tapListener: TapListener?           = null
    private var clickListener: ClickListener?       = null
    private var animListener: AnimListener?         = null

    enum class NotyPosition {
        TOP, BOTTOM, CENTER
    }

    enum class NotyStyle {
        SIMPLE, ACTION
    }

    enum class TextStyle {
        NORMAL, BOLD, ITALIC, BOLD_ITALIC
    }

    enum class RevealAnim {
        SLIDE_UP, SLIDE_DOWN, FADE_IN, NO_ANIM
    }

    enum class DismissAnim {
        BACK_TO_TOP, BACK_TO_BOTTOM, FADE_OUT, NO_ANIM
    }

    private var screenWidth                         = 0
    private var screenHeight                        = 0
    private var revealAnimation: Animation?         = null
    private var dismissAnimation: Animation?        = null

    interface TapListener {
        public fun onTap(noty: Noty?)
    }

    interface ClickListener {
        fun onClick(noty: Noty?)
    }

    interface AnimListener {
        public fun onRevealStart(noty: Noty?)
        public fun onRevealEnd(noty: Noty?)
        public fun onDismissStart(noty: Noty?)
        public fun onDismissEnd(noty: Noty?)
    }

    public fun setTapListener(listener: TapListener?): Noty {
        tapListener = listener
        return this
    }

    public fun setClickListener(listener: ClickListener?): Noty {
        clickListener = listener
        return this
    }

    public fun setAnimationListener(listener: AnimListener?): Noty {
        animListener = listener
        return this
    }

    public fun tapToDismiss(tapToDismiss: Boolean): Noty {
        this.tapToDismiss = tapToDismiss
        return this
    }

    private fun setAnimation(
        reveal: RevealAnim,
        dismiss: DismissAnim,
        revealTime: Int,
        dismissTime: Int
    ): Noty {
        this.revealTime = revealTime
        this.dismissTime = dismissTime
        when (reveal) {
            RevealAnim.SLIDE_UP -> {
                revealAnimation = TranslateAnimation(0f, 0f, screenHeight.toFloat(), 0f)
            }
            RevealAnim.SLIDE_DOWN -> {
                revealAnimation = TranslateAnimation(0f, 0f, (-screenHeight).toFloat(), 0f)
            }
            RevealAnim.FADE_IN -> {
                revealAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            }
            RevealAnim.NO_ANIM -> {
                revealAnimation = TranslateAnimation(0f, 0f, 0f, 0f)
                this.revealTime = 0
            }
        }
        when (dismiss) {
            DismissAnim.BACK_TO_BOTTOM -> {
                dismissAnimation = TranslateAnimation(0f, 0f, 0f, screenHeight.toFloat())
            }
            DismissAnim.BACK_TO_TOP -> {
                dismissAnimation = TranslateAnimation(0f, 0f, 0f, (-screenHeight).toFloat())
            }
            DismissAnim.FADE_OUT -> {
                dismissAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            }
            DismissAnim.NO_ANIM -> {
                dismissAnimation = TranslateAnimation(0f, 0f, 0f, 0f)
                this.dismissTime = 0
            }
        }
        return this
    }

    private fun createShape() {
        shapeDrawable = ShapeDrawable()
        layerDrawable = LayerDrawable(arrayOf<Drawable>(shapeDrawable!!))
    }

    private fun createNoty(color: String, isToggle: Boolean) {
        setNotyBoxRadius(
            topLeftRadius,
            topRightRadius,
            bottomLeftRadius,
            bottomRightRadius
        )
        if (isToggle) {
            toggleNotyBgColor(color)
        } else {
            setNotyBoxBgColor(color)
        }
        setNotyBoxShadow(
            shadowRadius,
            shadowOffsetX,
            shadowOffsetY,
            shadowColor
        )
        shapeDrawable!!.paint.isAntiAlias = true
        shapeDrawable!!.paint.style = Paint.Style.FILL
        setNotyInset(
            notyInsetLeft,
            notyInsetUp,
            notyInsetRight,
            notyInsetBottom
        )
    }

    public fun setNotyBoxRadius(
        topLeftRadius: Float, topRightRadius: Float,
        bottomLeftRadius: Float, bottomRightRadius: Float
    ): Noty {
        val topLeft = dpToPx(topLeftRadius)
        val topRight = dpToPx(topRightRadius)
        val bottomLeft = dpToPx(bottomLeftRadius)
        val bottomRight = dpToPx(bottomRightRadius)
        this.topLeftRadius = topLeftRadius
        this.topRightRadius = topRightRadius
        this.bottomLeftRadius = bottomLeftRadius
        this.bottomRightRadius = bottomRightRadius
        shapeDrawable!!.shape = RoundRectShape(
            floatArrayOf(
                topLeft.toFloat(),
                topLeft.toFloat(),
                topRight.toFloat(),
                topRight.toFloat(),
                bottomLeft.toFloat(),
                bottomLeft.toFloat(),
                bottomRight.toFloat(),
                bottomRight.toFloat()
            ), null, null
        )
        return this
    }

    public fun setShadowColor(htmlColor: String): Noty {
        shadowColor = htmlColor
        shapeDrawable!!.paint.setShadowLayer(
            shadowRadius,
            shadowOffsetX,
            shadowOffsetY,
            Color.parseColor(htmlColor)
        )
        return this
    }

    public fun setNotyBoxBgColor(htmlColor: String): Noty {
        notyBoxBgColor = htmlColor
        shapeDrawable!!.paint.color = Color.parseColor(htmlColor)
        return this
    }

    private fun toggleNotyBgColor(htmlColor: String) {
        shapeDrawable!!.paint.color = Color.parseColor(htmlColor)
    }

    public fun setNotyTappedColor(htmlColor: String): Noty {
        tappedColor = htmlColor
        return this
    }

    public fun setNotyBoxShadow(
        shadowRadius: Float,
        shadowOffsetX: Float,
        shadowOffsetY: Float,
        color: String?
    ): Noty {
        val radius = dpToPx(shadowRadius)
        val x = dpToPx(shadowOffsetX)
        val y = dpToPx(shadowOffsetY)
        this.shadowRadius = shadowRadius
        this.shadowOffsetX = shadowOffsetX
        this.shadowOffsetY = shadowOffsetY
        if (color == null) {
            shapeDrawable!!.paint.setShadowLayer(
                radius.toFloat(),
                x.toFloat(),
                y.toFloat(),
                Color.parseColor(shadowColor)
            )
        } else {
            shapeDrawable!!.paint.setShadowLayer(
                radius.toFloat(),
                x.toFloat(),
                y.toFloat(),
                Color.parseColor(color)
            )
            shadowColor = color
        }
        return this
    }

    public fun setNotyInset(
        notyInsetLeft: Float,
        notyInsetUp: Float,
        notyInsetRight: Float,
        notyInsetBottom: Float
    ): Noty {
        this.notyInsetUp = notyInsetUp
        this.notyInsetBottom = notyInsetBottom
        this.notyInsetLeft = notyInsetLeft
        this.notyInsetRight = notyInsetRight
        layerDrawable!!.setLayerInset(
            0,
            dpToPx(notyInsetLeft),
            dpToPx(notyInsetUp),
            dpToPx(notyInsetRight),
            dpToPx(notyInsetBottom)
        )
        return this
    }

    public fun hasShadow(hasShadow: Boolean): Noty {
        this.hasShadow = hasShadow
        if (!hasShadow) {
            shadowRadius = 0f
            setNotyBoxShadow(
                shadowRadius,
                shadowOffsetX,
                shadowOffsetY,
                shadowColor
            )
        }
        return this
    }

    public fun setNotyBoxPosition(position: NotyPosition): Noty {
        notyViewParams.addRule(getNotyPos(notyBoxPosition), 0)
        notyViewParams.addRule(getNotyPos(position))
        notyViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        notyBoxPosition = position
        return this
    }

    private fun getNotyPos(position: NotyPosition): Int {
        return if (position == NotyPosition.TOP) RelativeLayout.ALIGN_PARENT_TOP else if (position == NotyPosition.BOTTOM) RelativeLayout.ALIGN_PARENT_BOTTOM else if (position == NotyPosition.CENTER) RelativeLayout.CENTER_VERTICAL else RelativeLayout.ALIGN_PARENT_TOP
    }

    public fun show(): Noty {

        notyView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        notyView.background = layerDrawable
        notyView.layoutParams = notyViewParams

        parentLayout.addView(notyView)
        notyView.addView(contentFrame)

        revealAnimation!!.duration = revealTime.toLong()
        revealAnimation!!.fillAfter = true
        revealAnimation!!.setInterpolator(context, android.R.anim.decelerate_interpolator)

        notyView.visibility = View.VISIBLE
        notyView.startAnimation(revealAnimation)

        dismissAnimation!!.duration = dismissTime.toLong()
        dismissAnimation!!.fillAfter = true
        dismissAnimation!!.setInterpolator(context, android.R.anim.accelerate_interpolator)
        notyView.visibility = View.VISIBLE

        return this
    }

    private fun attachAnimListener() {
        revealAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (animListener != null) {
                    animListener!!.onRevealStart(this@Noty)
                }
            }

            override fun onAnimationEnd(animation: Animation) {
                if (animListener != null) {
                    animListener!!.onRevealEnd(this@Noty)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        dismissAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (animListener != null) {
                    animListener!!.onDismissStart(this@Noty)
                }
            }

            override fun onAnimationEnd(animation: Animation) {
                if (animListener != null) {
                    animListener!!.onDismissEnd(this@Noty)
                }
                parentLayout.removeView(notyView)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    inner class CustomTapListener : OnTouchListener {
        override fun onTouch(
            view: View,
            motionEvent: MotionEvent
        ): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    createNoty(tappedColor, true)
                    notyView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    notyView.background = layerDrawable
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (tapToDismiss) {
                        createNoty(notyBoxBgColor, false)
                        notyView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        notyView.background = layerDrawable
                        notyView.startAnimation(dismissAnimation)
                    }
                    if (tapListener != null) {
                        tapListener!!.onTap(this@Noty)
                    }
                }
            }
            return true
        }
    }

    private fun getScreen() {
        val wm =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
    }

    private fun dpToPx(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    companion object {
        public fun init(
            context: Context, image: String, title: String, message: String,
            parentLayout: ViewGroup, style: NotyStyle
        ): Noty { return Noty(context, image, title, message, parentLayout, style) }
    }

    init {

        getScreen()

        notyView = RelativeLayout(context)
        notyView.gravity = Gravity.CENTER_VERTICAL

        notyViewParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        notyView.layoutParams = notyViewParams

        setNotyBoxPosition(notyBoxPosition)
        createShape()
        createNoty(notyBoxBgColor, false)

        contentFrame = LayoutInflater.from(context).inflate(R.layout.view_noty, null, false)

        val contentFrameLayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        contentFrameLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        contentFrame.layoutParams = contentFrameLayoutParams

        titleTextView = contentFrame.findViewById(R.id.noty_title)
        messageTextView = contentFrame.findViewById(R.id.noty_message)
        imageView = contentFrame.findViewById(R.id.noty_image)

        titleTextView.text = title
        messageTextView.text = message
        Picasso.get().load(if(image?.contains("http") == true || image?.contains("www") == true) {
            image
        } else { "${Routes.HOST_END_POINT}$image" }).into(imageView)

        setAnimation(revealAnim, dismissAnim, revealTime, dismissTime)

        notyView.setOnTouchListener(CustomTapListener())
        attachAnimListener()
    }
}