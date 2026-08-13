package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.UserAccountRequestDto
import com.example.vacationsheet.mainapp.exception.ResourceNotFoundException
import com.example.vacationsheet.mainapp.exception.UserEmailAlreadyExistsException
import com.example.vacationsheet.mainapp.hql.dto.UserAccountDto
import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
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
		userAccountRepository.findAllByOrderByLastNameAscFirstNameAscEmailAsc().map(userAccountMapper::toDto)

	@Transactional(readOnly = true)
	fun findById(id: Long): UserAccountDto = userAccountMapper.toDto(getById(id))

	@Transactional
	fun create(request: UserAccountRequestDto): UserAccountDto {
		val email = normalizeEmail(request.email)
		ensureEmailAvailable(email)
		return userAccountRepository.save(
			UserAccountEntity(
				email = email,
				firstName = normalizeName(request.firstName),
				lastName = normalizeName(request.lastName),
				isAdmin = request.isAdmin,
				isActive = request.isActive,
			),
		).let(userAccountMapper::toDto)
	}

	@Transactional
	fun update(id: Long, request: UserAccountRequestDto): UserAccountDto {
		val user = getById(id)
		val email = normalizeEmail(request.email)
		ensureEmailAvailable(email, id)
		user.email = email
		user.firstName = normalizeName(request.firstName)
		user.lastName = normalizeName(request.lastName)
		user.isAdmin = request.isAdmin
		user.isActive = request.isActive
		return userAccountMapper.toDto(user)
	}

	@Transactional
	fun delete(id: Long) {
		userAccountRepository.delete(getById(id))
	}

	@Transactional(readOnly = true)
	fun findCurrent(email: String): UserAccountDto = userAccountRepository.findByEmail(email.trim().lowercase())?.let(userAccountMapper::toDto)
		?: throw ResourceNotFoundException("Authenticated user was not found")

	private fun getById(id: Long): UserAccountEntity = userAccountRepository.findById(id).orElseThrow {
		ResourceNotFoundException("User $id was not found")
	}

	private fun ensureEmailAvailable(email: String, currentId: Long? = null) {
		val existing = userAccountRepository.findByEmail(email)
		if (existing != null && existing.id != currentId) {
			throw UserEmailAlreadyExistsException(email)
		}
	}

	private fun normalizeEmail(email: String) = email.trim().lowercase()

	private fun normalizeName(name: String?) = name?.trim()?.takeIf(String::isNotEmpty)
}
