package com.ivy.wallet.domain.data

import android.icu.util.Currency
import androidx.compose.runtime.Immutable
import com.ivy.legacy.utils.getDefaultFIATCurrency

@Immutable
data class IvyCurrency(
    val code: String,
    val name: String,
    val isCrypto: Boolean
) {
    companion object {
        private const val CRYPTO_DECIMAL = 18
        private const val FIAT_DECIMAL = 2

        private val CRYPTO = setOf(
            IvyCurrency(
                code = "BTC",
                name = "Bitcoin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "ETH",
                name = "Ethereum",
                isCrypto = true
            ),
            IvyCurrency(
                code = "USDT",
                name = "Tether USD",
                isCrypto = true
            ),
            IvyCurrency(
                code = "BNB",
                name = "Binance Coin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "ADA",
                name = "Cardano",
                isCrypto = true
            ),
            IvyCurrency(
                code = "XRP",
                name = "Ripple",
                isCrypto = true
            ),
            IvyCurrency(
                code = "DOGE",
                name = "Dogecoin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "USDC",
                name = "USD Coin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "DOT",
                name = "Polkadot",
                isCrypto = true
            ),
            IvyCurrency(
                code = "UNI",
                name = "Uniswap",
                isCrypto = true
            ),
            IvyCurrency(
                code = "BUSD",
                name = "Binance USD",
                isCrypto = true
            ),
            IvyCurrency(
                code = "BCH",
                name = "Bitcoin Cash",
                isCrypto = true
            ),
            IvyCurrency(
                code = "SOL",
                name = "Solana",
                isCrypto = true
            ),
            IvyCurrency(
                code = "LTC",
                name = "Litecoin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "LINK",
                name = "ChainLink Token",
                isCrypto = true
            ),
            IvyCurrency(
                code = "SHIB",
                name = "Shiba Inu coin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "LUNA",
                name = "Terra",
                isCrypto = true
            ),
            IvyCurrency(
                code = "AVAX",
                name = "Avalanche",
                isCrypto = true
            ),
            IvyCurrency(
                code = "MATIC",
                name = "Polygon",
                isCrypto = true
            ),
            IvyCurrency(
                code = "CRO",
                name = "Cronos",
                isCrypto = true
            ),
            IvyCurrency(
                code = "WBTC",
                name = "Wrapped Bitcoin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "ALGO",
                name = "Algorand",
                isCrypto = true
            ),
            IvyCurrency(
                code = "XLM",
                name = "Stellar",
                isCrypto = true
            ),
            IvyCurrency(
                code = "MANA",
                name = "Decentraland",
                isCrypto = true
            ),
            IvyCurrency(
                code = "AXS",
                name = "Axie Infinity",
                isCrypto = true
            ),
            IvyCurrency(
                code = "DAI",
                name = "Dai",
                isCrypto = true
            ),
            IvyCurrency(
                code = "ICP",
                name = "Internet Computer",
                isCrypto = true
            ),
            IvyCurrency(
                code = "ATOM",
                name = "Cosmos",
                isCrypto = true
            ),
            IvyCurrency(
                code = "FIL",
                name = "Filecoin",
                isCrypto = true
            ),
            IvyCurrency(
                code = "ETC",
                name = "Ethereum Classic",
                isCrypto = true
            ),
            IvyCurrency(
                code = "DASH",
                name = "Dash",
                isCrypto = true
            ),
            IvyCurrency(
                code = "TRX",
                name = "Tron",
                isCrypto = true
            ),
            IvyCurrency(
                code = "TON",
                name = "Tonchain",
                isCrypto = true
            ),
        )

        private val PRECIOUS_METAL_GRAMS = setOf(
            IvyCurrency(Currency.getInstance("XAU")),
            IvyCurrency(
                code = "XAUG",
                name = "Gold (grams)",
                isCrypto = false
            ),
            IvyCurrency(Currency.getInstance("XAG")),
            IvyCurrency(
                code = "XAGG",
                name = "Silver (grams)",
                isCrypto = false
            ),
            IvyCurrency(Currency.getInstance("XPT")),
            IvyCurrency(
                code = "XPTG",
                name = "Platinum (grams)",
                isCrypto = false
            ),
            IvyCurrency(Currency.getInstance("XPD")),
            IvyCurrency(
                code = "XPDG",
                name = "Palladium (grams)",
                isCrypto = false
            ),
        )

        fun getAvailable(): List<IvyCurrency> {
            return Currency.getAvailableCurrencies()
                .map {
                    IvyCurrency(
                        code = it.currencyCode,
                        name = it.displayName,
                        isCrypto = false
                    )
                }
                .plus(CRYPTO)
                .plus(PRECIOUS_METAL_GRAMS)
        }

        fun fromCode(code: String): IvyCurrency? {
            if (code.isBlank()) return null

            val crypto = CRYPTO.find { it.code == code }
            if (crypto != null) {
                return crypto
            }

            val metal = PRECIOUS_METAL_GRAMS.find { it.code == code }
            if (metal != null) {
                return metal
            }

            return try {
                val fiat = Currency.getInstance(code)
                IvyCurrency(
                    fiatCurrency = fiat
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getDefault(): IvyCurrency = IvyCurrency(
            fiatCurrency = getDefaultFIATCurrency()
        )

        fun getDecimalPlaces(assetCode: String): Int =
            if (fromCode(assetCode) in CRYPTO) CRYPTO_DECIMAL else FIAT_DECIMAL
    }

    constructor(fiatCurrency: Currency) : this(
        code = fiatCurrency.currencyCode,
        name = fiatCurrency.displayName,
        isCrypto = false
    )
}
