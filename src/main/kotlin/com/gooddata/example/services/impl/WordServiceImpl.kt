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

import com.gooddata.example.data.Word
import com.gooddata.example.loggerFor
import com.gooddata.example.repository.WordRepository
import com.gooddata.example.services.WordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Paths

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
@Service
class WordServiceImpl @Autowired constructor(
        private val wordRepository: WordRepository
): WordService {

    companion object {
        private val log = loggerFor<SentenceServiceImpl>()
    }

    @Value("\${com.gooddata.sentence-api.forbidden_words_file}")
    private var forbiddenWordsFile: String? = null

    override fun insert(word: Word): Mono<Word> = wordRepository.insert(word)

    override fun findAll(): Flux<Word> = wordRepository.findAll()

    override fun findFirstByWord(word: String): Mono<Word> = wordRepository.findFirstByWord(word)

    override fun isWordForbidden(word: String): Mono<Boolean> {
        val file = forbiddenWordsFile
        if (file == null) {
            return Mono.just(false)
        }
        val path = Paths.get(file)
        if (!Files.exists(path)) {
            log.info("Forbidden words file does not exist")
            return Mono.just(false)
        }
        return Flux.using({ Files.lines(path) }, { Flux.fromStream(it) }) { it.close() }
                .map { it.trim() }
                .filter { word.equals(it, true) }
                .next()
                .map { true }
                .defaultIfEmpty(false)
    }
}