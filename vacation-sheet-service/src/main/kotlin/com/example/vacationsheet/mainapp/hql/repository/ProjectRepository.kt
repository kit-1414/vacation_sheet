package com.example.vacationsheet.mainapp.hql.repository

import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProjectRepository : JpaRepository<ProjectEntity, Long> {
	fun findByNameIgnoreCase(name: String): ProjectEntity?
	fun existsByManagersId(userId: Long): Boolean

	@Query("select distinct project from ProjectEntity project left join fetch project.members left join fetch project.managers order by project.name")
	fun findAllWithMembers(): List<ProjectEntity>

	@Query("select distinct project from ProjectEntity project left join fetch project.members left join fetch project.managers where project.id = :id")
	fun findByIdWithMembers(@Param("id") id: Long): ProjectEntity?
}
