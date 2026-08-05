package com.example.vacationsheet.service

import com.example.vacationsheet.dto.UserResponse
import com.example.vacationsheet.dto.toResponse
import com.example.vacationsheet.exception.ResourceNotFoundException
import com.example.vacationsheet.repository.UserAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
	private val userAccountRepository: UserAccountRepository,
) {
	@Transactional(readOnly = true)
	fun findAll(): List<UserResponse> = userAccountRepository.findAllByOrderByDisplayNameAscEmailAsc().map { it.toResponse() }

	@Transactional(readOnly = true)
	fun findCurrent(yandexId: String): UserResponse = userAccountRepository.findByYandexId(yandexId)?.toResponse()
		?: throw ResourceNotFoundException("Authenticated user was not found")

	@Transactional(readOnly = true)
	fun getEntity(id: UUID) = userAccountRepository.findById(id).orElseThrow {
		ResourceNotFoundException("User $id was not found")
	}
}
