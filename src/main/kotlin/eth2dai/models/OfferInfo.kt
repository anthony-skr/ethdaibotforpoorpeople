package daidaidai.eth2dai.models

import daidaidai.models.CryptoCurrency
import daidaidai.utils.bigIntToEther
import java.math.BigDecimal
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*

class OfferInfo(
    val id: BigInteger,
    val pay: BigInteger,
    val payAddress: CryptoCurrency,
    val buy: BigInteger,
    val buyAddress: CryptoCurrency,
    val owner: BigInteger,
    val timestamp: Date
) {
    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/YY HH:mm:ss")
    }

    val payAmount6: BigDecimal = bigIntToEther(pay)
    val buyAmount6: BigDecimal = bigIntToEther(buy)

    /** PAY token divide by BUY token */
    val pricePayBuy: BigDecimal =
        if (buyAmount6 == BigDecimal.ZERO) BigDecimal.ZERO
        else payAmount6 / buyAmount6

    /** BUY token divide by PAY token */
    val priceBuyPay: BigDecimal =
        if (payAmount6 == BigDecimal.ZERO) BigDecimal.ZERO
        else buyAmount6 / payAmount6

    override fun toString(): String {
        return "offer $id PAY $payAmount6 $payAddress BUY $buyAmount6 $buyAddress\n" +
                "price=$pricePayBuy / $priceBuyPay owner=$owner & timestamp=${dateFormat.format(timestamp)}"
    }
}