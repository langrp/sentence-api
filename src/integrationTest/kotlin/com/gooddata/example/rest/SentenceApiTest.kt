/*
 * MIT License
 *
 * Copyright (c) 2020 Petr Langr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.gooddata.example.rest

import com.gooddata.example.message.SentenceMsg
import com.gooddata.example.services.SentenceService
import com.mongodb.MongoException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * @author petr.langr
 * @since 1.0.0
 */
@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ SentenceApi::class ])
internal class SentenceApiTest {

    @MockBean
    private lateinit var sentenceService: SentenceService

    @Autowired
    private lateinit var webClient: WebTestClient

    @Test
    fun getSentences() {

        `when`(sentenceService.findAll()).thenReturn(Flux.fromIterable(listOf(
                SentenceMsg("0", "Kawa is best", 1),
                SentenceMsg("1", "Scuba is wet", 23)
        )))

        //@formatter:off
        webClient.get()
                .uri("/sentences")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk
                .expectBody()
                    .json("[{\"id\":\"0\",\"text\":\"Kawa is best\",\"views\":1}," +
                            "{\"id\":\"1\",\"text\":\"Scuba is wet\",\"views\":23}]")
        //@formatter:on

        verify(sentenceService, times(1)).findAll()

    }

    @Test
    fun getSentences_empty() {

        `when`(sentenceService.findAll()).thenReturn(Flux.empty())

        //@formatter:off
        webClient.get()
                .uri("/sentences")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk
                .expectBody()
                    .json("[]")
        //@formatter:on

        verify(sentenceService, times(1)).findAll()

    }

    @Test
    fun getSentences_error() {

        `when`(sentenceService.findAll()).thenReturn(Flux.error(MongoException("Unknown error")))

        //@formatter:off
        webClient.get()
                .uri("/sentences")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().is5xxServerError
                .expectBody()
                    .jsonPath("$.status").isEqualTo("500")
        //@formatter:on

        verify(sentenceService, times(1)).findAll()

    }

    @Test
    fun getSentence() {

        `when`(sentenceService.findById("0"))
                .thenReturn(Mono.just(SentenceMsg("0", "Kawa is best", 1)))

        //@formatter:off
        webClient.get()
                .uri("/sentences/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk
                .expectBody()
                    .jsonPath("$.id").isEqualTo("0")
                    .jsonPath("$.text").isEqualTo("Kawa is best")
                    .jsonPath("$.views").isEqualTo(1)
        //@formatter:on

        verify(sentenceService, times(1)).findById("0")

    }

    @Test
    fun getSentence_error() {

        `when`(sentenceService.findById("0")).thenReturn(Mono.empty())

        //@formatter:off
        webClient.get()
                .uri("/sentences/0")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isNotFound
                .expectBody()
                    .jsonPath("$.status").isEqualTo("404")
                    .jsonPath("$.message").isEqualTo("Sentence '0' not found")
        //@formatter:on

        verify(sentenceService, times(1)).findById("0")

    }

    @Test
    fun getSentenceYodaTalk() {

        `when`(sentenceService.findByIdYodaTalk("0"))
                .thenReturn(Mono.just(SentenceMsg("0", "best Kawa is", 1)))

        //@formatter:off
        webClient.get()
                .uri("/sentences/0/yodaTalk")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk
                .expectBody()
                    .jsonPath("$.id").isEqualTo("0")
                    .jsonPath("$.text").isEqualTo("best Kawa is")
                    .jsonPath("$.views").isEqualTo(1)
        //@formatter:on

        verify(sentenceService, times(1)).findByIdYodaTalk("0")

    }

    @Test
    fun getSentenceYodaTalk_error() {

        `when`(sentenceService.findByIdYodaTalk("0")).thenReturn(Mono.empty())

        //@formatter:off
        webClient.get()
                .uri("/sentences/0/yodaTalk")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isNotFound
                .expectBody()
                    .jsonPath("$.status").isEqualTo("404")
                    .jsonPath("$.message").isEqualTo("Sentence '0' not found")
        //@formatter:on

        verify(sentenceService, times(1)).findByIdYodaTalk("0")

    }

    @Test
    fun generateSentence() {

        `when`(sentenceService.generateSentence())
                .thenReturn(Mono.just(SentenceMsg("2", "Yoda is short", 0)))

        //@formatter:off
        webClient.post()
                .uri("/sentences/generate")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk
                .expectBody()
                        .jsonPath("$.id").isEqualTo("2")
                        .jsonPath("$.text").isEqualTo("Yoda is short")
                        .jsonPath("$.views").isEqualTo(0)
        //@formatter:on

        verify(sentenceService, times(1)).generateSentence()

    }

    @Test
    fun generateSentence_missing_category() {

        `when`(sentenceService.generateSentence())
                .thenReturn(Mono.error { IllegalStateException("No word for 'NOUN' category") })

        //@formatter:off
        webClient.post()
                .uri("/sentences/generate")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().is5xxServerError
                .expectBody()
                    .jsonPath("$.status").isEqualTo("500")
                    .jsonPath("$.message").isEqualTo("No word for 'NOUN' category")
        //@formatter:on

        verify(sentenceService, times(1)).generateSentence()

    }
}