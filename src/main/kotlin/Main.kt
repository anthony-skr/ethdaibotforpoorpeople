package daidaidai

import daidaidai.bitfinex.BitfinexClient
import daidaidai.eth2dai.Eth2DaiClient
import daidaidai.eth2dai.Web3Client
import daidaidai.logs.Logs
import daidaidai.models.OrderLine
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit

object Main {
    private const val TAG = "MAIN"

    private val repo = OrderBookRepository()

    private val clientWeb3 = Web3Client()
    private val clientEth2Dai = Eth2DaiClient(clientWeb3, repo)
    private val clientBitfinex = BitfinexClient(repo)

    @JvmStatic
    fun main(args: Array<String>) {
        clientEth2Dai.listen()
        clientBitfinex.listen()

        Observable.interval(5, TimeUnit.SECONDS)
            .map { showOrderBook() }
            .subscribeBy(onError = { t -> Logs.error(t, TAG, "error interval") })
    }

    private fun Collection<OrderLine>.askList() = sortedBy { o -> o.price }
    private fun Collection<OrderLine>.bidList() = sortedByDescending { o -> o.price }

    private fun showOrderBook() {
        searchOpportunity(
            "Bitfinex",
            repo.bitfinexETHUSD.askList.values.askList(),

            "ETH2DAI",
            repo.oasisWETHDAI.bidList.values.bidList(),

            true
        )

        searchOpportunity(
            "ETH2DAI",
            repo.oasisWETHDAI.askList.values.askList(),

            "Bitfinex",
            repo.bitfinexETHUSD.bidList.values.bidList(),

            false
        )
    }

    private fun Double.asQty() = String.format("%.06f", this)
    private fun Double.asUSD() = String.format("%.02f USD", this)

    private fun searchOpportunity(
        askMarket: String,
        askList: List<OrderLine>,
        bidMarket: String,
        bidList: List<OrderLine>,
        startFromBid: Boolean
    ) {
        val startList = if (startFromBid) bidList else askList
        val otherList = if (startFromBid) askList else bidList

        var profit = 0.0
        var usdNeeded = 0.0
        var amountNeeded = 0.0

        startList.forEach { startOffer ->
            var qty = startOffer.amount
            var stop = false

            otherList.forEach loopOther@{ otherOffer ->
                if (stop) return@loopOther

                val offerBid =
                    if (startFromBid) startOffer
                    else otherOffer

                val offerAsk =
                    if (startFromBid) otherOffer
                    else startOffer

                // opportunity
                if (offerBid.price > offerAsk.price) {
                    val diff = offerBid.price - offerAsk.price
                    val amountMax = Math.min(otherOffer.amount, qty)
                    val opportunityProfit = diff * amountMax

                    qty -= amountMax
                    profit += opportunityProfit
                    amountNeeded += amountMax
                    usdNeeded += amountMax * offerAsk.price
                }

                if (qty <= 0) stop = true
            }
        }

        val daiNeeded = if (startFromBid) {
            costBuyUsd(usdNeeded)
        } else {
            costSellUsd(usdNeeded)
        }

        val costDaiUsdTransfer = daiNeeded - usdNeeded
        val realProfit = profit - costDaiUsdTransfer

        if (profit > 0) {
            //Logs.info(TAG, "profit detected but no real profit ($realProfit)")
            if (realProfit > 0) {
                Logs.info(
                    TAG,
                    "PROFIT : +${profit.asUSD()} with $amountNeeded ETH, " +
                            "$usdNeeded USD, $daiNeeded DAI, " +
                            "cost transfer $costDaiUsdTransfer"
                )
            }
        }
    }

    // disgusting code below

    private fun costBuyUsd(usdNeeded: Double): Double {
        var usdQty = usdNeeded
        var daiNeeded = 0.0
        var stop = false

        repo.bitfinexDAIUSD.bidList.values.bidList().forEach { o ->
            if (stop) return@forEach

            val usdOffer = o.price * o.amount
            val usdToBuy = Math.min(usdQty, usdOffer)
            val daiPrice = usdToBuy / o.price

            usdQty -= usdToBuy
            daiNeeded += daiPrice

            if (usdQty <= 0) stop = true
        }

        return daiNeeded
    }

    private fun costSellUsd(usdOwned: Double): Double {
        var usdQty = usdOwned
        var daiNeeded = 0.0
        var stop = false

        repo.bitfinexDAIUSD.askList.values.askList().forEach { o ->
            if (stop) return@forEach

            val usdOffer = o.price * o.amount
            val usdToBuy = Math.min(usdQty, usdOffer)
            val daiPrice = usdToBuy / o.price

            usdQty -= usdToBuy
            daiNeeded += daiPrice

            if (usdQty <= 0) stop = true
        }

        return daiNeeded
    }
}