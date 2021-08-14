package io.metrix.mnemosyne.services

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.metrix.mnemosyne.interceptor.MetadataInterceptor
import mnemosyne.*
import org.lognet.springboot.grpc.GRpcService
import java.util.*

@GRpcService(interceptors = [MetadataInterceptor::class])
class MnemosyneService(
    private val userService: UserService,
    private val workspaceService: WorkspaceService,
    private val gridService: GridService,
    private val nodeService: NodeService,
    private val deviceService: DeviceService) : MnemosyneGrpc.MnemosyneImplBase() {
    override fun signUp(request: NewUserReq?, responseObserver: StreamObserver<UserResponse>?) {
        try {
            val workspace = getUUID(request!!.workspaceId)

            val response = userService.createUser(
                request.username,
                request.email,
                request.password,
                request.role,
                workspace
            )

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
            val response = userService.login(request!!.email, request.password)

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
            val response = userService.findUserById(UUID.fromString(request!!.id))

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
            val response = userService.findUsersByWorkspace(UUID.fromString(request!!.workspaceId))

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
            checkUserPermissions(UUID.fromString(request!!.currentUser))

            val workspace = getUUID(request.user.currentWorkspace)
            val grid = getUUID(request.user.currentGrid)

            val response = userService.update(
                UUID.fromString(request.user.id),
                request.user.email,
                request.user.username,
                request.user.picture,
                request.user.role,
                workspace,
                grid
            )

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
            userService.updatePwd(UUID.fromString(request!!.id), request.password)

            val response = buildStatusResponse()

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
            checkUserPermissions(UUID.fromString(request!!.currentUser))

            userService.delete(UUID.fromString(request.user.id))

            val response = buildStatusResponse()

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


    override fun createWorkspace(request: NewWorkspaceReq?, responseObserver: StreamObserver<WorkspaceResponse>?) {
        try {
            val response = workspaceService.createWorkspace(request!!.name)

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

    override fun getWorkspace(request: GetEntityReq?, responseObserver: StreamObserver<WorkspaceResponse>?) {
        try {
            val response = workspaceService.findWorkspaceById(UUID.fromString(request!!.id))

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

    override fun createGrid(request: NewGridReq?, responseObserver: StreamObserver<GridResponse>?) {
        try {
            val response = gridService.createGrid(request!!.name, UUID.fromString(request.workspaceId))

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

    override fun getGrid(request: GetEntityReq?, responseObserver: StreamObserver<GridResponse>?) {
        try {
            val response = gridService.findGridById(UUID.fromString(request!!.id))

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

    override fun getGridsByWorkspace(request: GetEntityByWorkspaceReq?, responseObserver: StreamObserver<GridsResponse>?) {
        try {
            val response = gridService.findGridsByWorkspace(UUID.fromString(request!!.workspaceId))

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

    override fun createNode(request: NewNodeReq?, responseObserver: StreamObserver<NodeResponse>?) {
        try {
            val response = nodeService.createNode(
                request!!.name,
                UUID.fromString(request.gridId),
                request.location,
                UUID.fromString(request.createdBy)
            )

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

    override fun getNode(request: GetEntityReq?, responseObserver: StreamObserver<NodeResponse>?) {
        try {
            val response = nodeService.findNodeById(UUID.fromString(request!!.id))

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

    override fun getNodesByGrid(request: GetEntityByGridReq?, responseObserver: StreamObserver<NodesResponse>?) {
        try {
            val response = nodeService.findNodesByGrid(UUID.fromString(request!!.gridId))

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

    override fun updateNode(request: UpdateNodeReq?, responseObserver: StreamObserver<NodeResponse>?) {
        try {
            val response = nodeService.update(
                UUID.fromString(request!!.nodeId),
                UUID.fromString(request.currentUser),
                request.fields.active
            )

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

    override fun deleteNode(request: UpdateNodeReq?, responseObserver: StreamObserver<StatusResponse>?) {
        try {
            checkUserPermissions(UUID.fromString(request!!.currentUser))

            nodeService.delete(UUID.fromString(request.nodeId))

            val response = buildStatusResponse()

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

    override fun createDevice(request: NewDeviceReq?, responseObserver: StreamObserver<DeviceResponse>?) {
        try {
            val response = deviceService.createDevice(
                request!!.apiKey,
                request.deviceType,
                UUID.fromString(request.createdBy)
            )

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

    override fun getDevice(request: GetEntityReq?, responseObserver: StreamObserver<DeviceResponse>?) {
        try {
            val response = deviceService.findDeviceById(UUID.fromString(request!!.id))

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } catch (e: Exception) {
            println(e)
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException()
            )
        }
    }

    override fun getDevicesByNode(request: GetEntityByNodeIdReq?, responseObserver: StreamObserver<DevicesResponse>?) {
        try {
            val response = deviceService.findDevicesByNode(UUID.fromString(request!!.nodeId))

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

    override fun updateDevice(request: UpdateDeviceReq?, responseObserver: StreamObserver<DeviceResponse>?) {
        try {
            checkUserPermissions(UUID.fromString(request!!.currentUser))

            val response = deviceService.update(
                request.apiKey,
                request.location,
                UUID.fromString(request.nodeId),
                UUID.fromString(request.currentUser)
            )

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

    override fun deleteDevice(request: DeleteDeviceReq?, responseObserver: StreamObserver<StatusResponse>?) {
        try {
            checkUserPermissions(UUID.fromString(request!!.currentUser))

            deviceService.delete(UUID.fromString(request.deviceId))

            val response = buildStatusResponse()

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

    private fun checkUserPermissions(id: UUID) {
        val currentUser = userService.findById(id) ?: throw Exception("Current user not found")
        if (currentUser.role != "admin") {
            throw Exception("Unauthorized")
        }
    }

    private fun getUUID(value: String?): UUID? {
        return try {
            UUID.fromString(value)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildStatusResponse(): StatusResponse {
        return StatusResponse.newBuilder().setSuccess(true).build()
    }
}
