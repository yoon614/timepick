package com.example.timepick

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

/**
 JobListActivity - 모집 중인 공고 리스트 화면

 플로우:
  - ViewModel의 matchedJobs StateFlow 관찰
  - 타임픽에서 시간 선택 O: 80% 이상 일치하는 공고 표시 (일치율 표시)
  - 타임픽에서 시간 선택 X: 전체 공고 표시 (일치율 숨김)
  - 공고 카드 클릭 -> JobDetailActivity(공고 상세)로 이동
  - 공고 없으면 Empty View 표시
 */
class JobListActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rvJobList: RecyclerView
    private lateinit var layoutEmptyView: LinearLayout

    private lateinit var viewModel: MainViewModel


    private lateinit var jobListAdapter: JobListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_list)

        android.util.Log.d("JobListActivity", "=== onCreate 시작 ===")


        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        // Intent에서 시간 선택 여부 확인
        val timeSelected = intent.getBooleanExtra("TIME_SELECTED", false)
        android.util.Log.d("JobListActivity", "시간 선택 여부: $timeSelected")

        // View 초기화
        initViews()

        // RecyclerView 설정
        setupRecyclerView()

        // 클릭 리스너 설정
        setupClickListeners()

        // ViewModel 관찰 시작
        observeViewModel()

        // onCreate에서 직접 매칭 실행
        lifecycleScope.launch {
            if (timeSelected) {
                // 시간 선택한 경우: DB에서 사용자 시간 로드 후 매칭
                val sharedPref = getSharedPreferences("TimePick", MODE_PRIVATE)
                val userId = sharedPref.getString("USER_ID", "0")?.toIntOrNull() ?: 0

                android.util.Log.d("JobListActivity", "사용자 시간 로드 (userId: $userId)")

                viewModel.loadUserTimes(userId) { savedIndices ->
                    android.util.Log.d("JobListActivity", "로드된 시간 개수: ${savedIndices.size}")
                    viewModel.findMatchingJobs(savedIndices)
                }
            } else {
                // 시간 선택 안 한 경우: 전체 공고 표시
                android.util.Log.d("JobListActivity", "전체 공고 표시 (빈 리스트로 매칭)")
                viewModel.findMatchingJobs(emptyList())
            }

            // StateFlow 업데이트 대기
            kotlinx.coroutines.delay(300)

            val currentJobs = viewModel.matchedJobs.value
            android.util.Log.d("JobListActivity", "최종 공고 개수: ${currentJobs.size}")
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_detail_back)
        rvJobList = findViewById(R.id.rv_job_list)
        layoutEmptyView = findViewById(R.id.layout_empty_view)
    }

    /**
     RecyclerView 설정
     */
    private fun setupRecyclerView() {
        rvJobList.layoutManager = LinearLayoutManager(this)

        jobListAdapter = JobListAdapter(emptyList()) { jobMatchResult ->
            // 공고 카드 클릭 시 상세 화면으로 이동
            val intent = Intent(this, JobDetailActivity::class.java)
            intent.putExtra("JOB_ID", jobMatchResult.job.jobId)
            intent.putExtra("JOB_TITLE", jobMatchResult.job.title)
            intent.putExtra("JOB_LOCATION", jobMatchResult.job.location)
            intent.putExtra("JOB_ADDRESS", jobMatchResult.job.address)
            intent.putExtra("JOB_CATEGORY", jobMatchResult.job.category)
            intent.putExtra("JOB_MATCH_RATE", jobMatchResult.matchRate)
            startActivity(intent)
        }

        rvJobList.adapter = jobListAdapter
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.matchedJobs.collect { matchedJobs ->
                android.util.Log.d("JobListActivity", "=== StateFlow 업데이트 ===")
                android.util.Log.d("JobListActivity", "매칭된 공고 개수: ${matchedJobs.size}")

                matchedJobs.forEachIndexed { index, job ->
                    android.util.Log.d("JobListActivity",
                        "$index: ${job.job.title} (일치율: ${job.matchRate}%)")
                }

                if (matchedJobs.isEmpty()) {
                    // 공고 없음
                    android.util.Log.d("JobListActivity", "공고 없음 - Empty View 표시")
                    rvJobList.visibility = View.GONE
                    layoutEmptyView.visibility = View.VISIBLE
                } else {
                    // 공고 있음
                    android.util.Log.d("JobListActivity", "공고 있음 - RecyclerView 표시")
                    rvJobList.visibility = View.VISIBLE
                    layoutEmptyView.visibility = View.GONE
                    jobListAdapter.updateJobs(matchedJobs)
                }
            }
        }
    }

    private fun setupClickListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener {
            finish()
        }
    }
}

/**
 JobListAdapter - 공고 리스트 어댑터
 */
class JobListAdapter(
    private var jobList: List<MainViewModel.JobMatchResult>,
    private val onItemClick: (MainViewModel.JobMatchResult) -> Unit
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
        val jobMatchResult = jobList[position]
        val job = jobMatchResult.job

        holder.tvTitle.text = job.title
        holder.tvLocation.text = job.location
        holder.tvCategory.text = job.category

        // 일치율 표시 (-1이면 시간 선택 안 함 -> 숨김)
        if (jobMatchResult.matchRate == -1) {
            holder.tvMatchRate.visibility = View.GONE
        } else {
            holder.tvMatchRate.visibility = View.VISIBLE
            holder.tvMatchRate.text = "일치율 ${jobMatchResult.matchRate}%"
        }

        // 카드 전체 클릭
        holder.view.setOnClickListener {
            onItemClick(jobMatchResult)
        }

        // 화살표 버튼 클릭
        holder.btnGoDetail.setOnClickListener {
            onItemClick(jobMatchResult)
        }
    }

    override fun getItemCount(): Int = jobList.size

    /**
     공고 리스트 업데이트
     */
    fun updateJobs(newJobs: List<MainViewModel.JobMatchResult>) {
        jobList = newJobs
        notifyDataSetChanged()
    }
}