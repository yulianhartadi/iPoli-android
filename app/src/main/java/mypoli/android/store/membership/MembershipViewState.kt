package mypoli.android.store.membership

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.player.Membership
import mypoli.android.store.membership.MembershipViewState.StateType.*
import mypoli.android.store.purchase.SubscriptionManager

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/16/18.
 */
sealed class MembershipAction : Action {
    data class Load(val subscriptionManager: SubscriptionManager) : MembershipAction()
    data class Loaded(
        val monthlyPrice: String,
        val yearlyPrice: String,
        val quarterlyPrice: String,
        val activeSkus: Set<String>
    ) : MembershipAction()

    data class SelectPlan(val plan: MembershipPlan) : MembershipAction()
    data class GoPremium(val plan: MembershipPlan, val activeSkus: Set<String>) : MembershipAction()
    data class Subscribed(val plan: MembershipPlan, val activeSkus: Set<String>) :
        MembershipAction()
    object SubscriptionError : MembershipAction()
}

object MembershipReducer : BaseViewStateReducer<MembershipViewState>() {

    override val stateKey = key<MembershipViewState>()

    override fun reduce(
        state: AppState,
        subState: MembershipViewState,
        action: Action
    ): MembershipViewState {
        return when (action) {
            is MembershipAction.Loaded -> {
                val currentPlan = state.dataState.player?.let {
                    when (state.dataState.player.membership) {
                        Membership.MONTHLY -> MembershipPlan.MONTHLY
                        Membership.YEARLY -> MembershipPlan.YEARLY
                        Membership.QUARTERLY -> MembershipPlan.QUARTERLY
                        Membership.NONE -> null
                    }
                }

                subState.copy(
                    type = DATA_CHANGED,
                    currentPlan = currentPlan,
                    showCurrentPlan = currentPlan != null && currentPlan == subState.selectedPlan,
                    monthlyPlanPrice = action.monthlyPrice,
                    yearlyPlanPrice = action.yearlyPrice,
                    quarterlyPlanPrice = action.quarterlyPrice,
                    activeSkus = action.activeSkus
                )
            }

            is MembershipAction.GoPremium -> {
                subState.copy(
                    type = SUBCRIPTON_IN_PROGRESS
                )
            }

            is MembershipAction.SelectPlan -> {
                subState.copy(
                    type = DATA_CHANGED,
                    selectedPlan = action.plan,
                    showCurrentPlan = action.plan == subState.currentPlan
                )
            }

            is MembershipAction.Subscribed -> {
                subState.copy(
                    type = MembershipViewState.StateType.SUBSCRIBED,
                    currentPlan = action.plan,
                    showCurrentPlan = action.plan == subState.selectedPlan,
                    activeSkus = action.activeSkus
                )
            }

            is MembershipAction.SubscriptionError -> {
                subState.copy(
                    type = SUBSCRIPTION_ERROR
                )
            }
            else -> subState
        }
    }

    override fun defaultState() = MembershipViewState(
        type = LOADING,
        selectedPlan = MembershipPlan.YEARLY,
        currentPlan = null,
        showCurrentPlan = false,
        monthlyPlanPrice = "0.00",
        quarterlyPlanPrice = "0.00",
        yearlyPlanPrice = "0.00",
        activeSkus = setOf()
    )

}

enum class MembershipPlan(val sku: String) {
    MONTHLY("test_sub"),
    YEARLY("test_sub_yearly"),
    QUARTERLY("test_sub")
}

data class MembershipViewState(
    val type: StateType,
    val selectedPlan: MembershipPlan,
    val currentPlan: MembershipPlan?,
    val showCurrentPlan: Boolean,
    val monthlyPlanPrice: String,
    val quarterlyPlanPrice: String,
    val yearlyPlanPrice: String,
    val activeSkus: Set<String>
) : ViewState {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SUBCRIPTON_IN_PROGRESS,
        SUBSCRIBED,
        SUBSCRIPTION_ERROR
    }
}
