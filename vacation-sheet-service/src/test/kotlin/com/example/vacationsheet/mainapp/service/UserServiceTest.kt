package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.UserAccountRequestDto
import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserServiceTest {
	private val userAccountRepository = mockk<UserAccountRepository>()
	private val service = UserService(userAccountRepository, UserAccountMapper())

	@Test
	fun `current user is found by normalized email`() {
		val account = UserAccountEntity(
			email = "user@example.com",
			firstName = "Test",
			lastName = "User",
			id = 1L,
		)
		every { userAccountRepository.findByEmail("user@example.com") } returns account

		val result = service.findCurrent("  User@Example.COM ")

		assertEquals(account.id, result.id)
		verify(exactly = 1) { userAccountRepository.findByEmail("user@example.com") }
	}

	@Test
	fun `create normalizes email and names`() {
		every { userAccountRepository.findByEmail("user@example.com") } returns null
		every { userAccountRepository.save(any()) } answers {
			val user = firstArg<UserAccountEntity>()
			UserAccountEntity(
				user.email,
				user.firstName,
				user.lastName,
				isAdmin = user.isAdmin,
				isActive = user.isActive,
				id = 1L,
			)
		}

		val result = service.create(
			UserAccountRequestDto(" User@Example.COM ", " Test ", " User ", isAdmin = true, isActive = false),
		)

		assertEquals("user@example.com", result.email)
		assertEquals("Test", result.firstName)
		assertEquals("User", result.lastName)
		assertTrue(result.isAdmin)
		assertFalse(result.isActive)
	}

	@Test
	fun `update changes account flags`() {
		val user = UserAccountEntity("user@example.com", "Test", "User", id = 1L)
		every { userAccountRepository.findById(1L) } returns Optional.of(user)
		every { userAccountRepository.findByEmail("user@example.com") } returns user

		val result = service.update(
			1L,
			UserAccountRequestDto("user@example.com", "Test", "User", isAdmin = true, isActive = false),
		)

		assertTrue(result.isAdmin)
		assertFalse(result.isActive)
	}

	@Test
	fun `delete removes only the user entity`() {
		val user = UserAccountEntity("user@example.com", "Test", "User", id = 1L)
		every { userAccountRepository.findById(1L) } returns Optional.of(user)
		every { userAccountRepository.delete(user) } returns Unit

		service.delete(1L)

		verify(exactly = 1) { userAccountRepository.delete(user) }
	}
}
