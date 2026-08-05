package com.example.vacationsheet.service

import com.example.vacationsheet.dto.ProjectRequest
import com.example.vacationsheet.dto.ProjectResponse
import com.example.vacationsheet.dto.toResponse
import com.example.vacationsheet.entity.Project
import com.example.vacationsheet.exception.ResourceNotFoundException
import com.example.vacationsheet.repository.ProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ProjectService(
	private val projectRepository: ProjectRepository,
	private val userService: UserService,
) {
	@Transactional(readOnly = true)
	fun findAll(): List<ProjectResponse> = projectRepository.findAllWithMembers().map { it.toResponse() }

	@Transactional
	fun create(request: ProjectRequest): ProjectResponse {
		val project = Project(
			name = request.name.trim(),
			description = request.description?.trim()?.ifEmpty { null },
		)
		return projectRepository.save(project).toResponse()
	}

	@Transactional
	fun update(id: UUID, request: ProjectRequest): ProjectResponse {
		val project = getProject(id)
		project.name = request.name.trim()
		project.description = request.description?.trim()?.ifEmpty { null }
		project.updatedAt = Instant.now()
		return project.toResponse()
	}

	@Transactional
	fun delete(id: UUID) {
		projectRepository.delete(getProject(id))
	}

	@Transactional
	fun addMember(projectId: UUID, userId: UUID): ProjectResponse {
		val project = getProject(projectId)
		if (project.members.add(userService.getEntity(userId))) {
			project.updatedAt = Instant.now()
		}
		return project.toResponse()
	}

	@Transactional
	fun removeMember(projectId: UUID, userId: UUID): ProjectResponse {
		val project = getProject(projectId)
		val removed = project.members.removeIf { it.id == userId }
		if (removed) project.updatedAt = Instant.now()
		return project.toResponse()
	}

	private fun getProject(id: UUID): Project = projectRepository.findByIdWithMembers(id)
		?: throw ResourceNotFoundException("Project $id was not found")
}
