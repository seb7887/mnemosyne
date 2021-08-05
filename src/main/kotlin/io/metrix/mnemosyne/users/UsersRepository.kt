package io.metrix.mnemosyne.users

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository: CrudRepository<UsersEntity, Long> {
}