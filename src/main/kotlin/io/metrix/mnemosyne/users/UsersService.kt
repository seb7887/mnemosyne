package io.metrix.mnemosyne.users

import io.grpc.stub.StreamObserver
import mnemosyne.*
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class UsersService : MnemosyneGrpc.MnemosyneImplBase() {
    override fun signUp(request: NewUserReq?, responseObserver: StreamObserver<UserResponse>?) {
        val response = UserResponse.newBuilder()
            .setId("1")
            .setEmail(request!!.email)
            .setUsername(request!!.username)
            .setRole("admin")
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

    override fun login(request: LoginReq?, responseObserver: StreamObserver<LoginResponse>?) {
        val response = LoginResponse.newBuilder()
            .setAuthenticated(true)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}
