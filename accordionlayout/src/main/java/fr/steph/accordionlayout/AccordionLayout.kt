package fr.steph.accordionlayout

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import fr.steph.accordionlayout.WidgetHelper.getFullHeight
import fr.steph.accordionlayout.WidgetHelper.isNullOrBlank

class AccordionLayout: RelativeLayout {
    private val defaultTextColor = Color.parseColor("#737373")
    private lateinit var children: Array<View?>
    private lateinit var headingLayout: RelativeLayout
    private lateinit var heading: TextView
    private lateinit var partition: View
    private lateinit var body: RelativeLayout
    private lateinit var dropdownImage: ImageView
    private lateinit var dropupImage: ImageView
    private lateinit var headingImage: ImageView
    private lateinit var inflater: LayoutInflater
    private var expandCollapseListener: AccordionExpansionCollapseListener? = null

    private var isExpanded = false
    private var isAnimated = false
    private var isPartitioned = false

    private var headingString: String? = null
    private var headingColor = defaultTextColor
    private var headingTextSize = 0
    private var headingDrawable: Drawable? = null
    private var headingDrawableWidth = 0
    private var headingDrawableHeight = 0
    private var headingBackground: Drawable? = null
    private var headingBackgroundColor = Color.WHITE

    private var bodyTopMargin = 0
    private var bodyBottomMargin = 0
    private var bodyBackgroundColor = Color.WHITE
    private var bodyBackground: Drawable? = null

    constructor(context: Context) : super(context) { prepareLayoutWithoutChildren(context) }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { handleAttributeSet(context, attrs) }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { handleAttributeSet(context, attrs) }

    private fun handleAttributeSet(context: Context, attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.accordion, 0, 0).apply {
            try {
                isExpanded = getBoolean(R.styleable.accordion_isExpanded, false)
                isAnimated = getBoolean(R.styleable.accordion_isAnimated, false)
                isPartitioned = getBoolean(R.styleable.accordion_isPartitioned, false)
                headingString = getString(R.styleable.accordion_headingString)
                headingTextSize = getDimensionPixelSize(R.styleable.accordion_headingTextSize, 20)
                headingColor = getColor(R.styleable.accordion_headingColor, defaultTextColor)
                headingDrawable = getDrawable(R.styleable.accordion_headingDrawable)
                headingDrawableWidth = getDimensionPixelSize(R.styleable.accordion_headingDrawableWidth, 0)
                headingDrawableHeight = getDimensionPixelSize(R.styleable.accordion_headingDrawableHeight, 0)
                headingBackground = getDrawable(R.styleable.accordion_headingBackground)
                headingBackgroundColor = getColor(R.styleable.accordion_headingBackgroundColor, 0)
                bodyBackground = getDrawable(R.styleable.accordion_bodyBackground)
                bodyBackgroundColor = getColor(R.styleable.accordion_bodyBackgroundColor, 0)
            }
            finally {
                recycle()
            }
        }
    }

    private fun initializeViewWithoutChildren(context: Context) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val accordionLayout = inflater.inflate(R.layout.accordion, this,false) as LinearLayout
        headingLayout = accordionLayout.findViewById(R.id.heading_layout)
        heading = accordionLayout.findViewById(R.id.heading)
        partition = accordionLayout.findViewById(R.id.partition)
        body = accordionLayout.findViewById(R.id.body_layout)
        dropdownImage = accordionLayout.findViewById(R.id.dropdown_image)
        dropupImage = accordionLayout.findViewById(R.id.dropup_image)
        headingImage = accordionLayout.findViewById(R.id.heading_image)
        body.removeAllViews()
        removeAllViews()
        bodyBottomMargin = (body.layoutParams as LinearLayout.LayoutParams).bottomMargin
        bodyTopMargin = (body.layoutParams as LinearLayout.LayoutParams).topMargin
        addView(accordionLayout)
    }

    private fun initializeViews(context: Context) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val accordionLayout = inflater.inflate(R.layout.accordion, this, false) as LinearLayout
        headingLayout = accordionLayout.findViewById(R.id.heading_layout)
        heading = accordionLayout.findViewById(R.id.heading)
        partition = accordionLayout.findViewById(R.id.partition)
        body = accordionLayout.findViewById(R.id.body_layout)
        dropdownImage = accordionLayout.findViewById(R.id.dropdown_image)
        dropupImage = accordionLayout.findViewById(R.id.dropup_image)
        headingImage = accordionLayout.findViewById(R.id.heading_image)
        body.removeAllViews()
        children = arrayOfNulls(childCount)

        var i = 0
        while (i < childCount) children[i] = getChildAt(i++)

        removeAllViews()

        i = 0
        while (i < children.size) body.addView(children[i++])

        bodyBottomMargin = (body.layoutParams as LinearLayout.LayoutParams).bottomMargin
        bodyTopMargin = (body.layoutParams as LinearLayout.LayoutParams).topMargin
        addView(accordionLayout)
    }

    private fun prepareLayout(context: Context) {
        initializeViews(context)

        headingLayout.layoutTransition = if(isAnimated) LayoutTransition() else null
        heading.text = headingString
        heading.textSize = headingTextSize.toFloat()
        heading.setTextColor(headingColor)
        if(!isNullOrBlank(headingDrawable)){
            headingImage.setImageDrawable(headingDrawable)
            if(headingDrawableWidth != 0)headingImage.layoutParams.width = headingDrawableWidth
            if(headingDrawableHeight != 0)headingImage.layoutParams.height = headingDrawableHeight
        }

        if (!isNullOrBlank(headingBackground)) headingLayout.background = headingBackground
        else if (!isNullOrBlank(headingBackgroundColor)) headingLayout.setBackgroundColor(headingBackgroundColor)

        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE

        body.visibility = VISIBLE
        if (!isNullOrBlank(bodyBackground)) body.background = bodyBackground
        else if (!isNullOrBlank(bodyBackgroundColor)) body.setBackgroundColor(bodyBackgroundColor)

        if (isExpanded) expand() else collapse()

        setOnClickListenerOnHeading()
    }

    private fun prepareLayoutWithoutChildren(context: Context) {
        initializeViewWithoutChildren(context)

        headingLayout.layoutTransition = if(isAnimated) LayoutTransition() else null
        heading.text = headingString

        body.visibility = VISIBLE
        partition.visibility = if(isPartitioned) VISIBLE else INVISIBLE

        if (isExpanded) expand() else collapse()

        setOnClickListenerOnHeading()
    }

    override fun onFinishInflate() {
        prepareLayout(context)
        super.onFinishInflate()
    }

    private fun expand() {
        if (isAnimated) {
            val expandAnimation = AccordionTransitionAnimation(body, 300, AccordionTransitionAnimation.EXPAND)
            expandAnimation.height = getFullHeight(body)
            expandAnimation.endBottomMargin = bodyBottomMargin
            expandAnimation.endTopMargin = bodyTopMargin
            body.startAnimation(expandAnimation)
        }
        else body.visibility = VISIBLE

        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE
        dropupImage.visibility = VISIBLE
        dropdownImage.visibility = GONE
        if (!isNullOrBlank(expandCollapseListener)) expandCollapseListener!!.onExpanded(this)
    }

    private fun collapse() {
        if (isAnimated) {
            val collapseAnimation = AccordionTransitionAnimation(body, 300, AccordionTransitionAnimation.COLLAPSE)
            body.startAnimation(collapseAnimation)
        }
        else body.visibility = GONE

        partition.visibility = INVISIBLE
        dropupImage.visibility = GONE
        dropdownImage.visibility = VISIBLE
        if (!isNullOrBlank(expandCollapseListener)) expandCollapseListener!!.onCollapsed(this)
    }

    private fun setOnClickListenerOnHeading() {
        heading.setOnClickListener(togglebodyVisiblity)
        dropdownImage.setOnClickListener(togglebodyVisiblity)
        dropupImage.setOnClickListener(togglebodyVisiblity)
    }

    private var togglebodyVisiblity = OnClickListener {
        if (body.visibility == VISIBLE) collapse()
        else expand()
    }

    fun addViewToBody(child: View) {
        body.addView(child)
    }

    fun setHeadingString(headingString: String) {
        heading.text = headingString
    }

    fun setIsAnimated(isAnimated: Boolean) {
        this.isAnimated = isAnimated
    }

    fun getAnimated(): Boolean {
        return isAnimated
    }

    fun setAnimated(animated: Boolean) {
        isAnimated = animated
    }

    fun setOnExpandCollapseListener(listener: AccordionExpansionCollapseListener) {
        this.expandCollapseListener = listener
    }

    fun setOnHeadingImageClickListener(listener: OnClickListener){
        headingImage.setOnClickListener(listener)
    }

    fun setOnHeadingImageLongClickListener(listener: OnLongClickListener){
        headingImage.setOnLongClickListener(listener)
    }

    fun getBody(): RelativeLayout {
        return body
    }

    fun getbody(): RelativeLayout {
        return body
    }

    fun getExpanded(): Boolean {
        return isExpanded
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
    }

    fun getPartitioned(): Boolean {
        return isPartitioned
    }

    fun setPartitioned(partitioned: Boolean) {
        isPartitioned = partitioned
        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE
    }

    fun setHeadingBackground(drawable: Drawable){
        if(isNullOrBlank(headingLayout)) headingLayout = findViewById(R.id.heading_layout)
        headingLayout.background = drawable
    }

    fun setHeadingBackground(resId: Int){
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        if(isNullOrBlank(headingLayout)) headingLayout = findViewById(R.id.heading_layout)
        headingLayout.background = drawable
    }

    fun setHeadingColor(color: Int){
        heading.setTextColor(color)
    }

    fun setHeadingImage(drawable: Drawable){
        headingImage.setImageDrawable(drawable)
    }

    fun setHeadingImage(resId: Int){
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        headingImage.setImageDrawable(drawable)
    }

    fun setBodyBackground(drawable: Drawable){
        if(isNullOrBlank(body)) body = findViewById(R.id.body_layout)
        body.background = drawable
    }

    fun setBodyBackground(resId: Int){
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        if(isNullOrBlank(body)) body = findViewById(R.id.body_layout)
        body.background = drawable
    }

    fun setHeadingBackgroundColor(color: Int){
        if(isNullOrBlank(headingLayout))headingLayout = findViewById(R.id.heading_layout)
        headingLayout.setBackgroundColor(color)
    }

    fun setBodyBackgroundColor(color: Int){
        if(isNullOrBlank(body))body = findViewById(R.id.body_layout)
        body.setBackgroundColor(color)
    }
}