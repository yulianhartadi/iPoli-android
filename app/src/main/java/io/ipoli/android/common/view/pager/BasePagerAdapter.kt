package io.ipoli.android.common.view.pager

import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class BasePagerAdapter<VM>() : PagerAdapter() {

    private val items: MutableList<VM> = mutableListOf()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val vm = items[position]
        val vmRes = layoutResourceFor(vm)
        val view = inflater.inflate(vmRes, container, false)
        bindItem(vm, view)
        container.addView(view)
        return view
    }

    abstract fun layoutResourceFor(item: VM): Int

    abstract fun bindItem(item: VM, view: View)

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) =
        container.removeView(view as View)

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun getCount() = items.size

    override fun getItemPosition(`object`: Any) = POSITION_NONE

    fun itemAt(position: Int): VM {
        return items[position]
    }

    fun updateAll(items: List<VM>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}