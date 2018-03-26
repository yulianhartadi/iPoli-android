package io.ipoli.android.store.membership.usecase

import io.ipoli.android.common.UseCase

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/22/18.
 */
class CalculateMembershipPlanPriceUseCase :
    UseCase<CalculateMembershipPlanPriceUseCase.Params, String> {

    companion object {
        const val MICRO_UNIT = 1000000
    }

    override fun execute(parameters: Params): String {
        return "${calculatePricePerMonth(
            parameters.amount,
            parameters.months
        )}${parameters.currency}"
    }

    private fun calculatePricePerMonth(price: Long, months: Int): Double {
        val pricePerMonth = price / months.toDouble()
        val x = (pricePerMonth / (MICRO_UNIT / 100)).toInt()
        return x / 100.0
    }

    data class Params(val amount: Long, val currency: String, val months: Int)
}