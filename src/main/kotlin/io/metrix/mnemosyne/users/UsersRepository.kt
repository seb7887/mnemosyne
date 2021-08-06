package io.metrix.mnemosyne.users

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UsersRepository: CrudRepository<UsersEntity, Long> {
    fun findById(id: UUID): UsersEntity?
    fun findByEmail(email: String): UsersEntity?
}