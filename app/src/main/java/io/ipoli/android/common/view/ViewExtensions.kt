package io.ipoli.android.common.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.support.annotation.LayoutRes
import android.support.constraint.Group
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import io.ipoli.android.R
import io.ipoli.android.common.ViewUtils
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import org.commonmark.node.Heading
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableBuilder
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableMarkdownVisitor
import ru.noties.markwon.spans.SpannableTheme

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 8/27/17.
 */
var View.visible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

fun View.setScale(scale: Float) {
    scaleX = scale
    scaleY = scale
}

fun Group.views() =
    referencedIds.map { id ->
        rootView.findViewById<View>(id)
    }

fun Group.goneViews() {
    this.visibility = View.GONE
}

fun Group.hideViews() {
    this.visibility = View.INVISIBLE
}

fun Group.showViews() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.enableClick() {
    isClickable = true
}

fun View.disableClick() {
    isClickable = false
}

fun View.visibleOrGone(isVisible: Boolean) {
    if (isVisible) visible()
    else gone()
}

val ViewGroup.children: List<View>
    get() = 0.until(childCount).map { getChildAt(it) }

fun ViewGroup.inflate(@LayoutRes layout: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(layout, this, attachToRoot)

fun View.fadeIn(
    animationDuration: Long,
    to: Float = 1f,
    delay: Long = 0L,
    onComplete: () -> Unit = {}
) {
    alpha = 0f
    animate().apply {
        alpha(to)
        startDelay = delay
        duration = animationDuration
        interpolator = AccelerateInterpolator()
        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                alpha = to
                onComplete()
            }
        })
    }
}

fun View.fadeOut(animationDuration: Long, onComplete: () -> Unit = {}) {
    alpha = 1f
    animate().apply {
        alpha(0f)
        duration = animationDuration
        setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                alpha = 0f
                onComplete()
            }
        })
    }
}

fun TextView.setMarkdown(markdown: String) {
    val parser = Markwon.createParser()

    val theme = SpannableTheme.builderWithDefaults(context)
        .headingBreakHeight(0)
        .thematicBreakColor(context.attrData(io.ipoli.android.R.attr.colorAccent))
        .listItemColor(context.attrData(io.ipoli.android.R.attr.colorAccent))
        .linkColor(context.attrData(io.ipoli.android.R.attr.colorAccent))
        .blockQuoteColor(context.attrData(io.ipoli.android.R.attr.colorAccent))
        .codeBackgroundColor(context.colorRes(io.ipoli.android.R.color.sourceCodeBackground))
        .codeTextColor(context.colorRes(io.ipoli.android.R.color.sourceCodeText))
        .codeTextSize(ViewUtils.spToPx(14, context))
        .build()
    val configuration = SpannableConfiguration.builder(context)
        .theme(theme)
        .build()

    val builder = SpannableBuilder()

    val node = parser.parse(markdown)

    val headlineVisitor = HeadlineColorVisitor(context, configuration, builder)

    node.accept(headlineVisitor)

    val text = builder.text()

    movementMethod = LinkMovementMethod.getInstance()

    Markwon.unscheduleDrawables(this)
    Markwon.unscheduleTableRows(this)

    setText(text)

    Markwon.scheduleDrawables(this)
    Markwon.scheduleTableRows(this)
}

class HeadlineColorVisitor(
    private val context: Context,
    config: SpannableConfiguration,
    private val builder: SpannableBuilder
) : SpannableMarkdownVisitor(config, builder) {

    override fun visit(heading: Heading) {

        val startLength = builder.length()

        super.visit(heading)

        builder.setSpan(
            ForegroundColorSpan(context.attrData(R.attr.colorAccent)),
            startLength,
            builder.length()
        )
    }
}

object Debounce {

    fun clickListener(action: suspend (View) -> Unit): View.OnClickListener {
        val eventActor = GlobalScope.actor<View>(Dispatchers.Main) {
            for (event in channel) {
                action(event)
                delay(400)
            }
            this.cancel()
        }

        return View.OnClickListener {
            eventActor.offer(it)
        }
    }
}