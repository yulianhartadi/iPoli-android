package mypoli.android.common.view.recyclerview

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

abstract class BaseRecyclerViewAdapter<in VM>(
    @LayoutRes private val itemLayout: Int,
    private var viewModels: List<VM>
) : RecyclerView.Adapter<SimpleViewHolder>() {

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
        onBindViewModel(viewModels[position], holder.itemView)
    }

    abstract fun onBindViewModel(vm: VM, view: View)

    override fun getItemCount() = viewModels.size

    fun updateAll(viewModels: List<VM>) {
        this.viewModels = viewModels
        notifyDataSetChanged()
    }
}

abstract class MultiViewRecyclerViewAdapter : RecyclerView.Adapter<SimpleViewHolder>() {
    private val items = mutableListOf<Any>()

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

    fun <T : Any> updateAll(items: List<T>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun <T : Any> getItemAt(index: Int, clazz: KClass<T>): T {
        val item = items[index]
        if (!(item.javaClass.isAssignableFrom(clazz.java))) {
            throw ClassCastException("item at index is not assignable from $clazz")
        }
        @Suppress("UNCHECKED_CAST") return item as T
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
        viewTypeToItemBinder[viewType]?.bind(items[position], holder.itemView)
    }

    override fun getItemViewType(position: Int): Int {
        if (viewTypeToItemBinder.isEmpty()) {
            onRegisterItemBinders()
        }
        return itemTypeToViewType[items[position].javaClass]!!
    }

    abstract fun onRegisterItemBinders()

    override fun getItemCount() = items.size

    interface ItemBinder {

        @get:LayoutRes
        val layoutResId: Int

        fun <T> bind(item: T, view: View)

    }
}