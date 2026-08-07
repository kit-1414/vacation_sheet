package com.example.vacationsheet.mainapp.hql.dto

import java.time.OffsetDateTime

data class ProjectDto(
	val id: Long,
	val name: String,
	val description: String?,
	val members: List<UserAccountDto>,
	val ctime: OffsetDateTime?,
	val utime: OffsetDateTime?,
)
