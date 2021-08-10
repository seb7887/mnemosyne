package io.metrix.mnemosyne.entities

import org.hibernate.annotations.GenericGenerator
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "workspaces")
class WorkspaceEntity (
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null,

    @get:Column(nullable = false)
    var name: String,

    @get:Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @get:Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)