package com.example.vacationsheet.mainapp.dto

import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import com.example.vacationsheet.mainapp.hql.model.VacationType
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class VacationRequestRequestDto(
	@field:NotBlank
	@field:Size(max = 50)
	val title: String,

	val requestState: VacationRequestState,

	val vacationType: VacationType,

	val startDate: LocalDate,

	val endDate: LocalDate,

	@field:Size(max = 2000)
	val userComments: String? = null,
) {
	@get:AssertTrue(message = "End date must not be before start date")
	@get:JsonIgnore
	val isDateRangeValid: Boolean
		get() = !endDate.isBefore(startDate)
}
