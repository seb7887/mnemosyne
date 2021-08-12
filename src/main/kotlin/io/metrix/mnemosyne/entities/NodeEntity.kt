package io.metrix.mnemosyne.entities

import org.hibernate.annotations.GenericGenerator
import org.locationtech.jts.geom.Geometry

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "nodes")
class NodeEntity (
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null,

    @get:Column(nullable = false)
    var name: String,

    @get:Column(nullable = false)
    var active: Boolean = true,

    @get:Column(name = "grid_id", nullable = false)
    var gridId: UUID,

    @get:Column(nullable = false)
    var location: Geometry,

    @get:Column(name = "created_by", nullable = false)
    val createdBy: UUID,

    @get:Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @get:Column(name = "updated_by", nullable = false)
    var updatedBy: UUID,

    @get:Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)