package com.example.vacationsheet.mainapp.hql.dto

import java.time.OffsetDateTime
import java.util.UUID

data class UserAccountDto(
	val id: UUID,
	val email: String,
	val firstName: String?,
	val lastName: String?,
	val ctime: OffsetDateTime?,
	val utime: OffsetDateTime?,
)
