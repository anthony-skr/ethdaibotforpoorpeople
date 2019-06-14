package daidaidai.logs

import java.text.SimpleDateFormat
import java.util.*

object Logs {
    private val time = SimpleDateFormat("dd/MM/YY HH:mm:ss.SSS")

    fun error(throwable: Throwable?, tag: String, msg: String) {
        val date = time.format(Date())
        println("$date [$tag] ----- ERROR ERROR ERROR ----- $msg")
        throwable?.run { throwable.printStackTrace() }
    }

    fun info(tag: String, msg: String) {
        val date = time.format(Date())
        println("$date [$tag] $msg")
    }
}