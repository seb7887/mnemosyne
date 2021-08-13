package io.metrix.mnemosyne.repositories

import io.metrix.mnemosyne.entities.DeviceEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface DeviceRepository: CrudRepository<DeviceEntity, Long> {
    fun findById(id: UUID): DeviceEntity?
    fun findByApiKey(apiKey: String): DeviceEntity?
    fun findAllByNodeId(id: UUID): Iterable<DeviceEntity>
}