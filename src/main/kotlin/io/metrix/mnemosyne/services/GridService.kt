package io.metrix.mnemosyne.services

import io.metrix.mnemosyne.entities.GridEntity
import io.metrix.mnemosyne.repositories.GridRepository
import mnemosyne.GridResponse
import mnemosyne.GridsResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class GridService(private val gridRepository: GridRepository) {
    fun createGrid(name: String, workspace: UUID): GridResponse {
        val newGrid = GridEntity(
            name = name,
            workspaceId = workspace
        )

        gridRepository.save(newGrid)

        return buildGridResponse(newGrid)
    }

    fun findGridById(id: UUID): GridResponse {
        val grid = gridRepository.findById(id) ?: throw Exception("Grid not found")

        return buildGridResponse(grid)
    }

    fun findGridsByWorkspace(workspace: UUID): GridsResponse {
        val grids = gridRepository.findAllByWorkspaceId(workspace).map {
            buildGridResponse(it)
        }

        return buildGridsResponse(grids)
    }

    private fun buildGridResponse(grid: GridEntity): GridResponse {
        return GridResponse.newBuilder()
            .setId(grid.id.toString())
            .setName(grid.name)
            .setWorkspaceId(grid.workspaceId.toString())
            .setCreatedAt(grid.createdAt.toString())
            .setUpdatedAt(grid.updatedAt.toString())
            .build()
    }

    private  fun buildGridsResponse(grids: List<GridResponse>): GridsResponse {
        return GridsResponse.newBuilder()
            .addAllGrids(grids)
            .setTotal(grids.count().toLong())
            .build()
    }
}