package com.gooddata.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SentenceApiApplication

fun main(args: Array<String>) {
	runApplication<SentenceApiApplication>(*args)
}
