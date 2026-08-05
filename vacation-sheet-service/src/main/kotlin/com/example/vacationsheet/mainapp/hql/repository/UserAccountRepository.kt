package com.example.vacationsheet.mainapp.hql.repository

import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccountEntity, UUID> {
	fun findByYandexId(yandexId: String): UserAccountEntity?

	fun findAllByOrderByDisplayNameAscEmailAsc(): List<UserAccountEntity>
}
