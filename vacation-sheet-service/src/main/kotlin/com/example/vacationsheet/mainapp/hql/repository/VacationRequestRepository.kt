package com.example.vacationsheet.mainapp.hql.repository

import com.example.vacationsheet.mainapp.hql.model.VacationRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface VacationRequestRepository : JpaRepository<VacationRequestEntity, Long> {
	@Query(
		"select request from VacationRequestEntity request join fetch request.author " +
			"left join fetch request.manager where request.author.id = :ownerId order by request.ctime desc",
	)
	fun findAllByOwnerId(@Param("ownerId") ownerId: Long): List<VacationRequestEntity>

	@Query(
		"select request from VacationRequestEntity request join fetch request.author " +
			"left join fetch request.manager where request.id = :id",
	)
	fun findByIdWithUsers(@Param("id") id: Long): VacationRequestEntity?

	fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean
}
