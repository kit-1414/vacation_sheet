package com.example.vacationsheet.mainapp.hql.handler

import com.example.vacationsheet.mainapp.hql.model.ProjectEntity
import com.example.vacationsheet.mainapp.hql.model.UserAccountEntity
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.OffsetDateTime
import java.time.ZoneOffset

class JpaTimeHandler {
	@PrePersist
	fun prePersist(entity: Any) {
		val now = OffsetDateTime.now(ZoneOffset.UTC)
		when (entity) {
			is ProjectEntity -> {
				entity.ctime = entity.ctime ?: now
				entity.utime = now
			}
			is UserAccountEntity -> {
				entity.ctime = entity.ctime ?: now
				entity.utime = now
			}
		}
	}

	@PreUpdate
	fun preUpdate(entity: Any) {
		val now = OffsetDateTime.now(ZoneOffset.UTC)
		when (entity) {
			is ProjectEntity -> entity.utime = now
			is UserAccountEntity -> entity.utime = now
		}
	}
}
