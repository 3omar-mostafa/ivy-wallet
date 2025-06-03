package com.ivy.balance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.legacy.data.model.FromToTimeRange
import com.ivy.legacy.data.model.Month
import com.ivy.ui.ComposeViewModel
import com.ivy.legacy.data.model.TimePeriod
import com.ivy.legacy.utils.atEndOfDay
import com.ivy.legacy.utils.ioThread
import com.ivy.legacy.utils.withDayOfMonthSafe
import com.ivy.wallet.domain.action.settings.BaseCurrencyAct
import com.ivy.wallet.domain.action.wallet.CalcWalletBalanceAct
import com.ivy.wallet.domain.deprecated.logic.PlannedPaymentsLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class BalanceViewModel @Inject constructor(
    private val plannedPaymentsLogic: PlannedPaymentsLogic,
    private val ivyContext: com.ivy.legacy.IvyWalletCtx,
    private val baseCurrencyAct: BaseCurrencyAct,
    private val calcWalletBalanceAct: CalcWalletBalanceAct,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
) : ComposeViewModel<BalanceState, BalanceEvent>() {

    private var period by mutableStateOf(ivyContext.selectedPeriod)
    private var baseCurrencyCode by mutableStateOf("")
    private var currentBalance by mutableDoubleStateOf(0.0)
    private var plannedPaymentsAmount by mutableDoubleStateOf(0.0)
    private var balanceAfterPlannedPayments by mutableDoubleStateOf(0.0)

    @Composable
    override fun uiState(): BalanceState {
        LaunchedEffect(Unit) {
            start(timePeriod = fixTimePeriod(ivyContext.selectedPeriod, 0))
        }

        return BalanceState(
            period = period,
            balanceAfterPlannedPayments = balanceAfterPlannedPayments,
            currentBalance = currentBalance,
            baseCurrencyCode = baseCurrencyCode,
            plannedPaymentsAmount = plannedPaymentsAmount
        )
    }

    override fun onEvent(event: BalanceEvent) {
        when (event) {
            is BalanceEvent.OnNextMonth -> nextMonth()
            is BalanceEvent.OnSetPeriod -> setTimePeriod(event.timePeriod)
            is BalanceEvent.OnPreviousMonth -> previousMonth()
        }
    }

    private fun start(
        timePeriod: TimePeriod
    ) {
        viewModelScope.launch {
            baseCurrencyCode = baseCurrencyAct(Unit)
            period = timePeriod

            currentBalance = calcWalletBalanceAct(
                CalcWalletBalanceAct.Input(baseCurrencyCode)
            ).toDouble()

            plannedPaymentsAmount = ioThread {
                val range = timePeriod.fromToRange ?: timePeriod.toRange(ivyContext.startDayOfMonth, timeConverter, timeProvider)
                plannedPaymentsLogic.plannedPaymentsAmountFor(range)
            }
            balanceAfterPlannedPayments =
                currentBalance + plannedPaymentsAmount

            if (period.month != null) {
                period = TimePeriod(month = period.month, year = period.year)
            }
        }
    }

    private fun setTimePeriod(timePeriod: TimePeriod) {
        start(timePeriod = fixTimePeriod(timePeriod, 0))
    }

    private fun nextMonth() {
        start(timePeriod = fixTimePeriod(period, 1))
    }

    private fun previousMonth() {
        start(timePeriod = fixTimePeriod(period, -1))
    }

    private fun fixTimePeriod(timePeriod: TimePeriod, plusMonths: Long): TimePeriod {
        var month = timePeriod.month
        var year = timePeriod.year ?: com.ivy.legacy.utils.dateNowUTC().year

        if (month != null) {
            val to = month.toDate().withYear(year)
                .plusMonths(plusMonths + 1)
                .withDayOfMonthSafe(1)
                .minusDays(1)
                .atEndOfDay()

            month = Month.fromMonthValue(to.monthValue)
            year = to.year

            val instant = to.atZone(timeProvider.getZoneId()).toInstant()
            return TimePeriod(fromToRange = FromToTimeRange(from = null, to = instant), month = month, year = year)
        }
        return timePeriod
    }
}
