package daidaidai.bitfinex

import bitfinex.models.OfferSubscription
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface BitfinexPublicApi {
    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocket.Event>

    @Send
    fun subscribeOfferBooks(subscribe: OfferSubscription)

    @Receive
    fun receiveOfferBooks(): Flowable<OfferSubscription>
}