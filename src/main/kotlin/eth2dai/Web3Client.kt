package daidaidai.eth2dai

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService


class Web3Client {
    val web3: Web3j = Web3j.build(HttpService(Config.infura))

    fun request(
        contract: String,
        name: String,
        input: List<Type<out Any>>,
        output: List<TypeReference<*>>
    ) =
        Web3Request(this, contract, name, input, output)
}