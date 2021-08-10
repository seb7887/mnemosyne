package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.WorkspaceEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorkspaceRepository: CrudRepository<WorkspaceEntity, Long> {
    fun findById(id: UUID): WorkspaceEntity?
}