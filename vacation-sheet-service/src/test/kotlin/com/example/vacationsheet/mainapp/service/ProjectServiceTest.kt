package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.ProjectRequestDto
import com.example.vacationsheet.mainapp.exception.ProjectNameAlreadyExistsException
import com.example.vacationsheet.mainapp.hql.mapper.ProjectMapper
import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectServiceTest {
	private val projectRepository = mockk<ProjectRepository>()
	private val userAccountRepository = mockk<UserAccountRepository>()
	private val mapper = ProjectMapper(UserAccountMapper())
	private val service = ProjectService(projectRepository, userAccountRepository, mapper)

	@Test
	fun `create trims values and saves project`() {
		val projectId = UUID.randomUUID()
		every { projectRepository.findByNameIgnoreCase("Project") } returns null
		every { projectRepository.save(any()) } answers {
			val project = firstArg<ProjectEntity>()
			ProjectEntity(name = project.name, description = project.description, id = projectId)
		}

		val response = service.create(ProjectRequestDto("  Project  ", "  Description  "))

		assertEquals(projectId, response.id)
		assertEquals("Project", response.name)
		assertEquals("  Description  ", response.description)
		verify(exactly = 1) { projectRepository.save(any()) }
	}

	@Test
	fun `create rejects duplicate project name ignoring case`() {
		val existing = ProjectEntity(name = "Project", description = null, id = UUID.randomUUID())
		every { projectRepository.findByNameIgnoreCase("project") } returns existing

		assertFailsWith<ProjectNameAlreadyExistsException> {
			service.create(ProjectRequestDto(" project ", null))
		}
		verify(exactly = 0) { projectRepository.save(any()) }
	}

	@Test
	fun `add member attaches user once`() {
		val projectId = UUID.randomUUID()
		val userId = UUID.randomUUID()
		val project = ProjectEntity(name = "Project", description = null, id = projectId)
		val user = UserAccountEntity(
			email = "user@example.com",
			firstName = "Test",
			lastName = "User",
			id = userId,
		)
		every { projectRepository.findByIdWithMembers(projectId) } returns project
		every { userAccountRepository.findById(userId) } returns Optional.of(user)

		val firstResponse = service.addMember(projectId, userId)
		val secondResponse = service.addMember(projectId, userId)

		assertEquals(1, firstResponse.members.size)
		assertEquals(1, secondResponse.members.size)
	}
}
