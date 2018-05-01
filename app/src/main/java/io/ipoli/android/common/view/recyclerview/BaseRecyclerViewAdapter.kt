package io.ipoli.android.common.view.recyclerview

import android.support.annotation.LayoutRes
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import timber.log.Timber

private class DiffCallback<VM> : DiffUtil.ItemCallback<VM>() {
    override fun areItemsTheSame(oldItem: VM?, newItem: VM?) = oldItem == newItem

    override fun areContentsTheSame(oldItem: VM?, newItem: VM?) =
        oldItem == newItem
}

abstract class BaseRecyclerViewAdapter<VM>(
    @LayoutRes private val itemLayout: Int
) : ListAdapter<VM, SimpleViewHolder>(DiffCallback<VM>()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimpleViewHolder =
        SimpleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                itemLayout,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
//        onBindViewModel(getItem(holder.adapterPosition), holder.itemView, holder)
        Timber.d("BBB ${holder.adapterPosition} $position")
        onBindViewModel(getItem(holder.adapterPosition), holder.itemView, holder)
    }

    abstract fun onBindViewModel(vm: VM, view: View, holder: SimpleViewHolder)

    fun updateAll(viewModels: List<VM>) {
        submitList(null)
        submitList(viewModels)
    }

    fun add(viewModel: VM) {
        val vms = items
        vms.add(viewModel)
        updateAll(vms)
    }

    fun move(oldPosition: Int, newPosition: Int) {
        val vms = items
        val vm = vms[oldPosition]
        vms.removeAt(oldPosition)
        vms.add(newPosition, vm)
        updateAll(vms)
    }

    fun removeAt(position: Int) {
        val vms = items
        vms.removeAt(position)
        updateAll(vms)
    }

    public val items: MutableList<VM>
        get() = 0.until(itemCount).map {
            getItem(it)
        }.toMutableList()

}

abstract class MultiViewRecyclerViewAdapter :
    ListAdapter<Any, SimpleViewHolder>(DiffCallback<Any>()) {

    val viewTypeToItemBinder = mutableMapOf<Int, ItemBinder>()
    val itemTypeToViewType = mutableMapOf<Class<*>, Int>()

    inline fun <reified ITEM> registerBinder(
        viewType: Int, @LayoutRes layoutRes: Int,
        crossinline binder: (ITEM, View) -> Unit
    ) {

        itemTypeToViewType[ITEM::class.java] = viewType

        viewTypeToItemBinder[viewType] = object : ItemBinder {

            override val layoutResId: Int
                get() = layoutRes

            @Suppress("UNCHECKED_CAST")
            override fun <T> bind(item: T, view: View) {
                binder(item as ITEM, view)
            }
        }
    }

    fun updateAll(items: List<*>) {
        submitList(items)
    }

    inline fun <reified ITEM> getItemAt(position: Int): ITEM {
        val item = `access$getItem`(position)
        if (!(item.javaClass.isAssignableFrom(ITEM::class.java))) {
            throw ClassCastException("item at index is not assignable from ${ITEM::class.java}")
        }
        @Suppress("UNCHECKED_CAST") return item as ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val resId = viewTypeToItemBinder[viewType]?.layoutResId
            ?: throw IllegalArgumentException(
                "Unknown view type $viewType. Have you used registerBinder() for it?"
            )
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(resId, parent, false)
        return SimpleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        viewTypeToItemBinder[viewType]?.bind(getItem(position), holder.itemView)
    }

    override fun getItemViewType(position: Int): Int {
        if (viewTypeToItemBinder.isEmpty()) {
            onRegisterItemBinders()
        }
        return itemTypeToViewType[getItem(position).javaClass]!!
    }

    abstract fun onRegisterItemBinders()

    interface ItemBinder {

        @get:LayoutRes
        val layoutResId: Int

        fun <T> bind(item: T, view: View)

    }

    @PublishedApi
    internal fun `access$getItem`(position: Int) = getItem(position)
}