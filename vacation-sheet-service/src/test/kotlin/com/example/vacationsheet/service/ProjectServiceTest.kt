package com.example.vacationsheet.service

import com.example.vacationsheet.dto.ProjectRequest
import com.example.vacationsheet.entity.Project
import com.example.vacationsheet.entity.UserAccount
import com.example.vacationsheet.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectServiceTest {
	private val projectRepository = mockk<ProjectRepository>()
	private val userService = mockk<UserService>()
	private val service = ProjectService(projectRepository, userService)

	@Test
	fun `create trims values and saves project`() {
		val projectId = UUID.randomUUID()
		every { projectRepository.save(any()) } answers {
			val project = firstArg<Project>()
			Project(name = project.name, description = project.description, id = projectId)
		}

		val response = service.create(ProjectRequest("  Project  ", "  Description  "))

		assertEquals(projectId, response.id)
		assertEquals("Project", response.name)
		assertEquals("Description", response.description)
		verify(exactly = 1) { projectRepository.save(any()) }
	}

	@Test
	fun `add member attaches user once`() {
		val projectId = UUID.randomUUID()
		val userId = UUID.randomUUID()
		val project = Project(name = "Project", description = null, id = projectId)
		val user = UserAccount(
			yandexId = "yandex-id",
			email = "user@example.com",
			displayName = "User",
			id = userId,
		)
		every { projectRepository.findByIdWithMembers(projectId) } returns project
		every { userService.getEntity(userId) } returns user

		val firstResponse = service.addMember(projectId, userId)
		val secondResponse = service.addMember(projectId, userId)

		assertEquals(1, firstResponse.members.size)
		assertEquals(1, secondResponse.members.size)
	}
}
