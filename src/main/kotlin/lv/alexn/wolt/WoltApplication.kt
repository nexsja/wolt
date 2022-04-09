package lv.alexn.wolt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WoltApplication

fun main(args: Array<String>) {

    @Suppress("SpreadOperator")
    runApplication<WoltApplication>(*args)
}
