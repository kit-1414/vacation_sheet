package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class UserServiceTest {
	private val userAccountRepository = mockk<UserAccountRepository>()
	private val service = UserService(userAccountRepository, UserAccountMapper())

	@Test
	fun `current user is found by normalized email`() {
		val account = UserAccountEntity(
			email = "user@example.com",
			firstName = "Test",
			lastName = "User",
			id = UUID.randomUUID(),
		)
		every { userAccountRepository.findByEmail("user@example.com") } returns account

		val result = service.findCurrent("  User@Example.COM ")

		assertEquals(account.id, result.id)
		verify(exactly = 1) { userAccountRepository.findByEmail("user@example.com") }
	}
}
