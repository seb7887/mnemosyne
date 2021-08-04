package io.metrix.mnemosyne

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MnemosyneApplication

fun main(args: Array<String>) {
    runApplication<MnemosyneApplication>(*args)
}
