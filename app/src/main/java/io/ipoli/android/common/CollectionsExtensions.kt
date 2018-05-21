package io.ipoli.android.common

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/19/18.
 */
fun <T> Iterable<T>.sumByLong(selector: (T) -> Long) = map { selector(it) }.sum()

fun <T> Iterable<T>.replace(filter: (T) -> Boolean, transform: (T) -> T) =
    map { if (filter(it)) transform(it) else it }