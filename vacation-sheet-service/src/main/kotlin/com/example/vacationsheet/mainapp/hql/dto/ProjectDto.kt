package com.example.vacationsheet.mainapp.hql.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ProjectDto(
	val id: UUID,
	val name: String,
	val description: String?,
	val members: List<UserAccountDto>,
	val ctime: OffsetDateTime?,
	val utime: OffsetDateTime?,
)
