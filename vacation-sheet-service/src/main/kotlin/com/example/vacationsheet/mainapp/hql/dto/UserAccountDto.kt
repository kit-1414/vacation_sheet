package com.example.vacationsheet.mainapp.hql.dto

import java.time.OffsetDateTime

data class UserAccountDto(
	val id: Long,
	val email: String,
	val firstName: String?,
	val lastName: String?,
	val isAdmin: Boolean,
	val isActive: Boolean,
	val ctime: OffsetDateTime?,
	val utime: OffsetDateTime?,
)
