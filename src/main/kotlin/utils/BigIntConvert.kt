package daidaidai.utils

import org.web3j.utils.Convert
import java.math.BigInteger


fun bigIntToEther(bigInt: BigInteger) = Convert.fromWei(bigInt.toBigDecimal(), Convert.Unit.ETHER)