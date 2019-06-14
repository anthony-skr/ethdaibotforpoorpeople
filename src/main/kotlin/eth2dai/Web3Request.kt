package daidaidai.eth2dai

import daidaidai.logs.Logs
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.utils.Convert
import java.math.BigDecimal

class Web3Request(
    client: Web3Client,
    contract: String,
    name: String,
    inputType: List<Type<out Any>>,
    outputType: List<TypeReference<*>>
) {
    companion object {
        private const val TAG = "WEB3"
    }

    private val web3 = client.web3
    val function = Function(name, inputType, outputType)
    val functionString: String = FunctionEncoder.encode(function)
    val transaction: Transaction = Transaction.createEthCallTransaction(Config.fromAddress, contract, functionString)

    init {
        //logGas(functionString, transaction)
        //Logs.info(functionString)
    }

    fun build(): Request<*, EthCall> = web3.ethCall(transaction, DefaultBlockParameterName.LATEST)

    private fun logGas(functionString: String, transaction: Transaction) {
        val gas = web3.ethEstimateGas(transaction).send().amountUsed.toBigDecimal()
        val gasPrice = web3.ethGasPrice().send().gasPrice.toBigDecimal()
        val transactionPrice = Convert.fromWei(gas * gasPrice, Convert.Unit.ETHER)
        val transactionPriceUsd = transactionPrice.multiply(BigDecimal(200.0))
        Logs.info(TAG, "FM: $functionString")
        Logs.info(TAG, "gas: $gas")
        Logs.info(TAG, "gas price: $gasPrice WEI")
        Logs.info(TAG, "transaction price: $transactionPrice ETH")
        Logs.info(TAG, "transaction price: $transactionPriceUsd USD")
    }
}