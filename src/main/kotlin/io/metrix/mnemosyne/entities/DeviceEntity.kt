package io.metrix.mnemosyne.entities

import org.hibernate.annotations.GenericGenerator
import org.locationtech.jts.geom.Geometry
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "devices")
class DeviceEntity (
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null,

    @get:Column(name = "api_key", nullable = false)
    val apiKey: String,

    @get:Column(name = "type", nullable = false)
    val type: String,

    @get:Column(name = "node_id")
    var nodeId: UUID? = null,

    @get:Column
    var location: Geometry? = null,

    @get:Column(name = "created_by", nullable = false)
    val createdBy: UUID,

    @get:Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @get:Column(name = "updated_by", nullable = false)
    var updatedBy: UUID,

    @get:Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)