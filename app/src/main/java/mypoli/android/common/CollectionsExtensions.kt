package mypoli.android.common

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/19/18.
 */
fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}