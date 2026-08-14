package com.example.vacationsheet.mainapp.hql.mapper

import com.example.vacationsheet.mainapp.dto.VacationRequestRequestDto
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.model.VacationType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VacationRequestMapperTest {
	private val mapper = VacationRequestMapper(UserAccountMapper())
	private val author = UserAccountEntity("user@example.com", "Test", "User", id = 1L)

	@Test
	fun `to entity sets author and ignores server managed fields`() {
		val entity = mapper.toEntity(request(), author)

		assertEquals(author, entity.author)
		assertNull(entity.manager)
		assertNull(entity.managerComments)
		assertNull(entity.ctime)
		assertNull(entity.utime)
	}

	@Test
	fun `update preserves server managed fields`() {
		val creationTime = time(1)
		val updateTime = time(2)
		val manager = UserAccountEntity("manager@example.com", "Test", "Manager", id = 2L)
		val entity = VacationRequestEntity(
			title = "Old",
			requestState = VacationRequestState.DRAFT,
			vacationType = VacationType.FREE_VACATION,
			startDate = LocalDate.of(2026, 8, 20),
			endDate = LocalDate.of(2026, 8, 21),
			userComments = null,
			managerComments = "Manager comment",
			author = author,
			manager = manager,
			ctime = creationTime,
			utime = updateTime,
		)

		mapper.updateEntity(request(), entity)

		assertEquals(author, entity.author)
		assertEquals(manager, entity.manager)
		assertEquals("Manager comment", entity.managerComments)
		assertEquals(creationTime, entity.ctime)
		assertEquals(updateTime, entity.utime)
	}

	private fun request() = VacationRequestRequestDto(
		title = " Vacation ",
		requestState = VacationRequestState.READY,
		vacationType = VacationType.PAYMENT_VACATION,
		startDate = LocalDate.of(2026, 9, 1),
		endDate = LocalDate.of(2026, 9, 14),
		userComments = "Comment",
	)

	private fun time(day: Int) = OffsetDateTime.of(2026, 1, day, 0, 0, 0, 0, ZoneOffset.UTC)
}
