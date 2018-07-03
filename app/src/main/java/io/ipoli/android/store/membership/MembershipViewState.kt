package io.ipoli.android.store.membership

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.store.membership.MembershipViewState.StateType.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/16/18.
 */
sealed class MembershipAction : Action {
    data class Load(
        val monthlyPrice: Price,
        val quarterlyPrice: Price,
        val yearlyPrice: Price,
        val activeSku: String?
    ) : MembershipAction()

    data class Loaded(
        val monthlyPrice: String,
        val yearlyPrice: String,
        val quarterlyPrice: String,
        val activeSku: String?
    ) : MembershipAction() {
        override fun toMap() = mapOf(
            "monthlyPrice" to monthlyPrice,
            "yearlyPrice" to yearlyPrice,
            "quarterlyPrice" to quarterlyPrice,
            "activeSku" to activeSku
        )
    }

    data class SelectPlan(val plan: MembershipPlan) : MembershipAction() {
        override fun toMap() = mapOf("plan" to plan)
    }

    data class Subscribed(
        val sku: String,
        val purchaseTime: Long,
        val purchaseToken: String
    ) :
        MembershipAction() {
        override fun toMap() = mapOf("plan" to sku, "purchaseTime" to purchaseTime)
    }
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
                val currentPlan = MembershipPlan.values().firstOrNull { it.sku == action.activeSku }
                subState.copy(
                    type = DATA_CHANGED,
                    currentPlan = currentPlan,
                    showCurrentPlan = currentPlan != null && currentPlan == subState.selectedPlan,
                    monthlyPlanPrice = action.monthlyPrice,
                    yearlyPlanPrice = action.yearlyPrice,
                    quarterlyPlanPrice = action.quarterlyPrice,
                    activeSku = action.activeSku
                )
            }

            is MembershipAction.Subscribed -> {
                val plan = MembershipPlan.values().first { it.sku == action.sku }
                subState.copy(
                    type = SUBSCRIBED,
                    currentPlan = plan,
                    showCurrentPlan = plan == subState.selectedPlan,
                    activeSku = action.sku
                )
            }

            is MembershipAction.SelectPlan -> {
                subState.copy(
                    type = DATA_CHANGED,
                    selectedPlan = action.plan,
                    showCurrentPlan = action.plan == subState.currentPlan
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
        activeSku = null
    )

}

data class Price(val amount: Long, val currency: String)

enum class MembershipPlan(val sku: String) {
    MONTHLY("monthly_plan_70_percent"),
    YEARLY("yearly_plan_70_percent"),
    QUARTERLY("quarterly_plan_70_percent")
}

data class MembershipViewState(
    val type: StateType,
    val selectedPlan: MembershipPlan,
    val currentPlan: MembershipPlan?,
    val showCurrentPlan: Boolean,
    val monthlyPlanPrice: String,
    val quarterlyPlanPrice: String,
    val yearlyPlanPrice: String,
    val activeSku: String?
) : BaseViewState() {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SUBSCRIBED
    }
}
