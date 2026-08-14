package com.example.vacationsheet.mainapp.hql.mapper

import com.example.vacationsheet.mainapp.dto.ProjectRequestDto
import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProjectMapperTest {
	private val mapper = ProjectMapper(UserAccountMapper())

	@Test
	fun `to entity ignores audit times`() {
		val entity = mapper.toEntity(ProjectRequestDto("Project", "Description"))

		assertNull(entity.ctime)
		assertNull(entity.utime)
	}

	@Test
	fun `update entity preserves audit times`() {
		val creationTime = time(1)
		val updateTime = time(2)
		val entity = ProjectEntity("Old", null, ctime = creationTime, utime = updateTime)

		mapper.updateEntity(ProjectRequestDto("New", "Description"), entity)

		assertEquals(creationTime, entity.ctime)
		assertEquals(updateTime, entity.utime)
	}

	private fun time(day: Int) = OffsetDateTime.of(2026, 1, day, 0, 0, 0, 0, ZoneOffset.UTC)
}
