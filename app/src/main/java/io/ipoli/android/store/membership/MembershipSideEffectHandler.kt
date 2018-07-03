package io.ipoli.android.store.membership

import com.crashlytics.android.Crashlytics
import io.ipoli.android.BuildConfig
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.api.Api
import io.ipoli.android.common.datetime.startOfDayUTC
import io.ipoli.android.common.redux.Action
import io.ipoli.android.store.membership.error.SubscriptionError
import io.ipoli.android.store.membership.usecase.CalculateMembershipPlanPriceUseCase
import io.ipoli.android.store.membership.usecase.UpdatePlayerMembershipUseCase
import space.traversal.kapsule.required
import timber.log.Timber

/**i
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/16/18.
 */
object MembershipSideEffectHandler : AppSideEffectHandler() {
    private val updatePlayerMembershipUseCase by required { updatePlayerMembershipUseCase }
    private val calculateMembershipPlanPriceUseCase by required { calculateMembershipPlanPriceUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is MembershipAction.Load -> {
                    dispatch(
                        MembershipAction.Loaded(
                            monthlyPrice = calculateMembershipPlanPriceUseCase.execute(
                                CalculateMembershipPlanPriceUseCase.Params(
                                    action.monthlyPrice.amount,
                                    action.monthlyPrice.currency,
                                    1
                                )
                            ),
                            yearlyPrice = calculateMembershipPlanPriceUseCase.execute(
                                CalculateMembershipPlanPriceUseCase.Params(
                                    action.yearlyPrice.amount,
                                    action.yearlyPrice.currency,
                                    12
                                )
                            ),
                            quarterlyPrice = calculateMembershipPlanPriceUseCase.execute(
                                CalculateMembershipPlanPriceUseCase.Params(
                                    action.quarterlyPrice.amount,
                                    action.quarterlyPrice.currency,
                                    3
                                )
                            ),
                            activeSku = action.activeSku
                        )
                    )
            }

            is MembershipAction.Subscribed -> {
                val plan = MembershipPlan.values().first { it.sku == action.sku }
                try {
                    val status = Api.getMembershipStatus(action.sku, action.purchaseToken)
                    updatePlayerMembershipUseCase.execute(
                        UpdatePlayerMembershipUseCase.Params(
                            plan = plan,
                            purchasedDate = status.startDate,
                            expirationDate = status.expirationDate
                        )
                    )
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Timber.e(e)
                    } else {
                        Crashlytics.logException(
                            SubscriptionError("Subscription error", e)
                        )
                    }
                    updatePlayerMembershipUseCase.execute(
                        UpdatePlayerMembershipUseCase.Params(
                            plan = plan,
                            purchasedDate = action.purchaseTime.startOfDayUTC
                        )
                    )
                }
            }
        }
    }

    override fun canHandle(action: Action) = action is MembershipAction
}