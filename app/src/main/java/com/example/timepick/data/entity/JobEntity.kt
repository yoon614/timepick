package com.example.timepick.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "jobs")
data class JobEntity(
    // 1. 기본키: 각 공고를 구분하는 고유 ID
    @PrimaryKey(autoGenerate = true)
    val jobId: Int = 0,

    // 2. 상단 기본 정보
    val title: String,            // 알바공고 제목 (예: 알바공고1)
    val hourlyRate: Int,          // 시급 (예: 10320)
    val category: String,         // 업종 (예: 베이커리, 카페, 음식점) 모집 중인 공고에 출력
    val employmentType: String,   // 고용 형태 (예: 계약직)

    // 3. 모집 조건 상세
    val deadline: String,         // 모집 마감 (예: 2026.XX.XX)
    val recruitCount: Int,        // 모집 인원 (예: 2)
    val recruitField: String,     // 모집 분야 (예: 주방, 조리, 홀)
    val education: String,        // 학력 (예: 고등학교 졸업 이상)
    val preferences: String,      // 우대 사항 (예: 유사업무 경험 우대)

    // 4. 기타 정보
    val location: String,         // 근무지역 (예: 서울특별시 노원구...)
    val address : String,         // 상세 주소 (예: 서울특별시 노원구 상계동 123-4) 모집중인 공고에 출력
    val description: String       // 상세 요강 텍스트
)

data class JobWithTimes(
    @Embedded val job: JobEntity,
    @Relation(
        parentColumn = "jobId",    // JobEntity의 PK
        entityColumn = "jobId"     // JobTimeEntity의 FK
    )
    val times: List<JobTimeEntity>
)

// 매칭된 공고 정보와 일치율을 함께 담는 클래스
data class MatchedJobResult(
    val job: JobEntity,
    val matchRate: Double
)