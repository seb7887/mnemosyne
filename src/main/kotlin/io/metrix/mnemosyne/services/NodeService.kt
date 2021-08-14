package io.metrix.mnemosyne.services

import io.metrix.mnemosyne.entities.NodeEntity
import io.metrix.mnemosyne.repositories.NodeRepository
import mnemosyne.Location
import mnemosyne.NodeResponse
import mnemosyne.NodesResponse
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class NodeService(private val nodeRepository: NodeRepository) {
    private val targetNotFound = "Target node not found"

    fun createNode(name: String, gridId: UUID, location: Location, createdBy: UUID): NodeResponse {
        nodeRepository.create(
            name,
            gridId,
            location.latitude,
            location.longitude,
            createdBy,
            createdBy
        )

        val newNode = nodeRepository.findByName(name) ?: throw Exception("Error getting node data")

        return buildNodeResponse(newNode)
    }

    fun findNodeById(id: UUID): NodeResponse {
        val node = findById(id) ?: throw Exception("Node not found")

        return buildNodeResponse(node)
    }

    fun findNodesByGrid(gridId: UUID): NodesResponse {
        val nodes = nodeRepository.findAllByGridId(gridId).map {
            buildNodeResponse(it)
        }

        return buildNodesResponse(nodes)
    }

    fun update(id: UUID, updatedBy: UUID, active: Boolean): NodeResponse {
        val node = findById(id) ?: throw Exception(targetNotFound)

        node.active = active
        node.updatedBy = updatedBy
        node.updatedAt = OffsetDateTime.now()

        nodeRepository.save(node)

        return buildNodeResponse(node)
    }

    fun delete(id: UUID) {
        val node = findById(id) ?: throw Exception(targetNotFound)
        nodeRepository.delete(node)
    }

    private fun findById(id: UUID): NodeEntity? {
        return nodeRepository.findById(id)
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
}