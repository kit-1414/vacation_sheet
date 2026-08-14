package com.example.vacationsheet.mainapp.hql.mapper

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class UserAccountMapperTest {
	private val mapper = UserAccountMapper()

	@Test
	fun `to dto includes audit times`() {
		val creationTime = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
		val updateTime = creationTime.plusDays(1)
		val entity = UserAccountEntity(
			"user@example.com",
			"Test",
			"User",
			ctime = creationTime,
			utime = updateTime,
			id = 1L,
		)

		val dto = mapper.toDto(entity)

		assertEquals(creationTime, dto.ctime)
		assertEquals(updateTime, dto.utime)
	}
}
