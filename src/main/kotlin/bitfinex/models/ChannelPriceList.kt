package daidaidai.bitfinex.models

import daidaidai.models.OrderLine

class ChannelPriceList(
    val channelId: String,
    val prices: List<OrderLine>
)