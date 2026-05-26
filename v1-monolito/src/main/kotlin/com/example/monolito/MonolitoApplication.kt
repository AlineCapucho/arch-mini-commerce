package com.example.monolito

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MonolitoApplication

fun main(args: Array<String>) {
    runApplication<MonolitoApplication>(*args)
}
