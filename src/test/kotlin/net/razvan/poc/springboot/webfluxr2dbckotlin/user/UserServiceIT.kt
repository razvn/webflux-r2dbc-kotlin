package net.razvan.poc.springboot.webfluxr2dbckotlin.user

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import net.razvan.poc.springboot.webfluxr2dbckotlin.toDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import reactor.kotlin.core.publisher.toFlux
import java.util.stream.Stream


@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DataR2dbcTest
internal class UserServiceIT(
    @Autowired private val connectionFactory: ConnectionFactory,
    @Autowired private val repo: UserRepository
) {
    private lateinit var service: UserService

    init {
        val resourceLoader: ResourceLoader = DefaultResourceLoader()
        val scripts = arrayOf(
            resourceLoader.getResource("classpath:schema.sql"),
            resourceLoader.getResource("classpath:data.sql")
        )
        ResourceDatabasePopulator(*scripts).execute(connectionFactory).block()
    }

    @BeforeAll
    fun beforeAll() {
        initDatabase()
        service = UserService(repo)
    }

    @Test
    @Order(1)
    fun `findAll returns values`() {
        runBlocking {
            val resp = service.findAll()
            assertThat(resp.count()).isEqualTo(3)
        }
    }

    @Test
    fun `findById returns a value`() {
        runBlocking {
            //get user1 id
            val dbUser = service.findByEmail("user1@users.com").first()
            val resp = service.findById(dbUser.id!!)
            assertThat(resp).isNotNull()
            assertThat(resp?.name).isEqualTo("User1")
        }
    }

    @Test
    fun `findById returns a null if value does not exists`() {
        runBlocking {
            val resp = service.findById(999)
            assertThat(resp).isNull()
        }
    }

    @Test
    fun `findByEmail returns a value`() {
        runBlocking {
            val resp = service.findByEmail("user2@users.com")
            assertThat(resp.count()).isEqualTo(1)
            resp.map {
                assertThat(it.name).isEqualTo("User2")
            }
        }
    }

    @Test
    fun `findByEmail returns a null if email does not exists`() {
        runBlocking {
            val resp = service.findByEmail("unknown@users.com")
            assertThat(resp.count()).isEqualTo(0)
        }
    }

    @Test
    fun `addOne adds a user`() {
        runBlocking {
            val newUser = UserDTO("New user", "newuser", "newuser@users.com")
            val resp = service.addOne(newUser)
            assertThat(resp).isNotNull()
            assertThat(resp?.id).isNotNull()
            val dbUser = service.findByEmail(newUser.email).first()
            assertThat(dbUser).isNotNull()
        }
    }

    @Test
    fun `updateOne update a user`() {
        runBlocking {
            val existingUser = service.findByEmail("user3@users.com").first()

            assertThat(existingUser).isNotNull()

            val resp = service.updateOne(existingUser!!.id!!, existingUser.toDto(avatar = "updateduser3.png"))
            assertThat(resp).isNotNull()
            assertThat(resp?.id).isNotNull()
            val dbUser = service.findByEmail(existingUser.email).first()
            assertThat(dbUser).isNotNull()
            assertThat(dbUser!!.avatar).isEqualTo("updateduser3.png")

        }
    }

    @Test
    fun `updateOne inexisting user returns null`() {
        runBlocking {
            val inexistingUser = UserDTO("Inexistent", "idonotexist", "notauser@users.con")

            val resp = service.updateOne(999, inexistingUser)
            assertThat(resp).isNull()
        }
    }

    @Test
    fun `deleteOne deletes existing user return true`() {
        runBlocking {
            // add a new user
            val tmpUser = UserDTO("Delete user", "deleteuser", "deleteuser@users.com")
            val respAdd = service.addOne(tmpUser)
            assertThat(respAdd).isNotNull()
            assertThat(respAdd?.id).isNotNull()
            val resp = service.deleteOne(respAdd!!.id!!)
            assertThat(resp).isTrue()

            val dbUser = service.findByEmail(tmpUser.email)
            assertThat(dbUser.count()).isEqualTo(0)
        }
    }

    @Test
    fun `deleteOne deletes inexisting user return false`() {
        runBlocking {
            // add a new user
            val resp = service.deleteOne(9999)
            assertThat(resp).isFalse()
        }
    }

    private fun initDatabase() {
        repo.deleteAll().subscribe()

        val initData = Stream.of(
            User(null, "User1", "user01", "user1@users.com", "user1.png"),
            User(null, "User2", "user02", "user2@users.com", "user2.png"),
            User(null, "User3", "user03", "user3@users.com", "user3.png")
        )
        val saveAll = repo.saveAll(initData.toFlux())
        saveAll.subscribe()
    }

}