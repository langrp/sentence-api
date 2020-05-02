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

import com.gooddata.example.data.Word
import com.gooddata.example.data.WordCategory
import com.gooddata.example.services.WordService
import com.gooddata.example.util.any
import com.mongodb.MongoException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.internal.invocation.InvocationsFinder
import org.mockito.invocation.Invocation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


/**
 * @author petr.langr
 * @since 1.0.0
 */
@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [ WordApi::class ])
internal class WordApiTest {

    @MockBean
    private lateinit var wordService: WordService

    @Autowired
    private lateinit var webClient: WebTestClient


    @Test
    fun getWords() {

        `when`(wordService.findAll()).thenReturn(Flux.just(Word("0", "hello", WordCategory.NOUN)))

        //@formatter:off
        webClient.get()
                .uri("/words")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Word::class.java)
                    .hasSize(1)
                    .contains(Word("0", "hello", WordCategory.NOUN))
        //@formatter:on

        verify(wordService, times(1)).findAll()

    }

    @Test
    fun getWords_empty() {

        `when`(wordService.findAll()).thenReturn(Flux.empty())

        webClient.get()
                .uri("/words")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json("[]")

        verify(wordService, times(1)).findAll()

    }

    @Test
    fun getWords_error() {

        `when`(wordService.findAll()).thenReturn(Flux.error(MongoException("Unknown error")))

        //@formatter:off
        webClient.get()
                .uri("/words")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                    .jsonPath("$.status").isEqualTo("500")
        //@formatter:on

        verify(wordService, times(1)).findAll()

    }

    @Test
    fun addWord() {

        `when`(wordService.insert(any())).thenReturn(Mono.empty())
        `when`(wordService.findFirstByWord("world")).thenReturn(Mono.empty())
        `when`(wordService.isWordForbidden("world")).thenReturn(Mono.just(false))

        webClient.put()
                .uri("/words")
                .body(BodyInserters.fromValue(Word("world", WordCategory.NOUN)))
                .exchange()
                .expectStatus().isOk

        verify(wordService, times(1)).findFirstByWord("world")
        verify(wordService, times(1)).insert(any())
        verify(wordService, times(1)).isWordForbidden("world")

    }

    @Test
    fun addWord_remove_id() {

        `when`(wordService.insert(any())).thenReturn(Mono.empty())
        `when`(wordService.findFirstByWord("world")).thenReturn(Mono.empty())
        `when`(wordService.isWordForbidden("world")).thenReturn(Mono.just(false))

        webClient.put()
                .uri("/words")
                .body(BodyInserters.fromValue(Word("1", "world", WordCategory.NOUN)))
                .exchange()
                .expectStatus().isOk

        verify(wordService, times(1)).findFirstByWord("world")
        verify(wordService, times(1)).isWordForbidden("world")
        verify(wordService, { m ->
            val actualInvocations: List<Invocation> = InvocationsFinder
                    .findInvocations(m.getAllInvocations(), m.getTarget())
            assertEquals(1, actualInvocations.size, "Insert method not called")
            assertNotNull(actualInvocations[0], "Insert method called with 'null' object")
            val word = actualInvocations[0].getRawArguments().get(0) as Word
            assertNull(word.id, "Insert with '_id' field")
        }).insert(any())

    }

    @Test
    fun addWord_error_duplicates() {

        `when`(wordService.findFirstByWord("world")).thenReturn(Mono.just(Word("0", "world", WordCategory.NOUN)))

        //@formatter:off
        webClient.put()
                .uri("/words")
                .body(BodyInserters.fromValue(Word(null, "world", WordCategory.NOUN)))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                    .jsonPath("$.message").isEqualTo("Duplicated word")
        //@formatter:on

        verify(wordService, times(1)).findFirstByWord("world")
        verify(wordService, times(0)).insert(any())
        verify(wordService, times(0)).isWordForbidden("world")

    }

    @Test
    fun addWord_forbidden() {

        `when`(wordService.findFirstByWord("world")).thenReturn(Mono.empty())
        `when`(wordService.isWordForbidden("world")).thenReturn(Mono.just(true))

        //@formatter:off
        webClient.put()
                .uri("/words")
                .body(BodyInserters.fromValue(Word("world", WordCategory.NOUN)))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                    .jsonPath("$.message").isEqualTo("Forbidden word")
        //@formatter:on

        verify(wordService, times(1)).findFirstByWord("world")
        verify(wordService, times(0)).insert(any())
        verify(wordService, times(1)).isWordForbidden("world")

    }

    @Test
    fun addWord_no_body() {

        webClient.put()
                .uri("/words")
                .exchange()
                .expectStatus().isBadRequest

    }

    @Test
    fun getWord() {

        `when`(wordService.findFirstByWord("hello"))
                .thenReturn(Mono.just(Word("0", "hello", WordCategory.VERB)))

        //@formatter:off
        webClient.get()
                .uri("/words/hello")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isOk
                .expectBody()
                    .jsonPath("$.id").isEqualTo("0")
                    .jsonPath("$.word").isEqualTo("hello")
                    .jsonPath("$.category").isEqualTo("VERB")
                    .json("{\"id\":\"0\",\"word\":\"hello\",\"category\":\"VERB\"}")
        //@formatter:on

        verify(wordService, times(1)).findFirstByWord("hello")

    }

    @Test
    fun getWord_not_found() {

        `when`(wordService.findFirstByWord("hello")).thenReturn(Mono.empty())

        webClient.get()
                .uri("/words/hello")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectStatus().isNotFound

        verify(wordService, times(1)).findFirstByWord("hello")

    }

}