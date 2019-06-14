package daidaidai.bitfinex.models

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import daidaidai.models.OrderLine

class ChannelPriceListAdapter : JsonAdapter<ChannelPriceList>() {
    override fun fromJson(reader: JsonReader): ChannelPriceList? {
        try {
            reader.beginArray()
            val channelId = reader.nextString()
            reader.beginArray()

            val list = mutableListOf<OrderLine>()
            while (reader.hasNext()) {
                reader.beginArray()
                val price = reader.nextDouble()
                val count = reader.nextDouble()
                val amount = reader.nextDouble()
                list.add(OrderLine(price, count, amount))
                reader.endArray()
            }
            return ChannelPriceList(channelId, list)
        } catch (e: Exception) {
        }
        return null
    }

    override fun toJson(writer: JsonWriter, value: ChannelPriceList?) {
        writer.nullValue()
    }
}