package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.ProjectRequestDto
import com.example.vacationsheet.mainapp.exception.ProjectNameAlreadyExistsException
import com.example.vacationsheet.mainapp.exception.ResourceNotFoundException
import com.example.vacationsheet.mainapp.hql.dto.ProjectDto
import com.example.vacationsheet.mainapp.hql.mapper.ProjectMapper
import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProjectService(
	private val projectRepository: ProjectRepository,
	private val userAccountRepository: UserAccountRepository,
	private val projectMapper: ProjectMapper,
) {
	@Transactional(readOnly = true)
	fun findAll(): List<ProjectDto> = projectRepository.findAllWithMembers().map(projectMapper::toDto)

	@Transactional(readOnly = true)
	fun findById(id: UUID): ProjectDto = projectMapper.toDto(getProject(id))

	@Transactional
	fun create(request: ProjectRequestDto): ProjectDto {
		ensureNameAvailable(request.name.trim())
		val project = projectRepository.save(projectMapper.toEntity(request))
		return projectMapper.toDto(project)
	}

	@Transactional
	fun update(id: UUID, request: ProjectRequestDto): ProjectDto {
		val project = getProject(id)
		ensureNameAvailable(request.name.trim(), id)
		projectMapper.updateEntity(request, project)
		return projectMapper.toDto(project)
	}

	@Transactional
	fun delete(id: UUID) {
		projectRepository.delete(getProject(id))
	}

	@Transactional
	fun addMember(projectId: UUID, userId: UUID): ProjectDto {
		val project = getProject(projectId)
		val user = userAccountRepository.findById(userId).orElseThrow {
			ResourceNotFoundException("User $userId was not found")
		}
		project.members.add(user)
		return projectMapper.toDto(project)
	}

	@Transactional
	fun removeMember(projectId: UUID, userId: UUID): ProjectDto {
		val project = getProject(projectId)
		project.members.removeIf { it.id == userId }
		return projectMapper.toDto(project)
	}

	private fun getProject(id: UUID): ProjectEntity = projectRepository.findByIdWithMembers(id)
		?: throw ResourceNotFoundException("Project $id was not found")

	private fun ensureNameAvailable(name: String, currentId: UUID? = null) {
		val existing = projectRepository.findByNameIgnoreCase(name)
		if (existing != null && existing.id != currentId) {
			throw ProjectNameAlreadyExistsException(name)
		}
	}
}
