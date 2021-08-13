package io.metrix.mnemosyne.services

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.metrix.mnemosyne.entities.*
import io.metrix.mnemosyne.interceptor.MetadataInterceptor
import io.metrix.mnemosyne.repositories.*
import mnemosyne.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.lognet.springboot.grpc.GRpcService
import org.mindrot.jbcrypt.BCrypt
import java.time.OffsetDateTime
import java.util.*

@GRpcService(interceptors = [MetadataInterceptor::class])
class MnemosyneService(
    private val userRepository: UserRepository,
    private val workspacesRepository: WorkspaceRepository,
    private val gridRepository: GridRepository,
    private val nodeRepository: NodeRepository,
    private val deviceRepository: DeviceRepository) : MnemosyneGrpc.MnemosyneImplBase() {
    override fun signUp(request: NewUserReq?, responseObserver: StreamObserver<UserResponse>?) {
        try {
            val newUser = UserEntity(
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
            val user = userRepository.save(newUser)

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
            val user = userRepository.findByEmail(request!!.email) ?: throw Exception("User not found")
            if (!BCrypt.checkpw(request.password, user.hash)) {
                throw Exception("Invalid password")
            }

            // Update lastLogin
            user.lastLogin = OffsetDateTime.now()
            userRepository.save(user)

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
            val user = userRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("User not found")

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
            val users = userRepository.findAllByCurrentWorkspace(UUID.fromString(request!!.workspaceId))
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
            val currentUser = userRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Invalid current user")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            var user = userRepository.findById(UUID.fromString(request.user.id)) ?: throw Exception("Unexistent target user")
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

            userRepository.save(user)

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
            val user = userRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("Cannot find user")
            user.hash = BCrypt.hashpw(request.password, BCrypt.gensalt())

            userRepository.save(user)

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
            val currentUser = userRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Invalid current user")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            val user = userRepository.findById(UUID.fromString(request.user.id)) ?: throw Exception("Unexistent target user")
            userRepository.delete(user)

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

    fun buildUserResponse(user: UserEntity): UserResponse {
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

    private fun buildUsersResponse(users: List<UserResponse>): UsersResponse {
        return UsersResponse.newBuilder()
            .addAllUsers(users)
            .setTotal(users.count().toLong())
            .build()
    }

    private fun buildStatusResponse(status: Boolean): StatusResponse {
        return StatusResponse.newBuilder().setSuccess(status).build()
    }

    override fun createWorkspace(request: NewWorkspaceReq?, responseObserver: StreamObserver<WorkspaceResponse>?) {
        try {
            val newWorkspace = WorkspaceEntity(
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

    override fun getWorkspace(request: GetEntityReq?, responseObserver: StreamObserver<WorkspaceResponse>?) {
        try {
            val workspace = workspacesRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("Workspace not found")
            val response = buildWorkspaceResponse(workspace)

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

    private fun buildWorkspaceResponse(workspace: WorkspaceEntity): WorkspaceResponse {
        return WorkspaceResponse.newBuilder()
            .setId(workspace.id.toString())
            .setName(workspace.name)
            .setCreatedAt(workspace.createdAt.toString())
            .setUpdatedAt(workspace.updatedAt.toString())
            .build()
    }

    override fun createGrid(request: NewGridReq?, responseObserver: StreamObserver<GridResponse>?) {
        try {
            val newGrid = GridEntity(
                name = request!!.name,
                workspaceId = UUID.fromString(request!!.workspaceId)
            )

            gridRepository.save(newGrid)

            val response = buildGridResponse(newGrid)

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
            val grid = gridRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("Grid not found")

            val response = buildGridResponse(grid)

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
            val grids = gridRepository.findAllByWorkspaceId(UUID.fromString(request!!.workspaceId)).map {
                buildGridResponse(it)
            }

            val response = buildGridsResponse(grids)


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

    fun buildGridResponse(grid: GridEntity): GridResponse {
        return GridResponse.newBuilder()
            .setId(grid.id.toString())
            .setName(grid.name)
            .setWorkspaceId(grid.workspaceId.toString())
            .setCreatedAt(grid.createdAt.toString())
            .setUpdatedAt(grid.updatedAt.toString())
            .build()
    }

    fun buildGridsResponse(grids: List<GridResponse>): GridsResponse {
        return GridsResponse.newBuilder()
            .addAllGrids(grids)
            .setTotal(grids.count().toLong())
            .build()
    }

    override fun createNode(request: NewNodeReq?, responseObserver: StreamObserver<NodeResponse>?) {
        try {
            nodeRepository.create(
                request!!.name,
                UUID.fromString(request!!.gridId),
                request.location.latitude,
                request.location.longitude,
                UUID.fromString(request.createdBy),
                UUID.fromString(request.createdBy)
            )

            val newNode = nodeRepository.findByName(request.name) ?: throw Exception("Error getting node data")

            val response = buildNodeResponse(newNode)

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
            val node = nodeRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("Node not found")

            val response = buildNodeResponse(node)

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
            val nodes = nodeRepository.findAllByGridId(UUID.fromString(request!!.gridId)).map {
                buildNodeResponse(it)
            }

            val response = buildNodesResponse(nodes)

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
            val node = nodeRepository.findById(UUID.fromString(request!!.nodeId)) ?: throw Exception("Node not found")

            node.active = request.fields.active
            node.updatedBy = UUID.fromString(request.currentUser)
            node.updatedAt = OffsetDateTime.now()

            nodeRepository.save(node)

            val response = buildNodeResponse(node)

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
            val currentUser = userRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Current User not found")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            val node = nodeRepository.findById(UUID.fromString(request!!.nodeId)) ?: throw Exception("Target node not found")
            nodeRepository.delete(node)

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

    private fun buildNodeResponse(node: NodeEntity): NodeResponse {
        val location = Location.newBuilder()
            .setLatitude(node.location.coordinate.y)
            .setLongitude(node.location.coordinate.x)
            .build()

        return NodeResponse.newBuilder()
            .setId(node.id.toString())
            .setName(node.name)
            .setGridId(node.gridId.toString())
            .setActive(node.active)
            .setLocation(location)
            .setCreatedBy(node.createdBy.toString())
            .setCreatedAt(node.createdAt.toString())
            .setUpdatedBy(node.updatedBy.toString())
            .setUpdatedAt(node.updatedAt.toString())
            .build()
    }

    private fun buildNodesResponse(nodes: List<NodeResponse>): NodesResponse {
        return NodesResponse.newBuilder()
            .addAllNodes(nodes)
            .setTotal(nodes.count().toLong())
            .build()
    }

    override fun createDevice(request: NewDeviceReq?, responseObserver: StreamObserver<DeviceResponse>?) {
        try {
            val newDevice = DeviceEntity(
                apiKey = request!!.apiKey,
                type = request!!.deviceType,
                createdBy = UUID.fromString(request.createdBy),
                updatedBy = UUID.fromString(request.createdBy)
            )

            deviceRepository.save(newDevice)

            val response = buildDeviceResponse(newDevice)

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
            val device = deviceRepository.findById(UUID.fromString(request!!.id)) ?: throw Exception("Device not found")
            println(device)
            val response = buildDeviceResponse(device)

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
            val devices = deviceRepository.findAllByNodeId(UUID.fromString(request!!.nodeId)).map {
                buildDeviceResponse(it)
            }

            val response = buildDevicesResponse(devices)

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
            val currentUser = userRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Current user not found")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            val device = deviceRepository.findByApiKey(request.apiKey) ?: throw Exception("Device not found")
            val geometryFactory = GeometryFactory()
            val coordinate = Coordinate(request.location.longitude, request.location.latitude)
            val point = geometryFactory.createPoint(coordinate)
            point.srid = 4326

            device.location = point
            device.nodeId = UUID.fromString(request!!.nodeId)
            device.updatedBy = UUID.fromString(request!!.currentUser)
            device.updatedAt = OffsetDateTime.now()

            deviceRepository.save(device)

            val response = buildDeviceResponse(device)

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

    private fun buildDeviceResponse(device: DeviceEntity): DeviceResponse {
        val location = if (device.location != null) {
            Location.newBuilder()
                .setLatitude(device.location!!.coordinate.y)
                .setLongitude(device.location!!.coordinate.x)
                .build()
        } else {
            Location.newBuilder().build()
        }

        return DeviceResponse.newBuilder()
            .setId(device.id.toString())
            .setDeviceType(device.type)
            .setNodeId(device.nodeId.toString())
            .setLocation(location)
            .setCreatedBy(device.createdBy.toString())
            .setCreatedAt(device.createdAt.toString())
            .setUpdatedBy(device.updatedBy.toString())
            .setUpdatedAt(device.updatedAt.toString())
            .build()
    }

    override fun deleteDevice(request: DeleteDeviceReq?, responseObserver: StreamObserver<StatusResponse>?) {
        try {
            val currentUser = userRepository.findById(UUID.fromString(request!!.currentUser)) ?: throw Exception("Current user not found")
            if (currentUser.role != "admin") {
                throw Exception("Unauthorized")
            }

            val device = deviceRepository.findById(UUID.fromString(request!!.deviceId)) ?: throw Exception("Device not found")
            deviceRepository.delete(device)

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

    private fun buildDevicesResponse(devices: List<DeviceResponse>): DevicesResponse {
        return DevicesResponse.newBuilder()
            .addAllDevices(devices)
            .setTotal(devices.count().toLong())
            .build()
    }

    private fun getUUID(value: String?): UUID? {
        return try {
            UUID.fromString(value)
        } catch (e: Exception) {
            null
        }
    }
}
