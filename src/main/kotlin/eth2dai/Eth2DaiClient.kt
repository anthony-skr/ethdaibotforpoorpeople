package daidaidai.eth2dai

import daidaidai.OrderBookRepository
import daidaidai.eth2dai.models.OfferInfo
import daidaidai.logs.Logs
import daidaidai.models.CryptoCurrency
import daidaidai.models.OrderLine
import daidaidai.utils.ParseHexString
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger
import java.util.concurrent.TimeUnit


class Eth2DaiClient(
    private val client: Web3Client,
    private val repo: OrderBookRepository
) {
    companion object {
        private const val TAG = "ETH2DAI"
    }

    fun listen() {
        Flowable.interval(1, 10, TimeUnit.SECONDS)
            .onBackpressureDrop()
            .flatMapSingle {
                Single.fromCallable {
                    // people who buy WETH with DAI. we can sell to them
                    getOffers(buyToken = Address(Config.weth), payToken = Address(Config.dai))
                        .also { repo.oasisWETHDAI.clearBid() }
                        .forEach { o ->
                            val price = o.pricePayBuy
                            val order = OrderLine(price.toDouble(), 1.0, o.buyAmount6.toDouble())
                            repo.oasisWETHDAI.putBid(order)
                        }

                    // people who buy DAI with WETH. we can buy from them
                    getOffers(buyToken = Address(Config.dai), payToken = Address(Config.weth))
                        .also { repo.oasisWETHDAI.clearAsk() }
                        .forEach { o ->
                            val price = o.priceBuyPay
                            val order = OrderLine(price.toDouble(), 1.0, o.payAmount6.toDouble())
                            repo.oasisWETHDAI.putAsk(order)
                        }
                }
            }
            .subscribeBy(
                onError = { t -> Logs.error(t, TAG, "error oasis get offers") }
            )
    }

    private fun getOffers(payToken: Address, buyToken: Address): List<OfferInfo> {
        val input = listOf(Address(Config.otc), payToken, buyToken)
        return getOffersOtc(input, payToken, buyToken)
    }

    private fun getOffersOtc(input: List<Type<out Any>>, payToken: Address, buyToken: Address): List<OfferInfo> {
        val outputType = listOf(
            TypeReference.create(DynamicArray(Uint256::class.java)::class.java),
            TypeReference.create(DynamicArray(Uint256::class.java)::class.java),
            TypeReference.create(DynamicArray(Uint256::class.java)::class.java),
            TypeReference.create(DynamicArray(Uint256::class.java)::class.java),
            TypeReference.create(DynamicArray(Uint256::class.java)::class.java)
        )

        return client.request(Config.otcSupportMethods, "getOffers", input, outputType).build().send()
            .run { ParseHexString(value) }
            .run { parseOffers(payToken, buyToken) }
    }

    private fun ParseHexString.parseOffers(payToken: Address, buyToken: Address): List<OfferInfo> {
        val nbItems = numberWords() / 5
        val ids = repeatReat(times = nbItems) { readLong() }
        val sells = repeatReat(times = nbItems) { readBigInt() }
        val buys = repeatReat(times = nbItems) { readBigInt() }
        val owners = repeatReat(times = nbItems) { readBigInt() }
        val timestamps = repeatReat(times = nbItems) { readTimestamp() }
        val inconsistent = sells.size < ids.size
                || buys.size < ids.size
                || owners.size < ids.size
                || timestamps.size < ids.size
        if (inconsistent) throw Exception("data inconsistency.")
        return ids.mapIndexed { i, id ->
            OfferInfo(
                id = BigInteger.valueOf(id),
                pay = sells[i],
                payAddress = CryptoCurrency.get(payToken),
                buy = buys[i],
                buyAddress = CryptoCurrency.get(buyToken),
                owner = owners[i],
                timestamp = timestamps[i]
            )
        }
    }

    /*
    fun getBestOffer(payToken: String, buyToken: String): Long {
        val input = listOf(Address(payToken), Address(buyToken))
        val outputType = listOf(TypeReference.create(Uint256::class.java))
        return client.request(Config.otc, "getBestOffer", input, outputType).build().send()
            .run { ParseHexString(value).readLong() }
    }

    fun getOffer(id: BigInteger): OfferInfo {
        val input = listOf(Uint256(id))
        val outputType = listOf(
            TypeReference.create(Uint256::class.java),
            TypeReference.create(Address::class.java),
            TypeReference.create(Uint256::class.java),
            TypeReference.create(Address::class.java),
            TypeReference.create(Address::class.java),
            TypeReference.create(Uint64::class.java)
        )
        return client.request(Config.otc, "offers", input, outputType).build().send()
            .run { ParseHexString(value) }
            .run {
                OfferInfo(
                    id = id,
                    pay = readBigInt(),
                    payAddress = CryptoCurrency.get(readAddress()),
                    buy = readBigInt(),
                    buyAddress = CryptoCurrency.get(readAddress()),
                    owner = readBigInt(),
                    timestamp = readTimestamp()
                )
            }
    }
    */
}