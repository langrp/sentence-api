package com.gooddata.example

import com.gooddata.example.data.Word
import com.gooddata.example.data.WordCategory
import com.gooddata.example.db.MongoExtension
import com.gooddata.example.message.SentenceAggregateMsg
import com.gooddata.example.message.SentenceMsg
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(MongoExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = [ "spring.data.mongodb.host=localhost",
			"spring.data.mongodb.port=27017",
			"com.gooddata.sentence-api.forbidden_words_file=src/integrationTest/resources/forbidden_words.txt" ]
)
class SentenceApiApplicationTests {

	@LocalServerPort
	private val port = 0

	@Autowired
	private lateinit var restTemplate: TestRestTemplate


	@Test
	@Order(0)
	fun addWords() {
		restTemplate.put("/words", Word("kawa", WordCategory.NOUN))
		restTemplate.put("/words", Word("scuba", WordCategory.NOUN))

		restTemplate.put("/words", Word("is", WordCategory.VERB))
		restTemplate.put("/words", Word("are", WordCategory.VERB))

	}

	@Test
	@Order(3)
	fun generateSentence_missing_category() {

		val response = restTemplate.postForEntity("/sentences/generate", null, String::class.java)

		assertEquals(500, response.statusCodeValue, "Response code")
		assertThatJson(response.body!!)
				.isObject
				.containsEntry("message", "No word for 'ADJECTIVE' category")
				.containsEntry("error", "Internal Server Error")

	}

	@Test
	@Order(6)
	fun addMissingWords() {
		restTemplate.put("/words", Word("best", WordCategory.ADJECTIVE))
		restTemplate.put("/words", Word("wet", WordCategory.ADJECTIVE))
	}

	@Test
	@Order(9)
	fun addWords_duplicates() {

		val response = restTemplate.exchange(
				"/words",
				HttpMethod.PUT,
				HttpEntity(Word("is", WordCategory.VERB)),
				String::class.java)

		assertEquals(400, response.statusCodeValue, "Response status")
		assertNotNull(response.body, "No body")
		assertThatJson(response.body!!)
				.isObject
				.containsEntry("message", "Duplicated word")
				.containsEntry("error", "Bad Request")

	}

	@Test
	@Order(12)
	fun addWords_forbidden() {

		val response = restTemplate.exchange(
				"/words",
				HttpMethod.PUT,
				HttpEntity(Word("test", WordCategory.NOUN)),
				String::class.java)

		assertEquals(400, response.statusCodeValue, "Response status")
		assertNotNull(response.body, "No body")
		assertThatJson(response.body!!)
				.isObject
				.containsEntry("message", "Forbidden word")
				.containsEntry("error", "Bad Request")

	}

	@Test
	@Order(15)
	fun getWords() {

		val wordsResponse = restTemplate.getForEntity("/words", Array<Word>::class.java)

		assertEquals(200, wordsResponse.statusCodeValue, "Response status")
		assertNotNull(wordsResponse.body, "Response body")
		assertEquals(6, wordsResponse.body?.size, "Words count")

	}

	@Test
	@Order(18)
	fun generateSentence() {

		val response = restTemplate.postForEntity("/sentences/generate", null, SentenceMsg::class.java)

		assertEquals(200, response.statusCodeValue, "Response status")
		assertNotNull(response.body, "No body - sentence expected")
		assertEquals(0L, response.body?.views, "Sentence views")
		val sentence = response.body?.text?.split(" ")!!
		assertEquals(3, sentence.size, "Words in sentence")
		assertTrue(sentence[0] == "scuba" || sentence[0] == "kawa", "Wrong noun in '$sentence'")
		assertTrue(sentence[1] == "is" || sentence[1] == "are", "Wrong verb in '$sentence'")
		assertTrue(sentence[2] == "wet" || sentence[2] == "best", "Wrong adjective in '$sentence'")

	}

	@Test
	@Order(21)
	fun getSentenceYodaTalk() {

		val sentences = restTemplate.getForEntity("/sentences", Array<SentenceMsg>::class.java)

		val sentenceId = sentences.body?.get(0)?.id!!

		val response = restTemplate.getForEntity("/sentences/$sentenceId/yodaTalk", SentenceMsg::class.java)

		assertEquals(200, response.statusCodeValue, "Response status")
		assertNotNull(response.body, "No body - sentence expected")
		assertEquals(1L, response.body?.views, "Sentence views")
		val sentence = response.body?.text?.split(" ")!!
		assertEquals(3, sentence.size, "Words in sentence")
		assertTrue(sentence[1] == "scuba" || sentence[1] == "kawa", "Wrong noun in '$sentence'")
		assertTrue(sentence[2] == "is" || sentence[2] == "are", "Wrong verb in '$sentence'")
		assertTrue(sentence[0] == "wet" || sentence[0] == "best", "Wrong adjective in '$sentence'")

	}

	@Test
	@Order(24)
	fun getDuplicates_empty() {

		val response = restTemplate.getForEntity("/sentences/duplicates", String::class.java)

		assertEquals(404, response.statusCodeValue, "Response status")

	}

	@Test
	@Order(27)
	fun generateDuplicates() {

		restTemplate.postForEntity("/sentences/generate", null, SentenceMsg::class.java)
		restTemplate.postForEntity("/sentences/generate", null, SentenceMsg::class.java)
		restTemplate.postForEntity("/sentences/generate", null, SentenceMsg::class.java)
		restTemplate.postForEntity("/sentences/generate", null, SentenceMsg::class.java)

		val response = restTemplate.getForEntity("/sentences/duplicates", Array<SentenceAggregateMsg>::class.java)

		assertEquals(200, response.statusCodeValue, "Response status")

	}

}
