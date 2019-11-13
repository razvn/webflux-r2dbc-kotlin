package net.razvan.poc.springboot.webfluxr2dbckotlin.user

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import net.razvan.poc.springboot.webfluxr2dbckotlin.createUser
import net.razvan.poc.springboot.webfluxr2dbckotlin.toDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

internal class UserHandlerTest {

    private val service = mockk<UserService>()
    private val handler  = UserHandler(service)
    private val request = mockk<ServerRequest>()

    @Test
    fun `users exist findAll retuns OK`() {
        coEvery { service.findAll() } returns flowOf(createUser(id = 1), createUser(id=2))

        runBlocking {
            val response = handler.findAll(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)

        }
    }

    @Test
    fun `users does not exist findAll retuns OK`() {
        coEvery { service.findAll() } returns emptyFlow<User>()

        runBlocking {
            val response = handler.findAll(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)

        }
    }

    @Test
    fun `existing user findUser returns OK`() {

        coEvery { service.findById(1) } returns createUser(id = 1)
        every { request.pathVariable("id") } returns "1"

        runBlocking {
            val response = handler.findUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `inexisting user findUser returns NotFound`() {

        coEvery { service.findById(999) } returns null
        every { request.pathVariable("id") } returns "999"

        runBlocking {
            val response = handler.findUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }


    @Test
    fun `path variable not a number findUser returns BadRequest`() {

        every { request.pathVariable("id") } returns "ABC"

        runBlocking {
            val response = handler.findUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `adding a user addUser returns OK`() {
        coEvery { service.addOne(any()) } answers {
            firstArg<UserDTO>().toModel().copy(id = 999)
        }
        every { request.bodyToMono<UserDTO>() } returns createUser().toDto().toMono()

        runBlocking {
            val response = handler.addUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED)
        }
    }

    @Test
    fun `invalid body addUser returns BadRequest`() {
        every { request.bodyToMono<UserDTO>() } returns Mono.empty()

        runBlocking {
            val response = handler.addUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `error on save addUser returns InternalServerError`() {
        coEvery { service.addOne(any()) } returns null
        every { request.bodyToMono<UserDTO>() } returns createUser().toDto().toMono()

        runBlocking {
            val response = handler.addUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @Test
    fun `existing user updateUser returns OK`() {
        every { request.pathVariable("id") } returns "2"
        coEvery { service.updateOne(2, any()) } answers {
            secondArg<UserDTO>().toModel(firstArg<Long>())
        }
        every { request.bodyToMono<UserDTO>() } returns createUser().toDto().toMono()

        runBlocking {
            val response = handler.updateUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK)
        }
    }

    @Test
    fun `id not a number updateUser returns BadRequest`() {
        every { request.pathVariable("id") } returns "BAD"

        runBlocking {
            val response = handler.updateUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `bad body updateUser returns BadRequest`() {
        every { request.pathVariable("id") } returns "2"
        every { request.bodyToMono<UserDTO>() } returns Mono.empty()

        runBlocking {
            val response = handler.updateUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }

    @Test
    fun `inexisting user updateUser returns NotFound`() {
        every { request.pathVariable("id") } returns "2"
        every { request.bodyToMono<UserDTO>() } returns createUser().toDto().toMono()
        coEvery { service.updateOne(2, any()) } returns null

        runBlocking {
            val response = handler.updateUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `success deleteUser returns NoContent`() {
        every { request.pathVariable("id") } returns "2"
        coEvery { service.deleteOne(2) } returns true

        runBlocking {
            val response = handler.deleteUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT)
        }
    }

    @Test
    fun `inexistent id deleteUser returns NotFound`() {
        every { request.pathVariable("id") } returns "2"
        coEvery { service.deleteOne(2) } returns false

        runBlocking {
            val response = handler.deleteUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Test
    fun `id not a number deleteeUser returns BadRequest`() {
        every { request.pathVariable("id") } returns "BAD"

        runBlocking {
            val response = handler.deleteUser(request)
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
        }
    }
}