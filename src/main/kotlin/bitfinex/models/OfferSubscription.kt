package bitfinex.models

class OfferSubscription(
    val event: String,
    val channel: String,
    val chanId: String? = null,
    val pair: String,
    val prec: String,
    val length: String,
    val freq: String
)

enum class OfferSubscriptionEvent(val value: String) {
    SUBSCRIBE("subscribe"),
    SUBSCRIBED("subscribed");
}

enum class Channel(val value: String) {
    BOOK("book");
}

enum class OfferPair(val value: String) {
    ETHUSD("ETHUSD"),
    DAIUSD("DAIUSD")
}