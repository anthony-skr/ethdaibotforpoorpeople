package daidaidai.bitfinex

import bitfinex.models.Channel
import bitfinex.models.OfferPair
import bitfinex.models.OfferSubscription
import bitfinex.models.OfferSubscriptionEvent
import com.tinder.scarlet.Message
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import daidaidai.OrderBookRepository
import daidaidai.bitfinex.models.ChannelPriceList
import daidaidai.bitfinex.models.ChannelPriceListAdapter
import daidaidai.bitfinex.models.ChannelPriceUpdateAdapter
import daidaidai.bitfinex.models.OfferPairPriceUpdate
import daidaidai.logs.Logs
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


class BitfinexClient(private val repo: OrderBookRepository) {
    companion object {
        private const val TAG = "BITFINEX"
    }

    private val okHttp = OkHttpClient.Builder()
            .build()
            .newWebSocketFactory("wss://api.bitfinex.com/ws")

    private val scarlet = Scarlet.Builder()
            .webSocketFactory(okHttp)
            .addMessageAdapterFactory(MoshiMessageAdapter.Factory())
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .build()

    private val client = scarlet.create<BitfinexPublicApi>()
    private val channelPair = ConcurrentHashMap<String, OfferPair>()
    private val adapterPriceList = ChannelPriceListAdapter()
    private val adapterPriceUpdate = ChannelPriceUpdateAdapter()

    fun listen() {
        client.observeWebSocketEvent().subscribeBy(
                onNext = ::onEvent,
                onComplete = {},
                onError = { t -> Logs.error(t, TAG, "observe web socket events") }
        )
    }

    private fun subscribeOffer(pair: OfferPair) =
            Single.fromCallable { callSubscribeOffer(pair) }
                    .flatMap { receiveConfirmation() }
                    .doOnSuccess { confirm ->
                        Logs.info(TAG, "subscribed to $pair (${confirm.chanId})")
                        channelPair[confirm.chanId!!] = pair
                    }

    private fun callSubscribeOffer(pair: OfferPair) {
        client.subscribeOfferBooks(
                OfferSubscription(
                        event = OfferSubscriptionEvent.SUBSCRIBE.value,
                        channel = Channel.BOOK.value,
                        pair = pair.value,
                        freq = "F0",
                        prec = "P0",
                        length = "25"
                )
        )
    }

    private fun receiveConfirmation() = client.receiveOfferBooks()
            .filter { msg -> msg.channel == Channel.BOOK.value }
            .filter { msg -> msg.event == OfferSubscriptionEvent.SUBSCRIBED.value }
            .timeout(10, TimeUnit.SECONDS)
            .firstOrError()

    private fun onEvent(ev: WebSocket.Event) {
        when (ev) {
            is WebSocket.Event.OnConnectionOpened<*> -> {
                Logs.info(TAG, "connection bitfinex open")
                subscribeOffer(OfferPair.ETHUSD)
                        .flatMap { subscribeOffer(OfferPair.DAIUSD) }
                        .subscribeBy(
                                onSuccess = { Logs.info(TAG, "subscribe OK") },
                                onError = { t -> Logs.error(t, TAG, "error subscribe") })
            }
            is WebSocket.Event.OnConnectionClosing -> {
                Logs.info(TAG, "connection bitfinex closing...")
            }
            is WebSocket.Event.OnConnectionClosed -> {
                Logs.info(TAG, "connection bitfinex closed")
            }
            is WebSocket.Event.OnConnectionFailed -> {
                Logs.error(ev.throwable, TAG, "connection bitfinex failed")
            }
            is WebSocket.Event.OnMessageReceived -> {
                onMessage(ev.message)
            }
        }
    }

    private fun onMessage(msg: Message) {
        if (msg is Message.Text) {
            val priceList = adapterPriceList.fromJson(msg.value)
            if (priceList != null) return onNewPriceList(priceList)

            val priceUpdate = adapterPriceUpdate.fromJson(msg.value)
            if (priceUpdate != null) return onPriceUpdate(priceUpdate)
        }
    }

    private fun onNewPriceList(priceList: ChannelPriceList) {
        val pair = channelPair[priceList.channelId] ?: return
        val (bidList, askList) = priceList.prices.partition { o -> o.amount > 0.0 }
        if (pair == OfferPair.ETHUSD) {
            repo.bitfinexETHUSD.putBidList(bidList)
            repo.bitfinexETHUSD.putAskList(askList.map { o -> o.copyAbsoluteAmount() })
        } else if (pair == OfferPair.DAIUSD) {
            repo.bitfinexDAIUSD.putBidList(bidList)
            repo.bitfinexDAIUSD.putAskList(askList.map { o -> o.copyAbsoluteAmount() })
        }
    }

    private fun onPriceUpdate(update: OfferPairPriceUpdate) = when {
        channelPair[update.channelId] == OfferPair.ETHUSD && update.offerPrice.amount > 0.0 -> repo.bitfinexETHUSD.putBid(update.offerPrice)
        channelPair[update.channelId] == OfferPair.ETHUSD -> repo.bitfinexETHUSD.putAsk(update.offerPrice.copyAbsoluteAmount())
        channelPair[update.channelId] == OfferPair.DAIUSD && update.offerPrice.amount > 0.0 -> repo.bitfinexDAIUSD.putBid(update.offerPrice)
        channelPair[update.channelId] == OfferPair.DAIUSD -> repo.bitfinexDAIUSD.putAsk(update.offerPrice.copyAbsoluteAmount())
        else -> {
        }
    }
}