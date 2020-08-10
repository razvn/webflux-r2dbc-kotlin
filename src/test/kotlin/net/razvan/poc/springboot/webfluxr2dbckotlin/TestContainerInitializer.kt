package net.razvan.poc.springboot.webfluxr2dbckotlin

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.PostgreSQLContainer


class TestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val env = applicationContext.environment

        val confdb = env.getProperty("app.test.testcontainer.db", "")

        val container: TestContainerInterface? = when(confdb) {
            "mysql" -> TestMySQLContainer
            "postgres" -> TestPostgresContainer
            else -> null
        }

        println("*** Container: $confdb ***")

        container?.let {
            it.start()

            val conf = mapOf(
                "spring.datasource.url" to it.getJdbcUrl(),
                "spring.datasource.username" to it.getUsername(),
                "spring.datasource.password" to it.getPassword()
            )

            val testContainers = MapPropertySource("testcontainers", conf)

            env.propertySources.addFirst(testContainers)
        }
    }
}

interface TestContainerInterface {
    fun start()
    fun getJdbcUrl(): String
    fun getUsername(): String
    fun getPassword(): String
}

object TestPostgresContainer : TestContainerInterface, PostgreSQLContainer<TestPostgresContainer>("postgres:9.5") {
    override fun stop() {
        //do nothing jvm handles it
    }
}

object TestMySQLContainer : TestContainerInterface, MySQLContainer<TestMySQLContainer>("mysql:8") {
    override fun stop() {
        //do nothing jvm handles it
    }
}

