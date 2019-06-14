package daidaidai.utils

import org.web3j.abi.datatypes.Address
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*


private const val BIGINT_SIZE = 32

class ParseHexString(value: String) {
    private val data = value.hexStringToByteArray()
    private val buffer = ByteBuffer.wrap(data)

    init {
        //Logs.info("parse value $value = ${data.size}")
    }

    fun numberWords(word: Int = BIGINT_SIZE) = data.size / word

    fun <T> repeatReat(word: Int = BIGINT_SIZE, times: Int, read: () -> T): List<T> {
        val out = mutableListOf<T>()
        for (i in 0 until times) {
            out.add(read())
            if (buffer.remaining() < word) return out
        }
        return out
    }

    private fun read(word: Int): ByteArray {
        val bytes = ByteArray(word)
        buffer.get(bytes, 0, word)
        //Logs.info("read ${bytes.toHexString()}")
        return bytes
    }

    fun readBigInt(word: Int = BIGINT_SIZE) = BigInteger(read(word))

    fun readAddress(word: Int = BIGINT_SIZE) = Address(BigInteger(read(word)))

    fun readLong(word: Int = BIGINT_SIZE): Long {
        val wordData = read(word)
        val buffer = ByteBuffer.wrap(ByteArray(Long.SIZE_BYTES))
        buffer.put(wordData, word - Long.SIZE_BYTES, Long.SIZE_BYTES)
        buffer.flip()
        return buffer.long
    }

    fun readTimestamp(word: Int = BIGINT_SIZE) = Date(readLong(word) * 1000)
}