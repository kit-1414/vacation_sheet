package com.example.vacationsheet.mainapp.hql.mapper

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.hql.dto.VacationRequestDto
import com.example.vacationsheet.mainapp.hql.dto.ManagerVacationRequestDto
import com.example.vacationsheet.mainapp.hql.dto.ProjectSummaryDto
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import org.springframework.stereotype.Component

@Component
class VacationRequestMapper(
	private val userAccountMapper: UserAccountMapper,
) {
	fun toEntity(dto: VacationRequestRequestDto, author: UserAccountEntity) = VacationRequestEntity(
		title = dto.title.trim(),
		requestState = dto.requestState,
		vacationType = dto.vacationType,
		startDate = dto.startDate,
		endDate = dto.endDate,
		userComments = dto.userComments?.ifEmpty { null },
		author = author,
	)

	fun updateEntity(dto: VacationRequestRequestDto, entity: VacationRequestEntity) {
		entity.title = dto.title.trim()
		entity.requestState = dto.requestState
		entity.vacationType = dto.vacationType
		entity.startDate = dto.startDate
		entity.endDate = dto.endDate
		entity.userComments = dto.userComments?.ifEmpty { null }
	}

	fun toDto(entity: VacationRequestEntity) = VacationRequestDto(
		id = requireNotNull(entity.id),
		title = entity.title,
		requestState = entity.requestState,
		vacationType = entity.vacationType,
		startDate = entity.startDate,
		endDate = entity.endDate,
		userComments = entity.userComments,
		managerComments = entity.managerComments,
		author = userAccountMapper.toDto(entity.author),
		manager = entity.manager?.let(userAccountMapper::toDto),
		ctime = entity.ctime,
		utime = entity.utime,
	)

	fun toManagerDto(entity: VacationRequestEntity, authorProjects: List<ProjectSummaryDto>) =
		ManagerVacationRequestDto(
			request = toDto(entity),
			authorProjects = authorProjects,
		)
}
