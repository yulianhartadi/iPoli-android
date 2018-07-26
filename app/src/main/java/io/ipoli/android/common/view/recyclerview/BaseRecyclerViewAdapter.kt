package io.ipoli.android.common.view.recyclerview

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapter
import android.support.annotation.LayoutRes
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

interface RecyclerViewViewModel {
    val id: String
}

data class SimpleRecyclerViewViewModel<VM>(val value: VM) : RecyclerViewViewModel {
    override val id: String
        get() = value.toString()
}

private class DiffCallback<VM : RecyclerViewViewModel> : DiffUtil.ItemCallback<VM>() {
    override fun areItemsTheSame(oldItem: VM?, newItem: VM?) = oldItem?.id == newItem?.id

    override fun areContentsTheSame(oldItem: VM?, newItem: VM?) =
        oldItem == newItem
}

abstract class BaseRecyclerViewAdapter<VM : RecyclerViewViewModel>(
    @LayoutRes private val itemLayout: Int
) : ListAdapter<VM, SimpleViewHolder>(DiffCallback<VM>()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        SimpleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                itemLayout,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        onBindViewModel(getItem(holder.adapterPosition), holder.itemView, holder)
    }

    abstract fun onBindViewModel(vm: VM, view: View, holder: SimpleViewHolder)

    fun updateAll(viewModels: List<VM>) {
        submitList(viewModels.toMutableList())
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

    fun getItemAt(position: Int): VM = getItem(position)

    val items: MutableList<VM>
        get() = 0.until(itemCount).map {
            getItem(it)
        }.toMutableList()

}

abstract class MultiViewRecyclerViewAdapter<VM : RecyclerViewViewModel> :
    ListAdapter<VM, SimpleViewHolder>(DiffCallback<VM>()) {

    val viewTypeToItemBinder = mutableMapOf<Int, ItemBinder>()
    val itemTypeToViewType = mutableMapOf<Class<*>, Int>()

    inline fun <reified ITEM> registerBinder(
        viewType: Int, @LayoutRes layoutRes: Int,
        crossinline binder: (ITEM, View, SimpleViewHolder) -> Unit
    ) {

        itemTypeToViewType[ITEM::class.java] = viewType

        viewTypeToItemBinder[viewType] = object : ItemBinder {

            override val layoutResId: Int
                get() = layoutRes

            @Suppress("UNCHECKED_CAST")
            override fun <T> bind(item: T, view: View, holder: SimpleViewHolder) {
                binder(item as ITEM, view, holder)
            }
        }
    }

    fun updateAll(items: List<VM>) {
        submitList(items.toMutableList())
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
        viewTypeToItemBinder[viewType]?.bind(getItem(position), holder.itemView, holder)
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

        fun <T> bind(item: T, view: View, holder: SimpleViewHolder)

    }

    @PublishedApi
    internal fun `access$getItem`(position: Int) = getItem(position)
}

abstract class MultiViewPagedRecyclerViewAdapter<VM : RecyclerViewViewModel> :
    PagedListAdapter<VM, SimpleViewHolder>(DiffCallback<VM>()) {

    val viewTypeToItemBinder = mutableMapOf<Int, MultiViewPagedRecyclerViewAdapter.ItemBinder>()
    val itemTypeToViewType = mutableMapOf<Class<*>, Int>()

    inline fun <reified ITEM> registerBinder(
        viewType: Int, @LayoutRes layoutRes: Int,
        crossinline binder: (ITEM, View, SimpleViewHolder) -> Unit
    ) {

        itemTypeToViewType[ITEM::class.java] = viewType

        viewTypeToItemBinder[viewType] = object : ItemBinder {

            override val layoutResId: Int
                get() = layoutRes

            @Suppress("UNCHECKED_CAST")
            override fun <T> bind(item: T, view: View, holder: SimpleViewHolder) {
                binder(item as ITEM, view, holder)
            }
        }
    }

    fun updateAll(items: PagedList<VM>) {
        submitList(items)
    }

    inline fun <reified ITEM> getItemAt(position: Int): ITEM {
        val item = `access$getItemAtPosition`(position)
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
        viewTypeToItemBinder[viewType]?.bind(getItem(position), holder.itemView, holder)
    }

    override fun getItemViewType(position: Int): Int {

        if (viewTypeToItemBinder.isEmpty()) {
            onRegisterItemBinders()
        }
        return itemTypeToViewType[`access$getItemAtPosition`(position).javaClass]!!
    }

    abstract fun onRegisterItemBinders()

    interface ItemBinder {

        @get:LayoutRes
        val layoutResId: Int

        fun <T> bind(item: T, view: View, holder: SimpleViewHolder)

    }

    @PublishedApi
    internal fun `access$getItemAtPosition`(position: Int): VM = getItem(position)!!
}