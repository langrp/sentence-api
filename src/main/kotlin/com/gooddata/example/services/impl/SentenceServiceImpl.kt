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

package com.gooddata.example.services.impl

import com.gooddata.example.data.Sentence
import com.gooddata.example.data.Word
import com.gooddata.example.data.WordCategory
import com.gooddata.example.message.SentenceAggregateMsg
import com.gooddata.example.message.SentenceMsg
import com.gooddata.example.repository.SentenceRepository
import com.gooddata.example.repository.WordRepository
import com.gooddata.example.services.SentenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function
import kotlin.random.Random

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
@Service
class SentenceServiceImpl @Autowired constructor(
        private val sentenceRepository: SentenceRepository,
        private val wordRepository: WordRepository
) : SentenceService {

    private val random = Random(0)

    override fun findAll(): Flux<SentenceMsg> {
        return sentenceRepository.findAll().map { s -> SentenceMsg.of(s) }
    }

    override fun findById(id: String): Mono<SentenceMsg> {
        return viewSentence(id).map { s -> SentenceMsg.of(s) }
    }

    override fun findByIdYodaTalk(id: String): Mono<SentenceMsg> {
        return viewSentence(id).map { s -> SentenceMsg.yoda(s) }
    }

    override fun generateSentence(): Mono<SentenceMsg> {
        @Suppress("RemoveExplicitTypeArguments")
        return Flux.combineLatest<Word, Sentence>(
                        Function { words -> Sentence(null, words.mapToTypedArray { it as Word }) },
                        generateWordByCategory(WordCategory.NOUN),
                        generateWordByCategory(WordCategory.VERB),
                        generateWordByCategory(WordCategory.ADJECTIVE)
                )
                .flatMap { s -> sentenceRepository.insert(s) }
                .map { s -> SentenceMsg.of(s) }
                .next()
    }

    override fun findByWords(): Flux<SentenceAggregateMsg> = sentenceRepository.findByWords()
            .map { SentenceAggregateMsg.of(it) }

    private fun viewSentence(id: String): Mono<Sentence> =
        sentenceRepository.findById(id)
                .map { s -> Sentence(s.id, s.words, s.views + 1, s.created) }
                //TODO consider onComplete or other to avoid increment of failures
                .flatMap { s -> sentenceRepository.save(s) }

    private fun generateWordByCategory(category: WordCategory): Mono<Word> {
        return wordRepository.countByCategory(category)
                .filter { it > 0L }
                .switchIfEmpty(Mono.error { IllegalStateException("No word for '$category' category") })
                .flatMap { c ->
                    wordRepository.findFirstByCategory(category,
                            PageRequest.of(random.nextInt(c.toInt()), 1))
                            .next()
                }
    }

    private inline fun <T, reified R> Array<T>.mapToTypedArray(transform: (T) -> R): Array<R> {
        return when (this) {
            is RandomAccess -> Array(size) { index -> transform(this[index]) }
            else -> with(iterator()) { Array(size) { transform(next()) } }
        }
    }

}