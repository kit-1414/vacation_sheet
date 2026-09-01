package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import com.example.vacationsheet.mainapp.dto.VacationRequestManagerActionDto
import com.example.vacationsheet.mainapp.exception.InvalidVacationRequestException
import com.example.vacationsheet.mainapp.exception.ResourceNotFoundException
import com.example.vacationsheet.mainapp.exception.VacationRequestAccessDeniedException
import com.example.vacationsheet.mainapp.exception.VacationRequestModificationNotAllowedException
import com.example.vacationsheet.mainapp.hql.dto.VacationRequestDto
import com.example.vacationsheet.mainapp.hql.dto.ManagerVacationRequestDto
import com.example.vacationsheet.mainapp.hql.mapper.ProjectMapper
import com.example.vacationsheet.mainapp.hql.mapper.VacationRequestMapper
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import com.example.vacationsheet.mainapp.hql.repository.VacationRequestRepository
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VacationRequestService(
	private val vacationRequestRepository: VacationRequestRepository,
	private val userAccountRepository: UserAccountRepository,
	private val vacationRequestMapper: VacationRequestMapper,
	private val projectRepository: ProjectRepository,
	private val projectMapper: ProjectMapper,
) {
	@Transactional(readOnly = true)
	fun getRequestsByOwnerId(id: Long): List<VacationRequestDto> =
		vacationRequestRepository.findAllByOwnerId(id).map(vacationRequestMapper::toDto)

	@Transactional(readOnly = true)
	fun getRequestsByOwnerId(currentUser: CurrentUserDto): List<VacationRequestDto> =
		getRequestsByOwnerId(currentUser.id)

	@Transactional(readOnly = true)
	fun findById(id: Long, currentUser: CurrentUserDto): VacationRequestDto {
		failIfNotCreator(id, currentUser)
		return vacationRequestMapper.toDto(getById(id))
	}

	@Transactional
	fun create(request: VacationRequestRequestDto, currentUser: CurrentUserDto): VacationRequestDto {
		validateUserRequest(request)
		val author = userAccountRepository.findById(currentUser.id).orElseThrow {
			ResourceNotFoundException("User ${currentUser.id} was not found")
		}
		return vacationRequestRepository.save(vacationRequestMapper.toEntity(request, author))
			.let(vacationRequestMapper::toDto)
	}

	@Transactional
	fun update(id: Long, currentUser: CurrentUserDto, request: VacationRequestRequestDto): VacationRequestDto {
		failIfNotCreator(id, currentUser)
		val entity = getById(id)
		failIfModificationNotAllowed(entity)
		validateUserRequest(request)
		vacationRequestMapper.updateEntity(request, entity)
		return vacationRequestMapper.toDto(vacationRequestRepository.saveAndFlush(entity))
	}

	@Transactional
	fun delete(id: Long, currentUser: CurrentUserDto) {
		failIfNotCreator(id, currentUser)
		val entity = getById(id)
		failIfModificationNotAllowed(entity)
		vacationRequestRepository.delete(entity)
	}

	@Transactional(readOnly = true)
	fun getRequestsForManager(): List<ManagerVacationRequestDto> =
		toManagerDtos(vacationRequestRepository.findAllByStatesWithUsers(managerStates))

	@Transactional(readOnly = true)
	fun findByIdForManager(id: Long): ManagerVacationRequestDto {
		val entity = getById(id)
		if (entity.requestState !in managerStates) {
			throw ResourceNotFoundException("Vacation request $id was not found")
		}
		return toManagerDtos(listOf(entity)).single()
	}

	@Transactional
	fun review(
		id: Long,
		currentUser: CurrentUserDto,
		action: VacationRequestManagerActionDto,
	): ManagerVacationRequestDto {
		if (action.requestState !in managerStates) {
			throw InvalidVacationRequestException("Managers can only set READY, APPROVED or REJECTED state")
		}
		val entity = vacationRequestRepository.findByIdWithUsersForUpdate(id)
			?: throw ResourceNotFoundException("Vacation request $id was not found")
		if (entity.requestState !in managerStates) {
			throw VacationRequestModificationNotAllowedException(
				"Vacation request $id cannot be reviewed in state ${entity.requestState}",
			)
		}

		entity.requestState = action.requestState
		if (action.requestState == VacationRequestState.READY) {
			entity.manager = null
			entity.managerComments = null
		} else {
			entity.manager = userAccountRepository.findById(currentUser.id).orElseThrow {
				ResourceNotFoundException("User ${currentUser.id} was not found")
			}
			if (action.updateManagerComment) {
				entity.managerComments = action.managerComment?.ifEmpty { null }
			}
		}

		return vacationRequestRepository.saveAndFlush(entity)
			.let { toManagerDtos(listOf(it)).single() }
	}

	@Transactional(readOnly = true)
	fun checkIsCreator(requestVacationId: Long, currentUser: CurrentUserDto): Boolean =
		vacationRequestRepository.existsByIdAndAuthorId(requestVacationId, currentUser.id)

	@Transactional(readOnly = true)
	fun failIfNotCreator(requestVacationId: Long, currentUser: CurrentUserDto) {
		if (!checkIsCreator(requestVacationId, currentUser)) {
			if (!vacationRequestRepository.existsById(requestVacationId)) {
				throw ResourceNotFoundException("Vacation request $requestVacationId was not found")
			}
			logger.debug(
				"User {} attempted to access vacation request {} owned by another user",
				currentUser.id,
				requestVacationId,
			)
			throw VacationRequestAccessDeniedException("Vacation request access denied")
		}
	}

	private fun getById(id: Long): VacationRequestEntity = vacationRequestRepository.findByIdWithUsers(id)
		?: throw ResourceNotFoundException("Vacation request $id was not found")

	private fun failIfModificationNotAllowed(entity: VacationRequestEntity) {
		if (entity.requestState !in userStates) {
			throw VacationRequestModificationNotAllowedException(
				"Vacation request ${entity.id} cannot be modified in state ${entity.requestState}",
			)
		}
	}

	private fun validateUserRequest(request: VacationRequestRequestDto) {
		if (request.requestState !in userStates) {
			throw InvalidVacationRequestException("Users can only set DRAFT or READY state")
		}
		if (request.endDate.isBefore(request.startDate)) {
			throw InvalidVacationRequestException("End date must not be before start date")
		}
	}

	private fun toManagerDtos(entities: List<VacationRequestEntity>): List<ManagerVacationRequestDto> {
		if (entities.isEmpty()) return emptyList()
		val authorIds = entities.mapTo(linkedSetOf()) { requireNotNull(it.author.id) }
		val projects = projectRepository.findAllWithMembersByMemberIds(authorIds)
		return entities.map { entity ->
			val authorId = requireNotNull(entity.author.id)
			val authorProjects = projects
				.filter { project -> project.members.any { it.id == authorId } }
				.map(projectMapper::toSummaryDto)
			vacationRequestMapper.toManagerDto(entity, authorProjects)
		}
	}

	private companion object {
		val logger = LoggerFactory.getLogger(VacationRequestService::class.java)
		val userStates = setOf(VacationRequestState.DRAFT, VacationRequestState.READY)
		val managerStates = setOf(
			VacationRequestState.READY,
			VacationRequestState.APPROVED,
			VacationRequestState.REJECTED,
		)
	}
}
