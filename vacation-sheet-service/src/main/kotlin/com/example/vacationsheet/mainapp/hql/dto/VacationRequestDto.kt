package com.example.vacationsheet.mainapp.hql.dto

import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.model.VacationType
import java.time.LocalDate
import java.time.OffsetDateTime

data class VacationRequestDto(
	val id: Long,
	val title: String,
	val requestState: VacationRequestState,
	val vacationType: VacationType,
	val startDate: LocalDate,
	val endDate: LocalDate,
	val userComments: String?,
	val managerComments: String?,
	val author: UserAccountDto,
	val manager: UserAccountDto?,
	val ctime: OffsetDateTime?,
	val utime: OffsetDateTime?,
)
