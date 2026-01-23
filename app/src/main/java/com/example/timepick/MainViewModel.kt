package com.example.timepick

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.timepick.data.AppDatabase
import com.example.timepick.data.entity.UserEntity
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.timepick.data.entity.JobEntity
import com.example.timepick.data.entity.JobTimeEntity
import com.example.timepick.data.entity.UserTimeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.timepick.data.entity.AppliedJobEntity


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val userDao = AppDatabase.getInstance(application).userDao()
    private val userTimeDao = database.userTimeDao()
    private val jobDao = database.jobDao()
    private val jobTimeDao = database.jobTimeDao()

    // JobEntity 대신 JobMatchResult를 사용하여 일치율까지 전달
    private val _matchedJobs = MutableStateFlow<List<JobMatchResult>>(emptyList())
    val matchedJobs: StateFlow<List<JobMatchResult>> = _matchedJobs

    // UI에서 '검색을 수행했는지' 여부를 알기 위한 상태 (결과 없음 화면 노출용)
    private val _isSearchPerformed = MutableStateFlow(false)
    val isSearchPerformed: StateFlow<Boolean> = _isSearchPerformed

    // 현재 사용자가 클릭하여 선택한 공고의 상세 정보를 담는 상태
    private val _selectedJob = MutableStateFlow<JobEntity?>(null)
    val selectedJob: StateFlow<JobEntity?> = _selectedJob

    private val appliedJobDao = database.appliedJobDao()

    // 사용자가 특정 공고에 지원했는지 상태를 저장할 변수
    private val _isAlreadyApplied = MutableStateFlow<Boolean>(false)
    val isAlreadyApplied: StateFlow<Boolean> = _isAlreadyApplied

    // UI에 출력할 정보를 담는 클래스
    data class JobMatchResult(
        val job: JobEntity,
        val matchRate: Int // 일치율 (%)
    )

    /* ---------- 데이터 삽입 테스트용 코드 ---------- */
    init {
        viewModelScope.launch {
            // 1. 더미 데이터 삽입 (최초 1회 실행)
            insertDummyData()
            /*
            // 2. 가상의 사용자 데이터 저장
            val testUserIndices = listOf(215, 216,217,222,223,229,230,236,237,243,244)
            saveUserTimes(userId = 1, selectedTimeIndices = testUserIndices) { success ->
                android.util.Log.d("TEST_LOG", "사용자 시간 저장 성공 여부: $success")
            }

             */

            // 3. 매칭 로직 실행
            //findMatchingJobs(testUserIndices)
        }

        // 4. 관찰(Collect) - 매칭된 공고가 결과로 나오는지 감시
        viewModelScope.launch {
            matchedJobs.collect { jobs ->
                android.util.Log.d("TEST_LOG", "매칭된 공고 개수: ${jobs.size}")
                jobs.forEach {
                    android.util.Log.d("TEST_LOG", "매칭된 공고명: ${it.job.title}, 일치율: ${it.matchRate}%")
                }
            }
        }
    }

    /* ---------- 로그인 ---------- */

    // 로그인 성공 시 유저 정보 반환, 아이디/비번 틀리면 null 반환
    fun logIn(email: String, password: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val hashed = hashPassword(password)
            val user = withContext(Dispatchers.IO) {
                userDao.login(email, hashed)
            }
            onResult(user)
        }
    }

    /* ---------- 회원가입 / 탈퇴 ---------- */

    // 회원 가입 함수 (가입 완료시 true 반환)
    fun signUp(email: String, password: String, name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val hashed = hashPassword(password)
            val user = UserEntity(email = email, password = hashed, name = name)

            // DB에 회원 저장
            withContext(Dispatchers.IO) {
                userDao.insertUser(user)
            }
            onResult(true)
        }
    }

    // 회원 탈퇴 함수 (탈퇴 완료 시 true 반환)
    fun deleteUser(userId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // 회원 삭제
            withContext(Dispatchers.IO) {
                userDao.deleteUserById(userId)
            }
            onResult(true)
        }
    }

    /* ---------- 유효성 검사, 해시화 ---------- */

    // 이메일(=아이디) 중복 검사 함수
    // 검사 결과 중복 아이디가 없으면 (=null이면) true 반환 (사용 가능)
    fun isEmailAvailable(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { userDao.getUserByEmail(email) }
            onResult(user == null)
        }
    }

    // 비밀번호 8자 이상 검사 함수 (8자 이상이면 true 반환)
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    // 비밀번호 재확인 검사 함수
    fun doPasswordMatch(password: String, confirm: String): Boolean {
        return password == confirm
    }

    // 비밀번호 해시화 함수
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /* ---------- 마이페이지 사용자 정보 수정 ---------- */

    fun updateUserInfo(
        userId: Int,
        newName: String,
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // 유저 정보 조회
            val user = withContext(Dispatchers.IO) {
                userDao.getUserById(userId)
            }

            if (user == null) {
                onResult(false)
                return@launch
            }

            // 현재 비밀번호 검증 (필수)
            val currentHashed = hashPassword(currentPassword)
            if (user.password != currentHashed) {
                onResult(false)
                return@launch
            }

            // 새 비밀번호가 있음 -> 해시, 없으면 기존 비밀번호 사용
            val passwordToSave = if (newPassword.isBlank()) {
                user.password
            } else {
                hashPassword(newPassword)
            }

            // 사용자 정보 업데이트
            withContext(Dispatchers.IO) {
                userDao.updateUser(
                    userId = userId,
                    name = newName,
                    password = passwordToSave
                )
            }
            onResult(true)
        }
    }

    /* ---------- 공고 초기 더미 데이터 삽입 ---------- */
    // 앱 초기 실행 시 테스트를 위한 공고 데이터와 공고별 근무 시간 데이터를 DB에 저장하는 함수
    private suspend fun insertDummyData() {
        withContext(Dispatchers.IO) {
            val existingJobs = jobDao.getAllJobsWithTimes()
            if (existingJobs.isNotEmpty()) {
                android.util.Log.d("TEST_LOG", "이미 데이터가 존재하여 더미 데이터를 넣지 않습니다.")
                return@withContext
            }
            // JobEntity 리스트 생성 (공고의 이름, 시급, 위치 등 상세 정보)
            val jobs = listOf(
                JobEntity(
                    title = "파리바게뜨 노원점",
                    hourlyRate = 10320,
                    category = "베이커리",
                    employmentType = "아르바이트",
                    deadline = "2026.02.15",
                    recruitCount = 2,
                    recruitField = "빵 진열 및 판매",
                    education = "고졸 이상",
                    preferences = "인근 거주자, 보건증 소지자",
                    location = "노원구",
                    address = "서울특별서 노원구 상계동 123-4",
                    description = "오전 타임 빵 진열과 계산 도와주실 성실한 분을 찾습니다."
                ),
                JobEntity(
                    title = "스타벅스 공릉DT점",
                    hourlyRate = 11500,
                    category = "카페",
                    employmentType = "계약직",
                    deadline = "2026.02.20",
                    recruitCount = 3,
                    recruitField = "바리스타",
                    education = "무관",
                    preferences = "관련 업무 경험자 우대",
                    location = "노원구",
                    address = "서울특별시 노원구 화랑로 456",
                    description = "함께 즐겁게 일할 바리스타 분들을 모집합니다."
                ),
                JobEntity(
                    title = "맥도날드 강남삼성점",
                    hourlyRate = 12000,
                    category = "패스트푸드",
                    employmentType = "아르바이트",
                    deadline = "2026.02.25",
                    recruitCount = 5,
                    recruitField = "주방/카운터 크루",
                    education = "무관",
                    preferences = "보건증 소지자, 장기 근무 가능자 우대",
                    location = "강남구",
                    address = "서울특별시 강남구 테헤란로 123",
                    description = "강남 중심가에서 활기차게 일하실 크루분들을 모집합니다. 주말 야간 근무 가능자 우대합니다."
                ),
                JobEntity(
                    title = "북카페 '문장'",
                    hourlyRate = 10500,
                    category = "문화/여가",
                    employmentType = "계약직",
                    deadline = "2026.03.01",
                    recruitCount = 1,
                    recruitField = "도서 정리 및 음료 제조",
                    education = "대졸 예정자 이상",
                    preferences = "책에 대한 이해도가 높으신 분, 바리스타 자격증 소지자 우대",
                    location = "마포구",
                    address = "서울특별시 마포구 양화로 789",
                    description = "책과 커피를 사랑하는 분들의 지원을 기다립니다. 조용한 분위기에서 꼼꼼하게 일하실 분 환영합니다."
                )
            )
            val jobIds = jobDao.insertAllJobs(jobs)


            //각 공고별 시간 인덱스 데이터 생성 (0~251 범위 내의 시간대 설정)

            val allJobTimes = mutableListOf<JobTimeEntity>()

            for (time in 0..7) {
                for (day in 0..4) {
                    allJobTimes.add(JobTimeEntity(jobId = 1, timeIndex = day + (time * 7)))
                }
            }

            for (time in 10..17) {
                for (day in 0..4) {
                    allJobTimes.add(JobTimeEntity(jobId = 2, timeIndex = day + (time * 7)))
                }
            }

            for (time in 30..35) {
                for (day in 5..6) {
                    allJobTimes.add(JobTimeEntity(jobId = 3, timeIndex = day + (time * 7)))
                }
            }

            for (time in 14..21) {
                for (day in listOf(0, 2, 4)) {
                    allJobTimes.add(JobTimeEntity(jobId = 4, timeIndex = day + (time * 7)))
                }
            }


            jobTimeDao.insertJobTimes(allJobTimes)
        }
    }


    /* ---------- 알바생 선택 시간 저장 ---------- */
    // 사용자가 타임테이블에서 선택한 시간 인덱스들을 DB에 저장하는 함수
    fun saveUserTimes(userId: Int, selectedTimeIndices: List<Int>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 기존에 저장된 해당 사용자의 시간 데이터를 모두 삭제 (초기화)
                    userTimeDao.deleteUserTimes(userId) // 필요 시 Dao에 추가

                    // 새로 선택된 인덱스들을 UserTimeEntity 객체 리스트로 변환
                    val userTimes = selectedTimeIndices.map { index ->
                        UserTimeEntity(
                            userId = userId,
                            timeIndex = index
                        )
                    }

                    // 변환된 리스트를 DB에 최종 저장
                    userTimeDao.insertUserTimes(userTimes)
                }
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
    /* ---------- 알바생 선택 시간 불러오기 ---------- */
    // DB에 저장되어 있는 사용자의 시간 인덱스 리스트를 가져오는 함수 (조회 후 뒤로가기버튼)

    fun loadUserTimes(userId: Int, onResult: (List<Int>) -> Unit) {
        viewModelScope.launch {
            val savedIndices = withContext(Dispatchers.IO) {
                // userTimeDao를 통해 해당 사용자의 ID로 저장된 시간 인덱스들만 조회
                userTimeDao.getUserTimes(userId)
            }
            onResult(savedIndices)
        }
    }

    /* ---------- 매칭 공고 검색 (알바생 시간과 공고 시간 대조) ---------- */
    // 사용자가 가능한 시간과 공고의 요구 시간을 비교하여 80% 이상 일치하는 공고를 찾는 함수

    fun findMatchingJobs(userSelectedIndices: List<Int>) {
        viewModelScope.launch {
            _isSearchPerformed.value = true

            val allJobsWithTimes = withContext(Dispatchers.IO) {
                jobDao.getAllJobsWithTimes()
            }

            // 1. 시간 선택 안 하고 확인 버튼 클릭 시 -> 전체 공고 노출
            if (userSelectedIndices.isEmpty()) {
                _matchedJobs.value = allJobsWithTimes.map {
                    JobMatchResult(it.job, -1) // 시간이 선택되지 않은 경우 일치율 텍스트뷰를 숨김
                }
                return@launch
            }

            // 2. 사용자가 선택한 시간이 있는 경우 (80% 이상 매칭)
            val resultList = allJobsWithTimes.map { jobWithTimes ->
                val jobTimeIndices = jobWithTimes.times.map { it.timeIndex }
                val matchingCount = userSelectedIndices.intersect(jobTimeIndices.toSet()).size
                val matchRate = if (jobTimeIndices.isNotEmpty()) {
                    (matchingCount.toDouble() / jobTimeIndices.size * 100).toInt()
                } else 0

                JobMatchResult(jobWithTimes.job, matchRate)
            }.filter { it.matchRate >= 80 } // 80% 이상만 필터링
                .sortedByDescending { it.matchRate }

            _matchedJobs.value = resultList
        }
    }

    /* ---------- 공고 누르면 상세 공고 내용 보기 ---------- */
    // userId를 추가로 받아서 지원 여부까지 한 번에 업데이트합니다.
    fun selectJob(job: JobEntity, userId: Int) {
        _selectedJob.value = job
        // 공고를 선택하자마자 해당 유저가 지원했는지 바로 체크
        checkIfApplied(userId, job.jobId)
    }

    // 상세 페이지를 닫을 때 상태 초기화
    fun clearSelectedJob() {
        _selectedJob.value = null
        _isAlreadyApplied.value = false // 다음 공고를 위해 상태를 깨끗이 비워둠
    }


    /* ---------- 지원하기 기능 ---------- */
    fun applyToJob(userId: Int, jobId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 이미 지원했는지 한 번 더 확인 후 삽입.
            val existing = appliedJobDao.getAppliedJob(userId, jobId)
            if (existing == null) {
                appliedJobDao.insertAppliedJob(AppliedJobEntity(userId, jobId))
                _isAlreadyApplied.value = true
            }
        }
    }

    /* ---------- 지원 여부 확인 ---------- */
    fun checkIfApplied(userId: Int, jobId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = appliedJobDao.getAppliedJob(userId, jobId)
            _isAlreadyApplied.value = (result != null)
        }
    }

}

