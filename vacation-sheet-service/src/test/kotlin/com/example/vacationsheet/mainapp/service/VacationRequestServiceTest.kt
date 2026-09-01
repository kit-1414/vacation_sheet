package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.CurrentUserDto
import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.dto.VacationRequestManagerActionDto
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
	fun `find by id rejects access to another user's request`() {
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns false
		every { vacationRequestRepository.existsById(10L) } returns true

		assertFailsWith<VacationRequestAccessDeniedException> { service.findById(10L, currentUser()) }
		verify(exactly = 0) { vacationRequestRepository.findByIdWithUsers(any()) }
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
	fun `create rejects manager-only state`() {
		assertFailsWith<InvalidVacationRequestException> {
			service.create( request(VacationRequestState.APPROVED), currentUser())
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
	fun `manager list includes non-draft requests and author projects`() {
		val entity = entity(VacationRequestState.READY)
		val project = ProjectEntity("Project", null, id = 20L).also { it.members.add(author) }
		every { vacationRequestRepository.findAllByStatesWithUsers(any()) } returns listOf(entity)
		every { projectRepository.findAllWithMembersByMemberIds(setOf(1L)) } returns listOf(project)

		val response = service.getRequestsForManager()

		assertEquals("Project", response.single().authorProjects.single().name)
		verify(exactly = 1) {
			vacationRequestRepository.findAllByStatesWithUsers(
				setOf(VacationRequestState.READY, VacationRequestState.APPROVED, VacationRequestState.REJECTED),
			)
		}
	}

	@Test
	fun `manager cannot open draft request`() {
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity(VacationRequestState.DRAFT)

		assertFailsWith<ResourceNotFoundException> { service.findByIdForManager(10L) }
		verify(exactly = 0) { projectRepository.findAllWithMembersByMemberIds(any()) }
	}

	@Test
	fun `manager changes state and preserves comment when update flag is false`() {
		val entity = entity(VacationRequestState.READY).also { it.managerComments = "Keep this comment" }
		every { vacationRequestRepository.findByIdWithUsersForUpdate(10L) } returns entity
		every { userAccountRepository.findById(1L) } returns java.util.Optional.of(author)
		every { vacationRequestRepository.saveAndFlush(entity) } returns entity
		every { projectRepository.findAllWithMembersByMemberIds(setOf(1L)) } returns emptyList()

		val response = service.review(
			10L,
			currentUser(),
			VacationRequestManagerActionDto(null, false, VacationRequestState.APPROVED),
		)

		assertEquals(VacationRequestState.APPROVED, response.request.requestState)
		assertEquals("Keep this comment", response.request.managerComments)
		assertEquals(1L, response.request.manager?.id)
	}

	@Test
	fun `manager clears comment when update flag is true and comment is null`() {
		val entity = entity(VacationRequestState.APPROVED).also { it.managerComments = "Old comment" }
		every { vacationRequestRepository.findByIdWithUsersForUpdate(10L) } returns entity
		every { userAccountRepository.findById(1L) } returns java.util.Optional.of(author)
		every { vacationRequestRepository.saveAndFlush(entity) } returns entity
		every { projectRepository.findAllWithMembersByMemberIds(setOf(1L)) } returns emptyList()

		val response = service.review(
			10L,
			currentUser(),
			VacationRequestManagerActionDto(null, true, VacationRequestState.REJECTED),
		)

		assertEquals(VacationRequestState.REJECTED, response.request.requestState)
		assertEquals(null, response.request.managerComments)
	}

	@Test
	fun `returning request to ready clears manager and comment`() {
		val entity = entity(VacationRequestState.REJECTED).also {
			it.manager = author
			it.managerComments = "Rejected"
		}
		every { vacationRequestRepository.findByIdWithUsersForUpdate(10L) } returns entity
		every { vacationRequestRepository.saveAndFlush(entity) } returns entity
		every { projectRepository.findAllWithMembersByMemberIds(setOf(1L)) } returns emptyList()

		val response = service.review(
			10L,
			currentUser(),
			VacationRequestManagerActionDto("Ignored", true, VacationRequestState.READY),
		)

		assertEquals(VacationRequestState.READY, response.request.requestState)
		assertEquals(null, response.request.manager)
		assertEquals(null, response.request.managerComments)
		verify(exactly = 0) { userAccountRepository.findById(any()) }
	}

	@Test
	fun `manager cannot review draft request`() {
		every { vacationRequestRepository.findByIdWithUsersForUpdate(10L) } returns entity(VacationRequestState.DRAFT)

		assertFailsWith<VacationRequestModificationNotAllowedException> {
			service.review(
				10L,
				currentUser(),
				VacationRequestManagerActionDto(null, false, VacationRequestState.APPROVED),
			)
		}
		verify(exactly = 0) { vacationRequestRepository.saveAndFlush(any()) }
	}

	@Test
	fun `manager cannot set draft state`() {
		assertFailsWith<InvalidVacationRequestException> {
			service.review(
				10L,
				currentUser(),
				VacationRequestManagerActionDto(null, false, VacationRequestState.DRAFT),
			)
		}
		verify(exactly = 0) { vacationRequestRepository.findByIdWithUsersForUpdate(any()) }
	}

	private fun entity(state: VacationRequestState) = VacationRequestEntity(
		title = "Old",
		requestState = state,
		vacationType = VacationType.PAYMENT_VACATION,
		startDate = LocalDate.of(2026, 9, 1),
		endDate = LocalDate.of(2026, 9, 14),
		userComments = null,
		author = author,
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
