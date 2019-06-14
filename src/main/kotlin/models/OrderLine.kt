package daidaidai.models

data class OrderLine(
    /** price for 1 token */
    val price: Double,
    /** number of offers */
    val count: Double,
    /** qty of token */
    val amount: Double
) {
    fun copyAbsoluteAmount() = copy(amount = Math.abs(amount))

    override fun toString(): String {
        return String.format("%.02f USD", price)
    }
}