package daidaidai.models

import daidaidai.eth2dai.Config
import org.web3j.abi.datatypes.Address

enum class CryptoCurrency(val address: String) {
    USD(""),
    ETH(""),
    WETH(Config.weth),
    DAI(Config.dai),
    UNKNOWN("");

    companion object {
        fun get(address: Address): CryptoCurrency {
            return when (address.toString()) {
                WETH.address -> CryptoCurrency.WETH
                DAI.address -> CryptoCurrency.DAI
                else -> CryptoCurrency.UNKNOWN
            }
        }
    }
}