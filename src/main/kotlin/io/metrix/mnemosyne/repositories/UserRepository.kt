package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository: CrudRepository<UserEntity, Long> {
    fun findById(id: UUID): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun findAllByCurrentWorkspace(id: UUID): Iterable<UserEntity>
}