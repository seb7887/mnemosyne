package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.WorkspacesEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorkspacesRepository: CrudRepository<WorkspacesEntity, Long> {
    fun findById(id: UUID): WorkspacesEntity?
}