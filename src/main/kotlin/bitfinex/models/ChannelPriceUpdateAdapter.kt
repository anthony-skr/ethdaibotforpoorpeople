package daidaidai.bitfinex.models

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import daidaidai.models.OrderLine

class ChannelPriceUpdateAdapter : JsonAdapter<OfferPairPriceUpdate>() {
    override fun fromJson(reader: JsonReader): OfferPairPriceUpdate? {
        try {
            reader.beginArray()
            val channelId = reader.nextString()
            val price = reader.nextDouble()
            val count = reader.nextDouble()
            val amount = reader.nextDouble()
            return OfferPairPriceUpdate(channelId, OrderLine(price, count, amount))
        } catch (e: Exception) {
        }
        return null
    }

    override fun toJson(writer: JsonWriter, value: OfferPairPriceUpdate?) {
        writer.nullValue()
    }
}