package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.exception.InvalidVacationRequestException
import com.example.vacationsheet.mainapp.exception.ResourceNotFoundException
import com.example.vacationsheet.mainapp.exception.VacationRequestAccessDeniedException
import com.example.vacationsheet.mainapp.exception.VacationRequestModificationNotAllowedException
import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.mapper.VacationRequestMapper
import com.example.vacationsheet.mainapp.hql.mapper.ProjectMapper
import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.model.VacationType
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import com.example.vacationsheet.mainapp.hql.repository.VacationRequestRepository
import com.example.vacationsheet.mainapp.hql.repository.ProjectRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VacationRequestServiceTest {
	private val vacationRequestRepository = mockk<VacationRequestRepository>()
	private val userAccountRepository = mockk<UserAccountRepository>()
	private val projectRepository = mockk<ProjectRepository>()
	private val userAccountMapper = UserAccountMapper()
	private val mapper = VacationRequestMapper(userAccountMapper)
	private val projectMapper = ProjectMapper(userAccountMapper)
	private val service = VacationRequestService(
		vacationRequestRepository,
		userAccountRepository,
		mapper,
		projectRepository,
		projectMapper,
	)
	private val author = UserAccountEntity("user@example.com", "Test", "User", id = 1L)

	@Test
	fun `find by id returns another user's request`() {
		val anotherAuthor = UserAccountEntity("another@example.com", "Another", "User", id = 2L)
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns
			entity(VacationRequestState.READY, anotherAuthor)

		val response = service.findById(10L)

		assertEquals(2L, response.author.id)
		verify(exactly = 0) { vacationRequestRepository.existsByIdAndAuthorId(any(), any()) }
	}

	@Test
	fun `find by id rejects missing request`() {
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns null

		assertFailsWith<ResourceNotFoundException> { service.findById(10L) }
	}

	@Test
	fun `update changes an owned draft request`() {
		val entity = entity(VacationRequestState.DRAFT)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity
		every { vacationRequestRepository.saveAndFlush(entity) } returns entity

		val response = service.update(10L, currentUser(), request(VacationRequestState.READY))

		assertEquals(VacationRequestState.READY, response.requestState)
		assertEquals("Vacation", response.title)
	}

	@Test
	fun `update rejects an approved request`() {
		val entity = entity(VacationRequestState.APPROVED)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity

		assertFailsWith<VacationRequestModificationNotAllowedException> {
			service.update(10L, currentUser(), request(VacationRequestState.DRAFT))
		}
		verify(exactly = 0) { vacationRequestRepository.saveAndFlush(any()) }
	}

	@Test
	fun `update rejects an in-progress request`() {
		val entity = entity(VacationRequestState.IN_PROGRESS)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity

		assertFailsWith<VacationRequestModificationNotAllowedException> {
			service.update(10L, currentUser(), request(VacationRequestState.DRAFT))
		}
		verify(exactly = 0) { vacationRequestRepository.saveAndFlush(any()) }
	}

	@Test
	fun `update rejects another user's request`() {
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns false
		every { vacationRequestRepository.existsById(10L) } returns true

		assertFailsWith<VacationRequestAccessDeniedException> {
			service.update(10L, currentUser(), request(VacationRequestState.READY))
		}
		verify(exactly = 0) { vacationRequestRepository.findByIdWithUsers(any()) }
		verify(exactly = 0) { vacationRequestRepository.saveAndFlush(any()) }
	}

	@Test
	fun `create rejects read-only state Approved`() {
		assertFailsWith<InvalidVacationRequestException> {
			service.create( request(VacationRequestState.APPROVED), currentUser())
		}
		verify(exactly = 0) { userAccountRepository.findById(any()) }
	}

	@Test
	fun `create rejects read-only state IN_PROGRESS`() {
		assertFailsWith<InvalidVacationRequestException> {
			service.create(request(VacationRequestState.IN_PROGRESS), currentUser())
		}
		verify(exactly = 0) { userAccountRepository.findById(any()) }
	}

	@Test
	fun `delete removes an owned ready request`() {
		val entity = entity(VacationRequestState.READY)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity
		every { vacationRequestRepository.delete(entity) } returns Unit

		service.delete(10L, currentUser())

		verify(exactly = 1) { vacationRequestRepository.delete(entity) }
	}

	@Test
	fun `delete rejects another user's request`() {
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns false
		every { vacationRequestRepository.existsById(10L) } returns true

		assertFailsWith<VacationRequestAccessDeniedException> { service.delete(10L, currentUser()) }
		verify(exactly = 0) { vacationRequestRepository.findByIdWithUsers(any()) }
		verify(exactly = 0) { vacationRequestRepository.delete(any()) }
	}

	@Test
	fun `all requests list includes non-draft requests and author projects`() {
		val entity = entity(VacationRequestState.READY)
		val project = ProjectEntity("Project", null, id = 20L).also { it.members.add(author) }
		every { vacationRequestRepository.findAllExceptStateWithUsers(VacationRequestState.DRAFT) } returns listOf(entity)
		every { projectRepository.findAllWithMembersByMemberIds(setOf(1L)) } returns listOf(project)

		val response = service.getAllRequests()

		assertEquals("Project", response.single().authorProjects.single().name)
		verify(exactly = 1) {
			vacationRequestRepository.findAllExceptStateWithUsers(VacationRequestState.DRAFT)
		}
	}

	private fun entity(
		state: VacationRequestState,
		requestAuthor: UserAccountEntity = author,
	) = VacationRequestEntity(
		title = "Old",
		requestState = state,
		vacationType = VacationType.PAYMENT_VACATION,
		startDate = LocalDate.of(2026, 9, 1),
		endDate = LocalDate.of(2026, 9, 14),
		userComments = null,
		author = requestAuthor,
		id = 10L,
	)

	private fun request(state: VacationRequestState) = VacationRequestRequestDto(
		title = " Vacation ",
		requestState = state,
		vacationType = VacationType.PAYMENT_VACATION,
		startDate = LocalDate.of(2026, 9, 1),
		endDate = LocalDate.of(2026, 9, 14),
	)

	private fun currentUser() = CurrentUserDto(
		id = 1L,
		email = "user@example.com",
		firstName = "Test",
		lastName = "User",
		isAdmin = false,
		isActive = true,
		ctime = null,
		utime = null,
		roles = setOf(UserRole.USER),
	)
}
