package com.example.vacationsheet.mainapp.hql.dto

data class ManagerVacationRequestDto(
	val request: VacationRequestDto,
	val authorProjects: List<ProjectSummaryDto>,
)
