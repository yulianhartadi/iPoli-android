package mypoli.android.common.view

import android.support.annotation.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import mypoli.android.MainActivity
import mypoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/7/17.
 */
fun Controller.stringRes(@StringRes stringRes: Int): String =
    resources!!.getString(stringRes)

fun Controller.stringRes(@StringRes stringRes: Int, vararg formatArgs: Any): String =
    resources!!.getString(stringRes, *formatArgs)

fun Controller.stringsRes(@ArrayRes stringArrayRes: Int): List<String> =
    resources!!.getStringArray(stringArrayRes).toList()

fun Controller.colorRes(@ColorRes colorRes: Int): Int =
    ContextCompat.getColor(activity!!, colorRes)

fun Controller.intRes(@IntegerRes res: Int): Int =
    resources!!.getInteger(res)

val Controller.shortAnimTime: Long
    get() = resources!!.getInteger(android.R.integer.config_shortAnimTime).toLong()

val Controller.mediumAnimTime: Long
    get() = resources!!.getInteger(android.R.integer.config_mediumAnimTime).toLong()

val Controller.longAnimTime: Long
    get() = resources!!.getInteger(android.R.integer.config_longAnimTime).toLong()

fun Controller.showBackButton() {
    (activity!! as MainActivity).showBackButton()
}

fun Controller.pushWithRootRouter(transaction: RouterTransaction) =
    (activity!! as MainActivity).pushWithRootRouter(transaction)

val Controller.rootRouter get() = (activity!! as MainActivity).rootRouter

fun Controller.attrData(@AttrRes attributeRes: Int) =
    TypedValue().let {
        activity!!.theme.resolveAttribute(attributeRes, it, true)
        it.data
    }

fun Controller.attrResourceId(@AttrRes attributeRes: Int) =
    TypedValue().let {
        activity!!.theme.resolveAttribute(attributeRes, it, true)
        it.resourceId
    }

var Controller.toolbarTitle: String
    set(value) {
        (activity!! as MainActivity).supportActionBar!!.title = value
    }
    get() = (activity!! as MainActivity).supportActionBar!!.title.toString()

val Controller.toolbar: Toolbar
    get() = activity!!.findViewById(R.id.toolbar)

fun Controller.setToolbar(toolbar: Toolbar) {
    (activity!! as MainActivity).setSupportActionBar(toolbar)
}

fun Controller.addToolbarView(@LayoutRes viewLayout: Int): View =
    activity!!.layoutInflater.inflate(viewLayout, toolbar, false).also {
        toolbar.addView(it)
        return it
    }

fun Controller.removeToolbarView(view: View) {
    toolbar.removeView(view)
}

fun Controller.showShortToast(@StringRes text: Int) {
    Toast.makeText(activity!!, text, Toast.LENGTH_SHORT).show()
}

fun Controller.enterFullScreen() {
    (activity as MainActivity).enterFullScreen()
}

fun Controller.exitFullScreen() {
    (activity as MainActivity).exitFullScreen()
}

fun Controller.setChildController(view: ViewGroup, controller: Controller) {
    val childRouter = getChildRouter(view)

    if (!childRouter.hasRootController()) {
        childRouter.setRoot(RouterTransaction.with(controller))
    }
}