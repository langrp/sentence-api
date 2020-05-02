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
import com.gooddata.example.repository.SentenceRepository
import com.gooddata.example.repository.WordRepository
import com.gooddata.example.util.any
import com.gooddata.example.util.eq
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * @author petr.langr
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
internal class SentenceServiceImplTest {

    @Mock
    lateinit var sentenceRepository: SentenceRepository

    @Mock
    lateinit var wordRepository: WordRepository

    lateinit var testService: SentenceServiceImpl

    @BeforeEach
    fun setup() {
        testService = SentenceServiceImpl(sentenceRepository, wordRepository)
    }

    @Test
    fun findAll() {

        `when`(sentenceRepository.findAll()).thenReturn(Flux.fromIterable(listOf(
                Sentence("0", arrayOf(
                        Word("Kawa", WordCategory.NOUN),
                        Word("is", WordCategory.VERB),
                        Word("best", WordCategory.ADJECTIVE)
                ), 1),
                Sentence("1", arrayOf(
                        Word("Scuba", WordCategory.NOUN),
                        Word("is", WordCategory.VERB),
                        Word("wet", WordCategory.ADJECTIVE)
                ), 34)
        )))

        StepVerifier.create(testService.findAll())
                .consumeNextWith {
                    assertEquals("0", it.id, "Wrong 1st item ID")
                    assertEquals("Kawa is best", it.text, "Wrong 1st item text")
                    assertEquals(1, it.views, "Wrong 1st item views")
                }.consumeNextWith {
                    assertEquals("1", it.id, "Wrong 2nd item ID")
                    assertEquals("Scuba is wet", it.text, "Wrong 2nd item text")
                    assertEquals(34, it.views, "Wrong 2nd item views")
                }
                .verifyComplete()

        verify(sentenceRepository, times(1)).findAll()

    }

    @Test
    fun findById() {

        `when`(sentenceRepository.findById("0")).thenReturn(Mono.just(
                Sentence("0", arrayOf(
                        Word("Kawa", WordCategory.NOUN),
                        Word("is", WordCategory.VERB),
                        Word("best", WordCategory.ADJECTIVE)
                ), 1)))
        `when`(sentenceRepository.save(any<Sentence>())).thenAnswer { Mono.just(it.arguments[0]) }

        StepVerifier.create(testService.findById("0"))
                .consumeNextWith {
                    assertEquals("0", it.id, "Wrong ID")
                    assertEquals("Kawa is best", it.text, "Wrong text")
                    assertEquals(2, it.views, "Wrong views")
                }
                .verifyComplete()

        verify(sentenceRepository, times(1)).findById(eq("0"))
        verify(sentenceRepository, times(1)).save(any())

    }

    @Test
    fun findById_empty() {

        `when`(sentenceRepository.findById("0")).thenReturn(Mono.empty())

        StepVerifier.create(testService.findById("0"))
                .verifyComplete()

        verify(sentenceRepository, times(1)).findById(eq("0"))
        verify(sentenceRepository, times(0)).save(any())

    }

    @Test
    fun findByIdYodaTalk() {

        `when`(sentenceRepository.findById("0")).thenReturn(Mono.just(
                Sentence("0", arrayOf(
                        Word("Kawa", WordCategory.NOUN),
                        Word("is", WordCategory.VERB),
                        Word("best", WordCategory.ADJECTIVE)
                ), 1)))
        `when`(sentenceRepository.save(any<Sentence>())).thenAnswer { Mono.just(it.arguments[0]) }

        StepVerifier.create(testService.findByIdYodaTalk("0"))
                .consumeNextWith {
                    assertEquals("0", it.id, "Wrong ID")
                    assertEquals("best Kawa is", it.text, "Wrong text")
                    assertEquals(2, it.views, "Wrong views")
                }
                .verifyComplete()

        verify(sentenceRepository, times(1)).findById(eq("0"))
        verify(sentenceRepository, times(1)).save(any())

    }

    @Test
    fun findByIdYodaTalk_empty() {

        `when`(sentenceRepository.findById("0")).thenReturn(Mono.empty())

        StepVerifier.create(testService.findByIdYodaTalk("0"))
                .verifyComplete()

        verify(sentenceRepository, times(1)).findById(eq("0"))
        verify(sentenceRepository, times(0)).save(any())

    }

    @Test
    fun generateSentence() {

        mockWords()
        `when`(sentenceRepository.insert(any<Sentence>())).thenAnswer { i ->
            val s = i.arguments[0] as Sentence
            Mono.just(Sentence("123", s.words, s.views, s.created))
        }

        StepVerifier.create(testService.generateSentence())
                .consumeNextWith {
                    assertEquals("123", it.id, "Wrong 1st item ID")
                    assertEquals("kawa is best", it.text, "Wrong 1st item text")
                    assertEquals(0, it.views, "Wrong 1st item views")
                }
                .verifyComplete()

        verify(sentenceRepository, times(1)).insert(any<Sentence>())

    }

    @Test
    fun generateSentence_missing_words_type() {

        mockWords_missing_category()

        StepVerifier.create(testService.generateSentence())
                .expectErrorMessage("No word for 'VERB' category")
                .verify()

        verify(sentenceRepository, times(0)).insert(any<Sentence>())

    }

    private fun mockWords() {
        mockWordCategory(WordCategory.NOUN,
                Word("kawa", WordCategory.NOUN),
                Word("scuba", WordCategory.NOUN))

        mockWordCategory(WordCategory.VERB,
                Word("is", WordCategory.VERB),
                Word("are", WordCategory.VERB))

        mockWordCategory(WordCategory.ADJECTIVE,
                Word("best", WordCategory.ADJECTIVE),
                Word("wet", WordCategory.ADJECTIVE))
    }

    private fun mockWords_missing_category() {
        mockWordCategory(WordCategory.NOUN,
                Word("kawa", WordCategory.NOUN),
                Word("scuba", WordCategory.NOUN))

        mockWordCategory(WordCategory.VERB)

        mockWordCategory(WordCategory.ADJECTIVE)
    }

    private fun mockWordCategory(category: WordCategory, vararg words: Word) {
        `when`(wordRepository.countByCategory(eq(category))).thenReturn(Mono.just(words.size.toLong()))

        if (words.isNotEmpty()) {
            `when`(wordRepository.findFirstByCategory(eq(category), any()))
                    .thenReturn(Flux.just(words[0]))
        }
    }
}