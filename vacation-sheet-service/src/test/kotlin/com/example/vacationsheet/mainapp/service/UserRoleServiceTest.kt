package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoleServiceTest {
	private val projectRepository = mockk<ProjectRepository>()
	private val service = UserRoleService(projectRepository)

	@Test
	fun `inactive user has only nobody role`() {
		val user = UserAccountEntity("user@example.com", null, null, isAdmin = true, isActive = false, id = 1L)
		assertEquals(setOf(UserRole.NOBODY), service.getRoles(user))
	}

	@Test
	fun `active admin without managed projects has admin and user roles`() {
		val user = UserAccountEntity("user@example.com", null, null, isAdmin = true, id = 1L)
		every { projectRepository.existsByManagersId(1L) } returns false
		assertEquals(setOf(UserRole.ADMIN, UserRole.USER), service.getRoles(user))
	}

	@Test
	fun `active manager has manager role instead of user role`() {
		val user = UserAccountEntity("user@example.com", null, null, id = 1L)
		every { projectRepository.existsByManagersId(1L) } returns true
		assertEquals(setOf(UserRole.MANAGER), service.getRoles(user))
	}
}
