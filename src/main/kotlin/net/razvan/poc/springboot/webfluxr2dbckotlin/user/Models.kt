package net.razvan.poc.springboot.webfluxr2dbckotlin.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

// data models
@Table("users")
data class User(
        @Id
        val id: Long? = null,
        val name: String,
        val login: String,
        val email: String,
        val avatar: String? = null
)


// rest models
data class ErrorMessage(val message: String)

data class UserDTO(
        val name: String,
        val login: String,
        val email: String,
        val avatar: String? = null
)

fun UserDTO.toModel(withId: Long? = null) = User(withId, this.name, this.login, this.email, this.avatar)