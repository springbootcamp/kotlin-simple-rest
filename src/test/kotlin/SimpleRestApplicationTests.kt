package org.springbootcamp.kotlin

import io.restassured.RestAssured
import io.restassured.response.Response
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(SimpleRestApplication::class), webEnvironment = DEFINED_PORT)
class LiveTest {


    private fun createRandomBook(): Book {
        val book = Book()
        book.title = RandomStringUtils.randomAlphabetic(10)
        book.author = RandomStringUtils.randomAlphabetic(15)
        return book
    }

    private fun createBookAsUri(book: Book): String {
        val response = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .post(API_ROOT)
        return API_ROOT + "/" + response.jsonPath().get("id")
    }

    companion object {

        private val API_ROOT = "http://localhost:8081/api/books"
    }

    @Test
    fun whenGetAllBooks_thenOK() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val response = RestAssured.get(API_ROOT)

        assertEquals(HttpStatus.OK.value(), response.statusCode)
    }

    @Test
    fun whenGetBooksByTitle_thenOK() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val book = createRandomBook()
        createBookAsUri(book)
        val response = RestAssured.get(
                API_ROOT + "/title/" + book.title)

        assertEquals(HttpStatus.OK.value(), response.statusCode)
        // assertTrue(response.`as`(List::class.java).size() > 0)
    }

    @Test
    fun whenGetCreatedBookById_thenOK() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val book = createRandomBook()
        val location = createBookAsUri(book)
        val response = RestAssured.get(location)

        assertEquals(HttpStatus.OK.value(), response.statusCode)
        assertEquals(book.title, response.jsonPath()
                .get("title"))
    }

    @Test
    fun whenGetNotExistBookById_thenNotFound() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val response = RestAssured.get(API_ROOT + "/" + randomNumeric(4))

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode)
    }

    @Test
    fun whenCreateNewBook_thenCreated() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val book = createRandomBook()
        val response = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .post(API_ROOT)

        assertEquals(HttpStatus.CREATED.value(), response.statusCode)
    }

    @Test
    fun whenInvalidBook_thenError() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val book = createRandomBook()
        book.author = null
        val response = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .post(API_ROOT)

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode)
    }

    @Test
    fun whenUpdateCreatedBook_thenUpdated() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val book = createRandomBook()
        val location = createBookAsUri(book)
        book.id = java.lang.Long.parseLong(location.split("api/books/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        book.author = "newAuthor"
        var response: Response = RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(book)
                .put(location)

        assertEquals(HttpStatus.OK.value(), response.getStatusCode())

        response = RestAssured.get(location)

        assertEquals(HttpStatus.OK.value(), response.statusCode)
        assertEquals("newAuthor", response.jsonPath()
                .get("author"))
    }

    @Test
    fun whenDeleteCreatedBook_thenOk() {
        RestAssured.authentication = RestAssured.preemptive().basic("john", "123")
        val book = createRandomBook()
        val location = createBookAsUri(book)
        var response = RestAssured.delete(location)

        assertEquals(HttpStatus.OK.value(), response.statusCode)

        response = RestAssured.get(location)
        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode)
    }
}
