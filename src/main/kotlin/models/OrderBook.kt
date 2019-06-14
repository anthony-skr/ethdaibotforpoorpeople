package daidaidai.models

import java.util.concurrent.ConcurrentHashMap

typealias Price = Double

class MarketOrderBook {
    /** offres de vente : je peux acheter **/
    private val bid: MutableMap<Price, OrderLine> = ConcurrentHashMap()

    /** offres d'achat : je peux leur vendre */
    private val ask: MutableMap<Price, OrderLine> = ConcurrentHashMap()

    val bidList = bid as Map<Price, OrderLine>
    val askList = ask as Map<Price, OrderLine>

    fun clearBid() = bid.clear()
    fun clearAsk() = ask.clear()

    fun putBidList(list: List<OrderLine>) {
        bid.clear()
        bid.putAll(list.associateBy { o -> o.price })
    }

    fun putAskList(list: List<OrderLine>) {
        ask.clear()
        ask.putAll(list.associateBy { o -> o.price })
    }

    fun putBid(order: OrderLine) {
        bid[order.price] = order.copyAbsoluteAmount()
        bid.clearBook()
    }

    fun putAsk(order: OrderLine) {
        ask[order.price] = order.copyAbsoluteAmount()
        ask.clearBook()
    }

    private fun MutableMap<Price, OrderLine>.clearBook() {
        val it = iterator()
        while (it.hasNext()) {
            val (_, order) = it.next()
            if (order.count == 0.0) it.remove()
        }
    }
}