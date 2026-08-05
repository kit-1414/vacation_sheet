package com.example.vacationsheet.mainapp.hql.repository

import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProjectRepository : JpaRepository<ProjectEntity, UUID> {
	@Query("select distinct project from ProjectEntity project left join fetch project.members order by project.name")
	fun findAllWithMembers(): List<ProjectEntity>

	@Query("select distinct project from ProjectEntity project left join fetch project.members where project.id = :id")
	fun findByIdWithMembers(@Param("id") id: UUID): ProjectEntity?
}
