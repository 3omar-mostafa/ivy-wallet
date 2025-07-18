package com.ivy.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Theme
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.data.model.AccountData
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.horizontalSwipeListener
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.navigation.TransactionsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.GreenLight
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.BalanceRowMini
import com.ivy.wallet.ui.theme.components.CircleButtonFilled
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.ReorderButton
import com.ivy.wallet.ui.theme.components.ReorderModalSingleType
import com.ivy.wallet.ui.theme.dynamicContrast
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.toComposeColor
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.AccountsTab() {
    val viewModel: AccountsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: AccountsState,
    onEvent: (AccountsEvent) -> Unit = {}
) {
    val nav = navigation()
    val ivyContext = com.ivy.legacy.ivyWalletCtx()
    var listState = rememberLazyListState()
    if (state.accountsData.any { it.account.isVisible }) {
        listState = rememberScrollPositionListState(
            key = "accounts_lazy_column",
            initialFirstVisibleItemIndex = ivyContext.accountsListState?.firstVisibleItemIndex ?: 0,
            initialFirstVisibleItemScrollOffset = ivyContext.accountsListState?.firstVisibleItemScrollOffset
                ?: 0
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .horizontalSwipeListener(
                sensitivity = 200,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    ivyContext.selectMainTab(com.ivy.legacy.data.model.MainTab.HOME)
                },
                onSwipeRight = {
                    ivyContext.selectMainTab(com.ivy.legacy.data.model.MainTab.HOME)
                }
            ),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(24.dp))

                Column {
                    Text(
                        text = stringResource(R.string.accounts),
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                HideAccountsButton(modifier = Modifier.size(size = 48.dp)) {
                    onEvent(
                        AccountsEvent.OnHideModalVisible(hideVisible = true)
                    )
                }

                Spacer(Modifier.width(16.dp))

                ReorderButton {
                    onEvent(
                        AccountsEvent.OnReorderModalVisible(reorderVisible = true)
                    )
                }

                Spacer(Modifier.width(24.dp))
            }
            if (!state.hideTotalBalance) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    IncomeExpensesRow(
                        currency = state.baseCurrency,
                        incomeLabel = stringResource(id = R.string.total_balance),
                        income = state.totalBalanceWithoutExcluded.toDoubleOrNull() ?: 0.00,
                        expensesLabel = stringResource(id = R.string.total_balance_excluded),
                        expenses = state.totalBalanceWithExcluded.toDoubleOrNull() ?: 0.00
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        items(state.accountsData.filter { it.account.isVisible }) {
            Spacer(Modifier.height(16.dp))
            AccountCard(
                baseCurrency = state.baseCurrency,
                accountData = it,
                compactModeEnabled = state.compactAccountsModeEnabled,
                onBalanceClick = {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = it.account.id.value,
                            categoryId = null
                        )
                    )
                }
            ) {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = it.account.id.value,
                        categoryId = null
                    )
                )
            }
        }

        item {
            Spacer(Modifier.height(150.dp)) // scroll hack
        }
    }

    HideAccountsModal(
        visible = state.hideVisible,
        initialItems = state.accountsData,
        onDismiss = { onEvent(AccountsEvent.OnHideModalVisible(hideVisible = false)) },
        onComplete = { onEvent(AccountsEvent.OnVisibilityUpdate(updatedList = it)) }
    )
    ReorderModalSingleType(
        visible = state.reorderVisible,
        initialItems = state.accountsData,
        dismiss = {
            onEvent(AccountsEvent.OnReorderModalVisible(reorderVisible = false))
        },
        onReordered = {
            onEvent(AccountsEvent.OnReorder(reorderedList = it))
        }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.account.name.value,
            style = UI.typo.b1.style(
                color = item.account.color.value.toComposeColor(),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun AccountCard(
    baseCurrency: String,
    accountData: AccountData,
    compactModeEnabled: Boolean,
    onBalanceClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .border(2.dp, UI.colors.medium, UI.shapes.r4)
            .clickable(
                onClick = onClick
            )
    ) {
        val account = accountData.account
        val contrastColor = findContrastTextColor(account.color.value.toComposeColor())
        val currency = account.asset.code

        AccountHeader(
            accountData = accountData,
            currency = currency,
            baseCurrency = baseCurrency,
            contrastColor = contrastColor,
            onBalanceClick = onBalanceClick
        )

        if (!compactModeEnabled) {
            Spacer(Modifier.height(12.dp))

            IncomeExpensesRow(
                currency = currency,
                incomeLabel = stringResource(R.string.month_income),
                income = accountData.monthlyIncome,
                expensesLabel = stringResource(R.string.month_expenses),
                expenses = accountData.monthlyExpenses
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AccountHeader(
    accountData: AccountData,
    currency: String,
    baseCurrency: String,
    contrastColor: Color,
    onBalanceClick: () -> Unit
) {
    val account = accountData.account

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(account.color.value.toComposeColor(), UI.shapes.r4Top)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(20.dp))

            ItemIconSDefaultIcon(
                iconName = account.icon?.id,
                defaultIcon = R.drawable.ic_custom_account_s,
                tint = contrastColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = account.name.value,
                style = UI.typo.b1.style(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            if (!account.includeInBalance) {
                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.excluded),
                    style = UI.typo.c.style(
                        color = account.color.value.toComposeColor().dynamicContrast()
                    )
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        BalanceRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickableNoIndication(rememberInteractionSource()) {
                    onBalanceClick()
                },
            textColor = contrastColor,
            currency = currency,
            balance = accountData.balance,

            balanceFontSize = 30.sp,
            currencyFontSize = 30.sp,

            currencyUpfront = false
        )

        if (currency != baseCurrency && accountData.balanceBaseCurrency != null) {
            BalanceRowMini(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickableNoIndication(rememberInteractionSource()) {
                        onBalanceClick()
                    }
                    .testTag("baseCurrencyEquivalent"),
                textColor = account.color.value.toComposeColor().dynamicContrast(),
                currency = baseCurrency,
                balance = accountData.balanceBaseCurrency!!,
                currencyUpfront = false
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun PreviewAccountsTabCompactModeDisabled(theme: Theme = Theme.LIGHT) {
    IvyWalletPreview(theme = theme) {
        val acc1 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Phyre"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )

        val acc2 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("DSK"),
            color = ColorInt(GreenLight.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )

        val acc3 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Revolut"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("revolut"),
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )

        val acc4 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Cash"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("cash"),
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )
        val state = AccountsState(
            baseCurrency = "BGN",
            accountsData = persistentListOf(
                AccountData(
                    account = acc1,
                    balance = 2125.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 920.0,
                    monthlyIncome = 3045.0
                ),
                AccountData(
                    account = acc2,
                    balance = 12125.21,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 1350.50,
                    monthlyIncome = 8000.48
                ),
                AccountData(
                    account = acc3,
                    balance = 1200.0,
                    balanceBaseCurrency = 1979.64,
                    monthlyExpenses = 750.0,
                    monthlyIncome = 1000.30
                ),
                AccountData(
                    account = acc4,
                    balance = 820.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 340.0,
                    monthlyIncome = 400.0
                ),
            ),
            totalBalanceWithExcluded = "25.54",
            totalBalanceWithExcludedText = "BGN 25.54",
            totalBalanceWithoutExcluded = "25.54",
            totalBalanceWithoutExcludedText = "BGN 25.54",
            reorderVisible = false,
            hideVisible = false,
            compactAccountsModeEnabled = false,
            hideTotalBalance = false
        )
        UI(state = state)
    }
}

@Preview
@Composable
private fun PreviewAccountsTabCompactModeEnabled(theme: Theme = Theme.LIGHT) {
    IvyWalletPreview(theme = theme) {
        val acc1 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Phyre"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )

        val acc2 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("DSK"),
            color = ColorInt(GreenLight.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )

        val acc3 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Revolut"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("revolut"),
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )

        val acc4 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Cash"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("cash"),
            includeInBalance = true,
            orderNum = 0.0,
            isVisible = true,
        )
        val state = AccountsState(
            baseCurrency = "BGN",
            accountsData = persistentListOf(
                AccountData(
                    account = acc1,
                    balance = 2125.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 920.0,
                    monthlyIncome = 3045.0
                ),
                AccountData(
                    account = acc2,
                    balance = 12125.21,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 1350.50,
                    monthlyIncome = 8000.48
                ),
                AccountData(
                    account = acc3,
                    balance = 1200.0,
                    balanceBaseCurrency = 1979.64,
                    monthlyExpenses = 750.0,
                    monthlyIncome = 1000.30
                ),
                AccountData(
                    account = acc4,
                    balance = 820.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 340.0,
                    monthlyIncome = 400.0
                ),
            ),
            totalBalanceWithExcluded = "25.54",
            totalBalanceWithExcludedText = "BGN 25.54",
            totalBalanceWithoutExcluded = "25.54",
            totalBalanceWithoutExcludedText = "BGN 25.54",
            reorderVisible = false,
            hideVisible = false,
            compactAccountsModeEnabled = true,
            hideTotalBalance = false
        )
        UI(state = state)
    }
}

@Composable
fun HideAccountsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CircleButtonFilled(
        modifier = modifier,
        icon = R.drawable.ic_hide_m,
        onClick = onClick
    )
}

/** For screen shot testing **/
@Composable
fun AccountsTabNonCompactUITest(dark: Boolean) {
    val theme = when (dark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    PreviewAccountsTabCompactModeDisabled(theme)
}

/** For screen shot testing **/
@Composable
fun AccountsTabCompactUITest(dark: Boolean) {
    val theme = when (dark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    PreviewAccountsTabCompactModeEnabled(theme)
}