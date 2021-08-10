package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.GridEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GridRepository: CrudRepository<GridEntity, Long> {
    fun findById(id: UUID): GridEntity?
    fun findAllByWorkspaceId(id: UUID): Iterable<GridEntity>
}