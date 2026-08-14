package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import com.example.vacationsheet.mainapp.exception.InvalidVacationRequestException
import com.example.vacationsheet.mainapp.exception.ResourceNotFoundException
import com.example.vacationsheet.mainapp.exception.VacationRequestAccessDeniedException
import com.example.vacationsheet.mainapp.exception.VacationRequestModificationNotAllowedException
import com.example.vacationsheet.mainapp.hql.dto.VacationRequestDto
import com.example.vacationsheet.mainapp.hql.mapper.VacationRequestMapper
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import com.example.vacationsheet.mainapp.hql.repository.VacationRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VacationRequestService(
	private val vacationRequestRepository: VacationRequestRepository,
	private val userAccountRepository: UserAccountRepository,
	private val vacationRequestMapper: VacationRequestMapper,
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

	private companion object {
		val logger = LoggerFactory.getLogger(VacationRequestService::class.java)
		val userStates = setOf(VacationRequestState.DRAFT, VacationRequestState.READY)
	}
}
