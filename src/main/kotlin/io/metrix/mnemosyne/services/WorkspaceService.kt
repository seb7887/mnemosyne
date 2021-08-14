package io.metrix.mnemosyne.services

import io.metrix.mnemosyne.entities.WorkspaceEntity
import io.metrix.mnemosyne.repositories.WorkspaceRepository
import mnemosyne.WorkspaceResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class WorkspaceService(private val workspaceRepository: WorkspaceRepository) {
    fun createWorkspace(name: String): WorkspaceResponse {
        val newWorkspace = WorkspaceEntity(
            name = name
        )

        val workspace = workspaceRepository.save(newWorkspace)

        return buildWorkspaceResponse(workspace)
    }

    fun findWorkspaceById(id: UUID): WorkspaceResponse {
        val workspace = workspaceRepository.findById(id) ?: throw Exception("Workspace not found")

        return buildWorkspaceResponse(workspace)
    }

    private fun buildWorkspaceResponse(workspace: WorkspaceEntity): WorkspaceResponse {
        return WorkspaceResponse.newBuilder()
            .setId(workspace.id.toString())
            .setName(workspace.name)
            .setCreatedAt(workspace.createdAt.toString())
            .setUpdatedAt(workspace.updatedAt.toString())
            .build()
    }
}