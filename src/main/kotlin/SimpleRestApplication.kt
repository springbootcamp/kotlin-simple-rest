package org.springbootcamp.kotlin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.CrudRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.persistence.*
import javax.validation.ConstraintViolationException


@SpringBootApplication
open class SimpleRestApplication

@Controller
class SimpleController {
    @Value("\${spring.application.name}")
    internal var appName: String? = null

    @RequestMapping("/")
    fun homePage(model: Model): String {
        model.addAttribute("appName", appName)
        return "home"
    }
}

@Repository
interface BookRepository : CrudRepository<Book, Long> {
    fun findByTitle(title: String): List<Book>
}

@Entity
class Book(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,

        @Column(nullable = false, unique = true)
        var title: String? = null,

        @Column(nullable = false)
        var author: String? = null
)

@RestController
@RequestMapping("/api/books")
class BookController {

    @Autowired
    private val bookRepository: BookRepository? = null

    @GetMapping
    fun findAll(): Iterable<*> {
        return bookRepository!!.findAll()
    }

    @GetMapping("/title/{bookTitle}")
    fun findByTitle(@PathVariable bookTitle: String): List<*> {
        return bookRepository!!.findByTitle(bookTitle)
    }

    @GetMapping("/{id}")
    fun findOne(@PathVariable id: Long?): Book {
        return bookRepository!!.findOne(id) ?: throw BookNotFoundException("no book found with id=" + id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody book: Book): Book {
        return bookRepository!!.save(book)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long?) {
        val book = bookRepository!!.findOne(id) ?: throw BookNotFoundException("no book found with id=" + id)
        bookRepository.delete(id)
    }

    @PutMapping("/{id}")
    fun updateBook(@RequestBody book: Book, @PathVariable id: Long?): Book {
        if (book.id != id) {
            throw BookIdMismatchException("ids do not match!")
        }
        val old = bookRepository!!.findOne(id) ?: throw BookNotFoundException("no book found with id=" + id)
        return bookRepository.save(book)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(SimpleRestApplication::class.java, *args)
}

// Exceptions
@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(BookNotFoundException::class)
    protected fun handleNotFound(ex: Exception, request: WebRequest): ResponseEntity<*> {
        return handleExceptionInternal(ex, "Book not found",
                HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }

    @ExceptionHandler(BookIdMismatchException::class, ConstraintViolationException::class, DataIntegrityViolationException::class)
    fun handleBadRequest(ex: Exception, request: WebRequest): ResponseEntity<*> {
        return handleExceptionInternal(ex, ex.localizedMessage,
                HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }
}
class BookNotFoundException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class BookIdMismatchException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)