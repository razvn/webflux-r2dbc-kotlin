package net.razvan.poc.springboot.webfluxr2dbckotlin.user

import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class UserService(private val repo: UserRepository) {

    suspend fun findAll() = repo.findAll().asFlow()
    suspend fun findById(id: Long) = repo.findById(id).awaitFirstOrNull()
    suspend fun findByEmail(email: String) = repo.findByEmail(email).asFlow()
    suspend fun addOne(user: UserDTO) = repo.save(user.toModel()).awaitFirstOrNull()
    suspend fun updateOne(id: Long, user: UserDTO): User? {
        val existingUser = findById(id)
        return if (existingUser != null) repo.save(user.toModel(withId = id)).awaitFirstOrNull() else null
    }

    suspend fun deleteOne(id: Long): Boolean {
        val existingUser = findById(id)
        return if (existingUser != null) {
            repo.delete(existingUser).awaitFirstOrNull()
            true
        } else false
    }

}
