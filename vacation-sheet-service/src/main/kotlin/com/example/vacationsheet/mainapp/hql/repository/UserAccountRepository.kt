package com.example.vacationsheet.mainapp.hql.repository

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserAccountRepository : JpaRepository<UserAccountEntity, Long> {
	fun findByEmail(email: String): UserAccountEntity?

	fun findAllByOrderByLastNameAscFirstNameAscEmailAsc(): List<UserAccountEntity>
}
