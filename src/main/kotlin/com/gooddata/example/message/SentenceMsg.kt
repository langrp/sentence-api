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

package com.gooddata.example.message

import com.gooddata.example.data.Sentence

/**
 *
 * @author petr.langr
 * @since 1.0.0
 */
data class SentenceMsg(
        val id: String,
        val text: String,
        val views: Long
) {

    companion object {

        fun of(sentence: Sentence): SentenceMsg {
            return SentenceMsg(
                    sentence.id!!,
                    sentence.words.joinToString(" ") { w -> w.word },
                    sentence.views)
        }

        fun yoda(sentence: Sentence): SentenceMsg {
            val text = sentence.words.asSequence()
                    .sortedBy { w -> w.category.yodaOrder }
                    .joinToString(" ") { w -> w.word }

            return SentenceMsg(
                    sentence.id!!,
                    text,
                    sentence.views)
        }

    }
}