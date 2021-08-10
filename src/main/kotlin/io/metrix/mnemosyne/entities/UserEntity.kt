package io.metrix.mnemosyne.entities

import org.hibernate.annotations.GenericGenerator
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users")
class UserEntity (
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null,

    @get:Column(nullable = false)
    var username: String,

    @get:Column(nullable = false, unique = true)
    var email: String,

    @get:Column(nullable = false)
    var hash: String,

    @get:Column
    var picture: String? = null,

    @get:Column(nullable = false)
    var role: String,

    @get:Column(name = "current_workspace")
    var currentWorkspace: UUID? = null,

    @get:Column(name = "current_grid")
    var currentGrid: UUID? = null,

    @get:Column(name = "last_login")
    var lastLogin: OffsetDateTime = OffsetDateTime.now(),

    @get:Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @get:Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)