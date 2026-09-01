package com.example.vacationsheet.mainapp.dto

import com.example.vacationsheet.mainapp.hql.model.VacationRequestState
import jakarta.validation.constraints.Size

data class VacationRequestManagerActionDto(
	@field:Size(max = 2000)
	val managerComment: String?,

	val updateManagerComment: Boolean,

	val requestState: VacationRequestState,
)
