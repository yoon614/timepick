package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * JobListActivity - 모집 중인 공고 리스트 화면
 *
 * 플로우:
 * - 타임픽에서 선택한 시간과 80% 이상 일치하는 공고 표시
 * - 공고 카드 클릭 -> JobDetailActivity(공고 상세)로 이동
 * - 공고 없으면 Empty View 표시
 */
class JobListActivity : AppCompatActivity() {

    // UI 컴포넌트
    private lateinit var btnBack: ImageButton
    private lateinit var rvJobList: RecyclerView
    private lateinit var layoutEmptyView: LinearLayout

    // 공고 리스트 어댑터
    private lateinit var jobListAdapter: JobListAdapter

    // 임시 공고 데이터 (TODO: 추후 DB 연동)
    private var jobList = mutableListOf<JobData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_list)

        // View 초기화
        initViews()

        // 임시 데이터 로드
        loadJobData()

        // RecyclerView 설정
        setupRecyclerView()

        // 클릭 리스너 설정
        setupClickListeners()
    }

    /**
     * View 초기화
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btn_detail_back)
        rvJobList = findViewById(R.id.rv_job_list)
        layoutEmptyView = findViewById(R.id.layout_empty_view)
    }

    /**
     * 임시 공고 데이터 로드
     * TODO: 추후 서버 API 또는 DB에서 불러오기
     */
    private fun loadJobData() {
        // 임시 더미 데이터 생성
        jobList = mutableListOf(
            JobData(
                id = 1,
                title = "카페 알바 구해요",
                location = "서울 강남구",
                category = "카페/음료",
                matchRate = 95
            ),
            JobData(
                id = 2,
                title = "편의점 야간 근무",
                location = "서울 서초구",
                category = "편의점",
                matchRate = 88
            ),
            JobData(
                id = 3,
                title = "PC방 알바",
                location = "서울 관악구",
                category = "PC방",
                matchRate = 82
            )
        )

        // 공고 유무에 따라 UI 전환
        if (jobList.isEmpty()) {
            rvJobList.visibility = View.GONE
            layoutEmptyView.visibility = View.VISIBLE
        } else {
            rvJobList.visibility = View.VISIBLE
            layoutEmptyView.visibility = View.GONE
        }
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        rvJobList.layoutManager = LinearLayoutManager(this)

        jobListAdapter = JobListAdapter(jobList) { jobData ->
            // 공고 카드 클릭 시 상세 화면으로 이동
            val intent = Intent(this, JobDetailActivity::class.java)
            intent.putExtra("JOB_ID", jobData.id)
            intent.putExtra("JOB_TITLE", jobData.title)
            intent.putExtra("JOB_LOCATION", jobData.location)
            intent.putExtra("JOB_CATEGORY", jobData.category)
            intent.putExtra("JOB_MATCH_RATE", jobData.matchRate)
            startActivity(intent)
        }

        rvJobList.adapter = jobListAdapter
    }

    /**
     * 클릭 이벤트 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }
    }
}

/**
 * JobData - 공고 데이터 클래스 (임시)
 * TODO: 추후 실제 Entity로 교체
 */
data class JobData(
    val id: Int,
    val title: String,
    val location: String,
    val category: String,
    val matchRate: Int
)

/**
 * JobListAdapter - 공고 리스트 어댑터
 */
class JobListAdapter(
    private val jobList: List<JobData>,
    private val onItemClick: (JobData) -> Unit
) : RecyclerView.Adapter<JobListAdapter.JobViewHolder>() {

    class JobViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: android.widget.TextView = view.findViewById(R.id.tv_job_title)
        val tvLocation: android.widget.TextView = view.findViewById(R.id.tv_location)
        val tvCategory: android.widget.TextView = view.findViewById(R.id.tv_category)
        val tvMatchRate: android.widget.TextView = view.findViewById(R.id.tv_match_rate)
        val btnGoDetail: ImageButton = view.findViewById(R.id.btn_go_detail)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): JobViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_card, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]

        holder.tvTitle.text = job.title
        holder.tvLocation.text = job.location
        holder.tvCategory.text = job.category
        holder.tvMatchRate.text = "일치율 ${job.matchRate}%"

        // 카드 전체 클릭
        holder.view.setOnClickListener {
            onItemClick(job)
        }

        // 화살표 버튼 클릭
        holder.btnGoDetail.setOnClickListener {
            onItemClick(job)
        }
    }

    override fun getItemCount(): Int = jobList.size
}