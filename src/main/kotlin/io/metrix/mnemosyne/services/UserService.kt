package io.metrix.mnemosyne.services

import io.metrix.mnemosyne.entities.UserEntity
import io.metrix.mnemosyne.repositories.UserRepository
import mnemosyne.UserResponse
import mnemosyne.UsersResponse
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class UserService(private val userRepository: UserRepository) {
    private val targetNotFound: String = "Target user not found"

    fun createUser(username: String, email: String, password: String, role: String, workspace: UUID?): UserResponse {
        val newUser = UserEntity(
            username = username,
            email = email,
            hash = hashPassword(password),
            role = role
        )

        if (workspace != null) {
            newUser.currentWorkspace = workspace
        }

        userRepository.save(newUser)

        return buildUserResponse(newUser)
    }

    fun login(email: String, password: String): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw Exception("User not found")
        if (!comparePassword(password, user.hash)) {
            throw Exception("Invalid password")
        }

        // Update lastLogin
        user.lastLogin = OffsetDateTime.now()
        userRepository.save(user)

        return buildUserResponse(user)
    }

    fun findById(id: UUID): UserEntity? {
        return userRepository.findById(id)
    }

    fun findUserById(id: UUID): UserResponse {
        val user = findById(id) ?: throw Exception("User not found")

        return buildUserResponse(user)
    }

    fun findUsersByWorkspace(workspace: UUID): UsersResponse {
        val users = userRepository.findAllByCurrentWorkspace(workspace)
            .map { buildUserResponse(it) }

        return buildUsersResponse(users)
    }

    fun update(id: UUID, email: String, username: String, picture: String, role: String, workspace: UUID?, grid: UUID?): UserResponse {
        val user = findById(id) ?: throw Exception(targetNotFound)
        user.email = email
        user.username = username
        user.picture = picture
        user.role = role

        if (workspace != null) {
            user.currentWorkspace = workspace
        }

        if (grid != null) {
            user.currentGrid = grid
        }

        user.updatedAt = OffsetDateTime.now()

        userRepository.save(user)

        return buildUserResponse(user)
    }

    fun updatePwd(id: UUID, newPassword: String) {
        val user = findById(id) ?: throw Exception(targetNotFound)

        user.hash = hashPassword(newPassword)

        userRepository.save(user)
    }

    fun delete(id: UUID) {
        val user = findById(id) ?: throw Exception(targetNotFound)

        userRepository.delete(user)
    }

    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    private fun comparePassword(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }

    private fun buildUserResponse(user: UserEntity): UserResponse {
        return UserResponse.newBuilder()
            .setId(user.id.toString())
            .setEmail(user.email)
            .setUsername(user.username)
            .setRole(user.role)
            .setPicture(user.picture ?: "")
            .setCurrentGrid(user.currentGrid.toString())
            .setCurrentWorkspace(user.currentWorkspace.toString())
            .setLastLogin(user.lastLogin.toString())
            .setUpdatedAt(user.updatedAt.toString())
            .build()
    }

    private fun buildUsersResponse(users: List<UserResponse>): UsersResponse {
        return UsersResponse.newBuilder()
            .addAllUsers(users)
            .setTotal(users.count().toLong())
            .build()
    }
}