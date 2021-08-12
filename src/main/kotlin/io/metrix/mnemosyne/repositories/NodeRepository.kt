package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.NodeEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface NodeRepository: CrudRepository<NodeEntity, Long> {
    fun findById(id: UUID): NodeEntity?
    fun findByName(name: String): NodeEntity?
    fun findAllByGridId(id: UUID): Iterable<NodeEntity>

    @Transactional
    @Modifying
    @Query(
        value = "INSERT INTO nodes(name, grid_id, location, created_by, updated_by) values (:name, :gridId, st_setsrid(st_point(:lon,:lat), 4326), :createdBy, :updatedBy)",
        nativeQuery = true,
    )
    fun create(name: String, gridId: UUID, lat: Double, lon: Double, createdBy: UUID, updatedBy: UUID)
}