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
import com.gooddata.example.services.WordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.validation.Valid

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
@RestController
class WordApi @Autowired constructor(
        private val wordService: WordService
) {

    @GetMapping("/words")
    fun getWords(): Flux<Word> = wordService.findAll()

    /**
     * Adds new word into the repository. Following is being assumed:
     * 1. Passed "id" parameter is being ignored
     * 2. No world duplicities allowed (case sensitive world, ignores category)
     * 3. Validated word length (or other validations defined on Word)
     */
    @PutMapping("/words")
    fun addWord(@RequestBody @Valid word: Mono<Word>): Mono<Void> {
        return word.map { w -> if (w.id == null ) w else Word(w.word, w.category)}
                .onErrorMap(WebExchangeBindException::class.java) {
                    //TODO Log info of binding fields for investigation
                    ResponseStatusException(HttpStatus.BAD_REQUEST, "Field validation failure")
                }.filterWhen { w -> wordService.findFirstByWord(w.word)
                                .map { false }
                                .defaultIfEmpty(true)
                }
                .switchIfEmpty(Mono.error { ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicated word") })
                .filterWhen { w -> wordService.isWordForbidden(w.word).map { !it } }
                .switchIfEmpty(Mono.error { ResponseStatusException(HttpStatus.BAD_REQUEST, "Forbidden word") })
                .flatMap { w -> wordService.insert(w).then() }
    }

    @GetMapping("/words/{word}")
    fun getWord(@PathVariable("word") word: String): Mono<Word> {
        return wordService.findFirstByWord(word)
                .switchIfEmpty(Mono.error {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "Word '$word' not found")
                })
    }

}