package com.ivy.planned.list

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.IntervalType
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.design.l0_system.Purple
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.datamodel.PlannedPaymentRule
import com.ivy.legacy.utils.timeNowUTC
import com.ivy.navigation.EditPlannedScreen
import com.ivy.navigation.PlannedPaymentsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Orange
import kotlinx.collections.immutable.persistentListOf
import java.time.ZoneOffset
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.PlannedPaymentsScreen(screen: PlannedPaymentsScreen) {
    val viewModel: PlannedPaymentsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: PlannedPaymentsScreenState,
    onEvent: (PlannedPaymentsScreenEvent) -> Unit = {}
) {
    PlannedPaymentsLazyColumn(
        Header = {
            Spacer(Modifier.height(32.dp))

            Text(
                modifier = Modifier.padding(start = 24.dp),
                text = stringResource(R.string.planned_payments_inline),
                style = UI.typo.h2.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )

            Spacer(Modifier.height(24.dp))
        },
        currency = state.currency,
        categories = state.categories,
        accounts = state.accounts,
        oneTime = state.oneTimePlannedPayment,
        oneTimeIncome = state.oneTimeIncome,
        oneTimeExpenses = state.oneTimeExpenses,
        recurring = state.recurringPlannedPayment,
        recurringIncome = state.recurringIncome,
        recurringExpenses = state.recurringExpenses,
        oneTimeExpanded = state.isOneTimePaymentsExpanded,
        recurringExpanded = state.isRecurringPaymentsExpanded,
        setOneTimeExpanded = {
            onEvent(PlannedPaymentsScreenEvent.OnOneTimePaymentsExpanded(it))
        },
        setRecurringExpanded = {
            onEvent(PlannedPaymentsScreenEvent.OnRecurringPaymentsExpanded(it))
        },
        listState = rememberScrollPositionListState(key = "plannedPayments")
    )

    val nav = navigation()
    PlannedPaymentsBottomBar(
        onClose = {
            nav.back()
        },
        onAdd = {
            nav.navigateTo(
                EditPlannedScreen(
                    type = TransactionType.EXPENSE,
                    plannedPaymentRuleId = null
                )
            )
        }
    )
}

@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        val account = Account(name = "Cash", isVisible = true, color = Green.toArgb())
        val food = Category(
            name = NotBlankTrimmedString.unsafe("Food"),
            color = ColorInt(Purple.toArgb()),
            icon = null,
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )
        val shisha = Category(
            name = NotBlankTrimmedString.unsafe("Shisha"),
            color = ColorInt(Orange.toArgb()),
            icon = null,
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )

        UI(
            PlannedPaymentsScreenState(
                currency = "BGN",
                accounts = persistentListOf(account),
                categories = persistentListOf(food, shisha),
                oneTimePlannedPayment = persistentListOf(
                    PlannedPaymentRule(
                        accountId = account.id,
                        title = "Lidl pazar",
                        categoryId = food.id.value,
                        amount = 250.75,
                        startDate = timeNowUTC().plusDays(5).toInstant(ZoneOffset.UTC),
                        oneTime = true,
                        intervalType = null,
                        intervalN = null,
                        type = TransactionType.EXPENSE
                    )
                ),
                oneTimeExpenses = 250.75,
                oneTimeIncome = 0.0,
                recurringPlannedPayment = persistentListOf(
                    PlannedPaymentRule(
                        accountId = account.id,
                        title = "Tabu",
                        categoryId = shisha.id.value,
                        amount = 1025.5,
                        startDate = timeNowUTC().plusDays(5).toInstant(ZoneOffset.UTC),
                        oneTime = false,
                        intervalType = IntervalType.MONTH,
                        intervalN = 1,
                        type = TransactionType.EXPENSE
                    )
                ),
                recurringExpenses = 1025.5,
                recurringIncome = 0.0,
                isOneTimePaymentsExpanded = true,
                isRecurringPaymentsExpanded = true
            )
        )
    }
}
