package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.UsersEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UsersRepository: CrudRepository<UsersEntity, Long> {
    fun findById(id: UUID): UsersEntity?
    fun findByEmail(email: String): UsersEntity?
    fun findAllByCurrentWorkspace(id: UUID): Iterable<UsersEntity>
}