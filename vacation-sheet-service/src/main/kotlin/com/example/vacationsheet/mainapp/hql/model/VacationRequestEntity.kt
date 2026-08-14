package com.example.vacationsheet.mainapp.hql.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "vacation_requests")
@EntityListeners(AuditingEntityListener::class)
class VacationRequestEntity(
	@Column(nullable = false, length = 50)
	var title: String,

	@Enumerated(EnumType.STRING)
	@Column(name = "request_state", nullable = false, length = 20)
	var requestState: VacationRequestState,

	@Enumerated(EnumType.STRING)
	@Column(name = "vacation_type", nullable = false, length = 30)
	var vacationType: VacationType,

	@Column(name = "start_date", nullable = false)
	var startDate: LocalDate,

	@Column(name = "end_date", nullable = false)
	var endDate: LocalDate,

	@Column(name = "user_comments", length = 2000)
	var userComments: String?,

	@Column(name = "manager_comments", length = 2000)
	var managerComments: String? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	val author: UserAccountEntity,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id")
	var manager: UserAccountEntity? = null,

	@Column(nullable = false, updatable = false)
	@field:CreatedDate
	var ctime: OffsetDateTime? = null,

	@Column(nullable = false)
	@field:LastModifiedDate
	var utime: OffsetDateTime? = null,

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,
)
