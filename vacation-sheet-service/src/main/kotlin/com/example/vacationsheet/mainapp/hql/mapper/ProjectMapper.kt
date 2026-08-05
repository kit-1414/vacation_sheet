package com.example.vacationsheet.mainapp.hql.mapper

import com.example.vacationsheet.mainapp.dto.ProjectRequestDto
import com.example.vacationsheet.mainapp.hql.dto.ProjectDto
import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import org.springframework.stereotype.Component

@Component
class ProjectMapper(
	private val userAccountMapper: UserAccountMapper,
) {
	fun toEntity(dto: ProjectRequestDto) = ProjectEntity(
		name = dto.name.trim(),
		description = dto.description?.trim()?.ifEmpty { null },
	)

	fun updateEntity(dto: ProjectRequestDto, entity: ProjectEntity) {
		entity.name = dto.name.trim()
		entity.description = dto.description?.trim()?.ifEmpty { null }
	}

	fun toDto(entity: ProjectEntity) = ProjectDto(
		id = requireNotNull(entity.id),
		name = entity.name,
		description = entity.description,
		members = entity.members.map(userAccountMapper::toDto).sortedBy { it.displayName ?: it.email },
		ctime = entity.ctime,
		utime = entity.utime,
	)
}
