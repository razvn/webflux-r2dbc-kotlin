package net.razvan.poc.springboot.webfluxr2dbckotlin

import io.r2dbc.spi.ConnectionFactory
import net.razvan.poc.springboot.webfluxr2dbckotlin.user.User
import net.razvan.poc.springboot.webfluxr2dbckotlin.user.UserDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest
@AutoConfigureWebTestClient
class WebfluxR2dbcKotlinApplicationIT(
    @Autowired val client: WebTestClient,
    @Autowired val connectionFactory: ConnectionFactory
) {

    private val usersList = listOf(
        User(1, "Test no 1", "test1", "test1@users.com", "test1.png"),
        User(2, "Test no 2", "test2", "test2@users.com", "test2.png"),
        User(3, "Test no 3", "test3", "test3@users.com", "test3.png"),
        User(4, "Test no 4", "test4", "test4@users.com", "test4.png")
    )

    init {
        val resourceLoader: ResourceLoader = DefaultResourceLoader()
        val scripts = arrayOf(
            resourceLoader.getResource("classpath:schema.sql"),
            resourceLoader.getResource("classpath:data.sql")
        )
        ResourceDatabasePopulator(*scripts).execute(connectionFactory).block()
    }

    @Nested
    inner class Find {
        @Test
        fun `list of users`() {
            val response = client.get()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBodyList<User>()
                .hasSize(usersList.size)
                .returnResult()
                .responseBody

            assertThat(response)
                .isNotNull()
                .allSatisfy { assertThat(it).isIn(usersList) }
        }

        @Test
        fun `existing user returns OK`() {
            val expectedUser = usersList.firstOrNull { it.id != null && it.id == 1L }
            assertThat(expectedUser).isNotNull()

            val response = client.get()
                .uri("/users/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody

            assertThat(response)
                .isNotNull()
                .isEqualTo(expectedUser)
        }

        @Test
        fun `inexisting user returns NotFound`() {
            client.get()
                .uri("/users/111")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound

        }

        @Test
        fun `id not a number returns BadRequest`() {
            client.get()
                .uri("/users/abc")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("`id` must be numeric")

        }
    }

    @Nested
    inner class Search {

        @Test
        fun `returns OK`() {
            val response = client.get()
                .uri("/users/search?email=test2@users.com")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBodyList<User>()
                .hasSize(1)
                .returnResult()
                .responseBody

            assertThat(response)
                .isNotNull()
                .allSatisfy { user ->
                    assertThat(user).isIn(
                        usersList.filter { it.email == "test2@users.com" }
                    )
                }
        }

        @Test
        fun `empty email value returns BadRequest`() {
            client.get()
                .uri("/users/search?email=")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("Incorrect search criteria value")

        }

        @Test
        fun `empty search returns BadRequest`() {
            client.get()
                .uri("/users/search?email=")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("Incorrect search criteria value")

        }

        @Test
        fun `no search returns BadRequest`() {
            client.get()
                .uri("/users/search")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("Search must have query params")

        }
    }

    @Nested
    inner class Add {
        @Test
        fun `returns OK`() {
            val newUser = UserDTO("New Test", "newtest", "newtest@users.com", "testnew.png")

            val response = client.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUser))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated
                .expectBody(User::class.java)
                .returnResult()
                .responseBody

            assertThat(response)
                .isNotNull()
                .isEqualToComparingOnlyGivenFields(newUser, "name", "login", "email", "avatar")
        }

        @Test
        fun `bad format returns BadRequest`() {
            val newUser = "bad format"

            client.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUser))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid body")
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `user exists returns OK`() {
            val updateUser = usersList.firstOrNull { it.id != null && it.id == 2L }?.toDto(avatar = "updatedavatar.png")
            assertThat(updateUser).isNotNull()

            val response = client.put()
                .uri("/users/2")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateUser!!))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBody(User::class.java)
                .returnResult()
                .responseBody

            assertThat(response)
                .isNotNull()
                .isEqualToComparingOnlyGivenFields(updateUser, "name", "login", "email", "avatar")
        }

        @Test
        fun `id not a number returns BadRequest`() {
            val updateUser = usersList.firstOrNull { it.id != null && it.id == 2L }?.toDto(avatar = "updatedavatar.png")
            assertThat(updateUser).isNotNull()

            client.put()
                .uri("/users/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateUser!!))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("`id` must be numeric")

        }

        @Test
        fun `bad format returns BadRequest`() {
            val updateUser = "bad format"

            client.put()
                .uri("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateUser))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid body")
        }

        @Test
        fun `inexisting user returns NotFound`() {
            val updateUser = usersList.firstOrNull { it.id != null && it.id == 2L }?.toDto(avatar = "updatedavatar.png")
            assertThat(updateUser).isNotNull()

            client.put()
                .uri("/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updateUser!!))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.message").isEqualTo("Resource 999 not found")
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `returns Ok`() {

            client.delete()
                .uri("/users/3")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent
        }

        @Test
        fun `inexisting user returns NotFound`() {

            client.delete()
                .uri("/users/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$.message").isEqualTo("Resource 999 not found")
        }

        @Test
        fun `id not a number returns BadRequest`() {
            client.delete()
                .uri("/users/abc")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest
                .expectBody()
                .jsonPath("$.message").isEqualTo("`id` must be numeric")

        }
    }
}