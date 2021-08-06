package io.metrix.mnemosyne.users

import io.grpc.Status
import io.grpc.stub.StreamObserver
import mnemosyne.*
import org.lognet.springboot.grpc.GRpcService
import org.mindrot.jbcrypt.BCrypt
import java.time.OffsetDateTime
import java.util.*

@GRpcService
class UsersService(private val usersRepository: UsersRepository) : MnemosyneGrpc.MnemosyneImplBase() {
    override fun signUp(request: NewUserReq?, responseObserver: StreamObserver<UserResponse>?) {
        val newUser = UsersEntity(
            username = request!!.username,
            email = request!!.email,
            hash = BCrypt.hashpw(request.password, BCrypt.gensalt()),
            role = "admin",
        )
        val user = usersRepository.save(newUser)

        val response = UserResponse.newBuilder()
            .setId(user.id.toString())
            .setEmail(user.email)
            .setUsername(user.username)
            .setRole(user.role)
            .setUpdatedAt(user.updatedAt.toString())
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
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

            val response = UserResponse.newBuilder()
                .setId(user.id.toString())
                .setEmail(user.email)
                .setUsername(user.username)
                .setRole(user.role)
                .setLastLogin(user.lastLogin.toString())
                .setUpdatedAt(user.updatedAt.toString())
                .build()

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

            val response = UserResponse.newBuilder()
                .setId(user.id.toString())
                .setEmail(user.email)
                .setUsername(user.username)
                .setRole(user.role)
                .setPicture(user.picture)
                .setCurrentWorkspace(user.currentWorkspace.toString())
                .setCurrentGrid(user.currentGrid.toString())
                .setLastLogin(user.lastLogin.toString())
                .setUpdatedAt(user.updatedAt.toString())
                .build()

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
}
