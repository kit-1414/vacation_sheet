package com.example.vacationsheet.repository

import com.example.vacationsheet.entity.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccount, UUID> {
	fun findByYandexId(yandexId: String): UserAccount?

	fun findAllByOrderByDisplayNameAscEmailAsc(): List<UserAccount>
}
