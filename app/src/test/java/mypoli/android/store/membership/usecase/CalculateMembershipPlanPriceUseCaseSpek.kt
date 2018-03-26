package mypoli.android.store.membership.usecase

import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/22/18.
 */
class CalculateMembershipPlanPriceUseCaseSpek : Spek({
    describe("CalculateMembershipPlanPriceUseCase") {

        fun executeUseCase(amount: Long, currency: String, months: Int) =
            CalculateMembershipPlanPriceUseCase().execute(
                CalculateMembershipPlanPriceUseCase.Params(
                    amount,
                    currency,
                    months
                )
            )

        it("should calculate for 1 month") {
            val result = executeUseCase(7990000, "USD", 1)
            result.`should equal`("7.99USD")
        }

        it("should calculate for 10 month") {
            val result = executeUseCase(10000000, "USD", 10)
            result.`should equal`("1.0USD")
        }
    }
})