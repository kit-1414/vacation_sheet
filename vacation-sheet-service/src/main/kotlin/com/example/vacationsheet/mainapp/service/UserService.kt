package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.exception.ResourceNotFoundException
import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
	private val userAccountRepository: UserAccountRepository,
	private val userAccountMapper: UserAccountMapper,
) {
	@Transactional(readOnly = true)
	fun findAll(): List<UserAccountDto> =
		userAccountRepository.findAllByOrderByDisplayNameAscEmailAsc().map(userAccountMapper::toDto)

	@Transactional(readOnly = true)
	fun findCurrent(yandexId: String): UserAccountDto = userAccountRepository.findByYandexId(yandexId)?.let(userAccountMapper::toDto)
		?: throw ResourceNotFoundException("Authenticated user was not found")
}
