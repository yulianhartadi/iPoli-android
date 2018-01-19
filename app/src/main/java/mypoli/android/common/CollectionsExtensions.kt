package mypoli.android.common

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/19/18.
 */
fun <T> Iterable<T>.sumByLong(selector: (T) -> Long) = map { selector(it) }.sum()