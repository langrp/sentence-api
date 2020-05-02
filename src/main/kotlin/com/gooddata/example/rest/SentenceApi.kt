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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
@RestController
class SentenceApi @Autowired constructor(
        private val sentenceService: SentenceService
) {

    @GetMapping("/sentences")
    fun getSentences(): Flux<SentenceMsg> = sentenceService.findAll()

    @GetMapping("/sentences/{sentenceId}")
    fun getSentence(@PathVariable("sentenceId") sentenceId: String): Mono<SentenceMsg> =
            sentenceService.findById(sentenceId)
                .switchIfEmpty(Mono.error {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "Sentence '$sentenceId' not found")
                })

    @GetMapping("/sentences/{sentenceId}/yodaTalk")
    fun getSentenceYodaTalk(@PathVariable("sentenceId") sentenceId: String): Mono<SentenceMsg> =
            sentenceService.findByIdYodaTalk(sentenceId)
                .switchIfEmpty(Mono.error {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "Sentence '$sentenceId' not found")
                })

    @PostMapping("/sentences/generate")
    fun generateSentence(): Mono<SentenceMsg> =
            sentenceService.generateSentence()
            .onErrorMap{ ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, it.message) }

}