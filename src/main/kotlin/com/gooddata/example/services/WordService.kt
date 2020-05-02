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

package com.gooddata.example.services

import com.gooddata.example.data.Word
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
interface WordService {

    /**
     * Insert new word into repository
     * @see [org.springframework.data.mongodb.repository.ReactiveMongoRepository.insert]
     */
    fun insert(word: Word): Mono<Word>

    /**
     * Finds all words from repository
     * @see [org.springframework.data.mongodb.repository.ReactiveMongoRepository.findAll]
     */
    fun findAll(): Flux<Word>

    /**
     * Finds first word from repository by word
     * @see [com.gooddata.example.repository.WordRepository.findFirstByWord]
     */
    fun findFirstByWord(word: String): Mono<Word>

    /**
     * Filter function to check whether given word is forbidden.
     * @param word Word to be tested
     * @return Mono of boolean where `true` means the word is forbidden
     */
    fun isWordForbidden(word: String): Mono<Boolean>

}