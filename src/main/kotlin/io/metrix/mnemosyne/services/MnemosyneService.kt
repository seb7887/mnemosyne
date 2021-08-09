package io.metrix.mnemosyne.services

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.metrix.mnemosyne.entities.UsersEntity
import io.metrix.mnemosyne.entities.WorkspacesEntity
import io.metrix.mnemosyne.repositories.UsersRepository
import io.metrix.mnemosyne.repositories.WorkspacesRepository
import mnemosyne.*
import org.lognet.springboot.grpc.GRpcService
import org.mindrot.jbcrypt.BCrypt
import java.time.OffsetDateTime
import java.util.*

@GRpcService
class MnemosyneService(
    private val usersRepository: UsersRepository,
    private val workspacesRepository: WorkspacesRepository) : MnemosyneGrpc.MnemosyneImplBase() {
    override fun signUp(request: NewUserReq?, responseObserver: StreamObserver<UserResponse>?) {
        try {
            val newUser = UsersEntity(
                username = request!!.username,
                email = request!!.email,
                hash = BCrypt.hashpw(request.password, BCrypt.gensalt()),
                role = request!!.role,
            )

            // If workspaceId is present, then assign the user to it
            val workspace = getUUID(request.workspaceId)
            if (workspace != null) {
                newUser.currentWorkspace = workspace
            }
            val user = usersRepository.save(newUser)

            val response = buildUserResponse(user)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun login(request: LoginReq?, responseObserver: StreamObserver<UserResponse>?) {
        try {
            val user = usersRepository.findByEmail(request!!.email) ?: throw Exception("User not found")
            if (!BCrypt.checkpw(request.password, user.hash)) {
                throw Exception("Invalid password")
            }

            // Update lastLogin
            user.lastLogin = OffsetDateTime.now()
            usersRepository.save(user)

            val response = buildUserResponse(user)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun getUser(request: GetEntityReq?, responseObserver: StreamObserver<UserResponse>?) {
        try {
            val user = usersRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("User not found")

            val response = buildUserResponse(user)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun getUsersByWorkspace(request: GetEntityByWorkspaceReq?, responseObserver: StreamObserver<UsersResponse>?) {
        try {
            val users = usersRepository.findAllByCurrentWorkspace(UUID.fromString(request!!.workspaceId))
                .map { buildUserResponse(it) }

            val response = buildUsersResponse(users)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun updateUser(request: ModifyUserReq?, responseObserver: StreamObserver<UserResponse>?) {
        try {
            val currentUser = usersRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Invalid current user")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            var user = usersRepository.findById(UUID.fromString(request.user.id)) ?: throw Exception("Unexistent target user")
            user.email = request.user.email
            user.username = request.user.username
            user.picture = request.user.picture
            user.role = request.user.role
            val workspace = getUUID(request.user.currentWorkspace)
            if (workspace != null) {
                user.currentWorkspace = UUID.fromString(request.user.currentWorkspace)
            }
            val grid = getUUID(request.user.currentGrid)
            if (grid != null) {
                user.currentGrid = UUID.fromString(request.user.currentGrid)
            }
            user.updatedAt = OffsetDateTime.now()

            usersRepository.save(user)

            val response = buildUserResponse(user)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun changePassword(request: ChangePasswordReq?, responseObserver: StreamObserver<StatusResponse>?) {
        try {
            val user = usersRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("Cannot find user")
            user.hash = BCrypt.hashpw(request.password, BCrypt.gensalt())

            usersRepository.save(user)

            val response = buildStatusResponse(true)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun deleteUser(request: ModifyUserReq?, responseObserver: StreamObserver<StatusResponse>?) {
        try {
            val currentUser = usersRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Invalid current user")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            val user = usersRepository.findById(UUID.fromString(request.user.id)) ?: throw Exception("Unexistent target user")
            usersRepository.delete(user)

            val response = buildStatusResponse(true)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    fun buildUserResponse(user: UsersEntity): UserResponse {
        return UserResponse.newBuilder()
            .setId(user.id.toString())
            .setEmail(user.email)
            .setUsername(user.username)
            .setRole(user.role)
            .setPicture(user.picture ?: "")
            .setCurrentGrid(user.currentGrid.toString() ?: "null")
            .setCurrentWorkspace(user.currentWorkspace.toString() ?: "null")
            .setLastLogin(user.lastLogin.toString() ?: "null")
            .setUpdatedAt(user.updatedAt.toString())
            .build()
    }

    fun buildUsersResponse(users: List<UserResponse>): UsersResponse {
        return UsersResponse.newBuilder()
            .addAllUsers(users)
            .build()
    }

    fun buildStatusResponse(status: Boolean): StatusResponse {
        return StatusResponse.newBuilder().setSuccess(status).build()
    }

    override fun createWorkspace(request: NewWorkspaceReq?, responseObserver: StreamObserver<WorkspaceResponse>?) {
        try {
            val newWorkspace = WorkspacesEntity(
                name = request!!.name
            )

            val workspace = workspacesRepository.save(newWorkspace)

            val response = buildWorkspaceResponse(workspace)

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e)
                    .asRuntimeException()
            )
        }
    }

    fun buildWorkspaceResponse(workspace: WorkspacesEntity): WorkspaceResponse {
        return WorkspaceResponse.newBuilder()
            .setId(workspace.id.toString())
            .setName(workspace.name)
            .setCreatedAt(workspace.createdAt.toString())
            .setUpdatedAt(workspace.updatedAt.toString())
            .build()
    }

    fun getUUID(value: String?): UUID? {
        try {
            return UUID.fromString(value)
        } catch (e: Exception) {
            return null
        }
    }
}
