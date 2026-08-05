package com.example.vacationsheet.repository

import com.example.vacationsheet.entity.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProjectRepository : JpaRepository<Project, UUID> {
	@Query("select distinct project from Project project left join fetch project.members order by project.name")
	fun findAllWithMembers(): List<Project>

	@Query("select distinct project from Project project left join fetch project.members where project.id = :id")
	fun findByIdWithMembers(@Param("id") id: UUID): Project?
}
