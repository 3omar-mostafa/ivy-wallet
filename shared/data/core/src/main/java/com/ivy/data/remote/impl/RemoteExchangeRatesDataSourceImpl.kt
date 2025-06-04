package com.ivy.data.remote.impl

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.catch
import com.ivy.data.remote.RemoteExchangeRatesDataSource
import com.ivy.data.remote.responses.ExchangeRatesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class RemoteExchangeRatesDataSourceImpl @Inject constructor(
    private val ktorClient: dagger.Lazy<HttpClient>,
) : RemoteExchangeRatesDataSource {
    private val urls = listOf(
        "https://currency-api.pages.dev/v1/currencies/eur.json",
        "https://currency-api.pages.dev/v1/currencies/eur.min.json",
        "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.min.json",
        "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.json",
    )

    override suspend fun fetchEurExchangeRates(): Either<String, ExchangeRatesResponse> {
        var latestResult: Either<String, ExchangeRatesResponse> = "Impossible".left()
        for (url in urls) {
            latestResult = fetchEurExchangeRates(url)
            if (latestResult.isRight()) {
                return latestResult
            }
        }
        return latestResult
    }

    private suspend fun fetchEurExchangeRates(
        url: String
    ): Either<String, ExchangeRatesResponse> = catch({
        var value = ktorClient.get().get(url).body<ExchangeRatesResponse>()
        // Add metal grams to the response
        val TROY_OUNCE = 31.1034768 // grams in a troy ounce
        val preciousMetalGrams = setOf(
            Pair("xaug", TROY_OUNCE * (value.rates["xau"] ?: 0.0)), // Gold (grams)
            Pair("xagg", TROY_OUNCE * (value.rates["xag"] ?: 0.0)), // Silver (grams)
            Pair("xptg", TROY_OUNCE * (value.rates["xpt"] ?: 0.0)), // Platinum (grams)
            Pair("xpdg", TROY_OUNCE * (value.rates["xpd"] ?: 0.0)), // Palladium (grams)
        ).toMap().filterValues { it > 0.0 }

        Either.Right(value.copy(rates = value.rates + preciousMetalGrams))
    }) { e ->
        Either.Left(e.message ?: "Error fetching exchange rates")
    }
}
