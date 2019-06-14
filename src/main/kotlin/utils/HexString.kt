package daidaidai.utils

fun String.hexStringToByteArray(): ByteArray {
    val str =
        if (startsWith("0x", true)) substring(2)
        else this

    val data = ByteArray(str.length / 2)
    for (i in 0 until str.length step 2) {
        val ch1 = Character.digit(str[i], 16)
        val ch2 = Character.digit(str[i + 1], 16)
        data[i / 2] = ((ch1 shl 4) + ch2).toByte()
    }
    return data
}

fun ByteArray.toHexString(): String {
    val out = StringBuilder()
    for (b in this) {
        out.append(String.format("%02X", b).toLowerCase())
    }
    return out.toString()
}