package com.example.vacationsheet.mainapp.service

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.exception.InvalidVacationRequestException
import com.example.vacationsheet.mainapp.exception.VacationRequestAccessDeniedException
import com.example.vacationsheet.mainapp.exception.VacationRequestModificationNotAllowedException
import com.example.vacationsheet.mainapp.hql.mapper.UserAccountMapper
import com.example.vacationsheet.mainapp.hql.mapper.VacationRequestMapper
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.model.VacationType
import com.example.vacationsheet.mainapp.hql.repository.UserAccountRepository
import com.example.vacationsheet.mainapp.hql.repository.VacationRequestRepository
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
	private val mapper = VacationRequestMapper(UserAccountMapper())
	private val service = VacationRequestService(vacationRequestRepository, userAccountRepository, mapper)
	private val author = UserAccountEntity("user@example.com", "Test", "User", id = 1L)

	@Test
	fun `find by id rejects access to another user's request`() {
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns false
		every { vacationRequestRepository.existsById(10L) } returns true

		assertFailsWith<VacationRequestAccessDeniedException> { service.findById(10L, 1L) }
		verify(exactly = 0) { vacationRequestRepository.findByIdWithUsers(any()) }
	}

	@Test
	fun `update changes an owned draft request`() {
		val entity = entity(VacationRequestState.DRAFT)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity
		every { vacationRequestRepository.saveAndFlush(entity) } returns entity

		val response = service.update(10L, 1L, request(VacationRequestState.READY))

		assertEquals(VacationRequestState.READY, response.requestState)
		assertEquals("Vacation", response.title)
	}

	@Test
	fun `update rejects an approved request`() {
		val entity = entity(VacationRequestState.APPROVED)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity

		assertFailsWith<VacationRequestModificationNotAllowedException> {
			service.update(10L, 1L, request(VacationRequestState.DRAFT))
		}
		verify(exactly = 0) { vacationRequestRepository.saveAndFlush(any()) }
	}

	@Test
	fun `create rejects manager-only state`() {
		assertFailsWith<InvalidVacationRequestException> {
			service.create(1L, request(VacationRequestState.APPROVED))
		}
		verify(exactly = 0) { userAccountRepository.findById(any()) }
	}

	@Test
	fun `delete removes an owned ready request`() {
		val entity = entity(VacationRequestState.READY)
		every { vacationRequestRepository.existsByIdAndAuthorId(10L, 1L) } returns true
		every { vacationRequestRepository.findByIdWithUsers(10L) } returns entity
		every { vacationRequestRepository.delete(entity) } returns Unit

		service.delete(10L, 1L)

		verify(exactly = 1) { vacationRequestRepository.delete(entity) }
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
}
