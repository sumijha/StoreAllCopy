package com.viceboy.babble.ui.screens.dashboard

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.appbar.AppBarLayout
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentDashboardBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.base.EventObserver
import com.viceboy.babble.ui.custom.behavior.CustomAvatarSwipeBehavior
import com.viceboy.babble.ui.custom.widgets.CustomDashboardLinearLayoutManager
import com.viceboy.babble.ui.state.ButtonState
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.scrollToTop
import com.viceboy.data_repo.model.dataModel.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class DashboardFragment : BaseHomeFragment<DashboardViewModel, FragmentDashboardBinding>(),
    Injectable {

    private lateinit var customAvatarSwipeBehavior: CustomAvatarSwipeBehavior

    private val recyclerContext = lifecycleScope.coroutineContext + Dispatchers.Main

    private var profileViewHeight: Int = 0
    private var groupTabItemHeight: Int = 0
    private var groupTabViewRefreshJob: Job? = null
    private var memberTabViewRefreshJob: Job? = null
    private var memberTabListAdapter: DashboardMemberTabListAdapter? = null
    private var groupTabListAdapter: DashboardGroupTabListAdapter? = null
    private var fabButtonState: ButtonState? = null

    //TODO: Update while setting up toolbar
    private val toolbarMenuItemListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.profile_menu -> Toast.makeText(
                context,
                "Profile clicked",
                Toast.LENGTH_SHORT
            ).show()
            else -> Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
        }
        true
    }

    private val appBarOffsetChangeListener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout: AppBarLayout, offset: Int ->
            val maxScroll = appBarLayout.totalScrollRange.toFloat()
            val percentage = abs(offset.toFloat() / maxScroll)

            handleDependentViewsTranslation(percentage)
            handleDependentViewsVisibility(percentage)
        }

    private val dashboardTouchListener = View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN && fabButtonState == ButtonState.CLICKED) {
            viewModel.resetMainFabStateWithAnimation()
            true
        } else {
            false
        }
    }

    override val hasBottomNavigationView: Boolean = true

    override val viewModelClass: Class<DashboardViewModel> = DashboardViewModel::class.java

    override fun layoutRes(): Int = R.layout.fragment_dashboard

    override fun onCreateView() {
        initGroupTabAdapter()
        initMemberTabAdapter()
        initCustomAvatarSwipeBehavior()
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpBinding()
        setUpRecyclerView()
        setUpDashboardBarChart()
        setUpScrollToTopFabClickListener()
        setUpNestedScrollItemListener()
        attachAvatarSwipeBehavior()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(viewModel: DashboardViewModel, binding: FragmentDashboardBinding) {
        observeImageHeightLiveData()
        observeDashboardMainFabButtonStateLiveData()
        observeFabAnimTriggerLiveData()
        observeNavigationLiveData()
        observeUserLatestExpenseLiveData()
        observeDashboardBarChartData()
        observeCurrencySelectorLiveData()
        observeGroupTabLiveData()
        observeUserDetailsLiveData()
        observeGroupTabClickEventLiveData()
    }

    override fun onPause() {
        resetFabMenu()
        super.onPause()
    }

    private fun observeImageHeightLiveData() {
        viewModel.profileHeightLiveData.observe(viewLifecycleOwner, Observer {
            profileViewHeight = it
        })
    }

    private fun initGroupTabAdapter() {
        groupTabListAdapter = DashboardGroupTabListAdapter {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToGroupDetailsFragment(
                    this
                )
            )
        }
    }

    //TODO: Required when calling from tab to next screen
    private fun initMemberTabAdapter() {
        memberTabListAdapter = DashboardMemberTabListAdapter {
            //TODO: Complete callback body
        }
    }

    private fun initCustomAvatarSwipeBehavior() {
        customAvatarSwipeBehavior = CustomAvatarSwipeBehavior(requireContext())
    }

    /**
     * Attach AvatarSwipeBehavior to Profile DashboardCircleImageView
     */
    private fun attachAvatarSwipeBehavior() {
        val circleImageViewParams =
            binding.civProfilePhoto.layoutParams as CoordinatorLayout.LayoutParams
        circleImageViewParams.behavior = customAvatarSwipeBehavior
    }

    /**
     * Setting up scrollToTop mini fab button click listener
     */
    private fun setUpScrollToTopFabClickListener() {
        binding.fabScrollUp.setOnClickListener {
            if (it.visibility == View.VISIBLE) {
                binding.nsvRecyclerItemContainer.scrollToTop()
            }
        }
    }

    /**
     * Setting up Nested scroll view listener to hide or show Scroll up fab button based on recycler item scroll
     */
    private fun setUpNestedScrollItemListener() {
        binding.nsvRecyclerItemContainer.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            binding.scrollToTopFabVisibility = scrollY > groupTabItemHeight && scrollY > 0
        }
    }


    /**
     * Setting up data binding to assign all databinding layout variables
     */
    private fun setUpBinding() {
        binding.apply {
            dashViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            menuItemListener = toolbarMenuItemListener
            onAppBarOffSetChangeListener = appBarOffsetChangeListener
            dashFabMenu.dashViewModel = viewModel
            dashFabMenu.fabMenuTouchListener = dashboardTouchListener
            dashFabMenu.addExpenseNavDirections =
                DashboardFragmentDirections.actionDashboardFragmentToAddExpense()
            dashFabMenu.addGroupNavDirections =
                DashboardFragmentDirections.actionDashboardFragmentToAddGroupFragment()
            placeholderNoData.dashBoardViewModel = viewModel
            placeholderNoData.addNewGroupNavDirections =
                DashboardFragmentDirections.actionDashboardFragmentToAddGroupFragment()
        }
    }

    /**
     * Setting up Groups Tab Recycler view
     */
    private fun setUpRecyclerView() {
        binding.rvGroupList.addItemDecoration(DividerItemDecoration(requireContext(), VERTICAL))
        binding.rvGroupList.layoutManager = CustomDashboardLinearLayoutManager(requireContext()) {
            groupTabItemHeight = this
        }
    }

    /**
     * Setting up Groups Tab Recycler view adapter
     */
    private fun setUpGroupsTabAdapter() {
        if (memberTabViewRefreshJob?.isActive == true) memberTabViewRefreshJob?.cancel()

        groupTabViewRefreshJob = viewLifecycleOwner.lifecycleScope.launch(recyclerContext) {
            binding.rvGroupList.recycledViewPool.clear()
            binding.rvGroupList.adapter?.notifyDataSetChanged()
            binding.rvGroupList.adapter = groupTabListAdapter
        }
    }

    /**
     * Setting up Members Tab Recycler view adapter
     */
    private fun setUpMembersTabAdapter() {
        if (groupTabViewRefreshJob?.isActive == true) groupTabViewRefreshJob?.cancel()

        memberTabViewRefreshJob = viewLifecycleOwner.lifecycleScope.launch(recyclerContext) {
            binding.rvGroupList.recycledViewPool.clear()
            binding.rvGroupList.adapter?.notifyDataSetChanged()
            binding.rvGroupList.adapter = memberTabListAdapter
        }
    }

    /**
     * Reset Main Fab Button state with reverse animation
     */
    private fun resetFabMenu() {
        if (isFabMenuExpanded()) viewModel.resetMainFabStateWithAnimation()
    }

    /**
     * Start observing User latest expense LiveData
     */
    private fun observeUserLatestExpenseLiveData() {
        viewModel.latestExpenseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> it.data?.let { dashboardExpense ->
                    binding.userLatestExpense = dashboardExpense
                }
                is Resource.Failure -> if (!it.message.isNullOrEmpty())
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG)
                        .show()
            }
        })
    }

    /**
     * Start observing Group Tab LiveData
     */
    private fun observeGroupTabLiveData() {
        viewModel.groupTabResourceLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    binding.isDataLoading = true
                    binding.showDashboard = false
                    bottomNavigationView.visibility = View.GONE
                }
                is Resource.Failure -> {
                    binding.isDataLoading = false
                    if (it.message.isNullOrEmpty()) {
                        binding.showDashboard = false
                        bottomNavigationView.visibility = View.GONE
                    } else {
                        binding.showDashboard = true
                        bottomNavigationView.visibility = View.VISIBLE
                    }
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Success -> {
                    binding.isDataLoading = false
                    binding.showDashboard = true
                    bottomNavigationView.visibility = View.VISIBLE
                    viewModel.onSuccessfulGroupLoad()
                    groupTabListAdapter?.submitList(it.data?.toMutableList())
                }
            }
        })
    }

    /**
     * Start observing currencySelectorLiveData
     */
    private fun observeCurrencySelectorLiveData() {
        viewModel.currencySelectorLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    binding.isExpenseFoundInCurrentYr = true
                    binding.tvBarCurrency.text = it.data
                }
                is Resource.Failure -> {
                    binding.isExpenseFoundInCurrentYr = false
                }
            }
        })
    }

    /**
     * Start observing userExpenseListForCurrentYrLiveData
     */
    private fun observeDashboardBarChartData() {
        viewModel.userExpenseListForCurrentYrLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> handleUserExpenseListForCurrentYr(it.data)
                is Resource.Failure -> {
                    if (it.message?.isNotEmpty() == true) Toast.makeText(
                        requireContext(),
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    /**
     * Start observing Group Tab Click Event LiveData
     */
    private fun observeGroupTabClickEventLiveData() {
        viewModel.onGroupTabClickedLiveData.observe(viewLifecycleOwner, Observer {
            if (it) setUpGroupsTabAdapter() else {
                setUpMembersTabAdapter()
                observeMemberTabClickEventLiveData()
            }
        })
    }

    /**
     * Start observing Member Tab Click Event LiveData
     */
    private fun observeMemberTabClickEventLiveData() {
        if (viewModel.memberTabResourceLiveData.hasObservers())
            viewModel.memberTabResourceLiveData.removeObservers(viewLifecycleOwner)
        viewModel.memberTabResourceLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> binding.isDataLoading = true
                is Resource.Failure -> binding.isDataLoading = false
                is Resource.Success -> {
                    binding.isDataLoading = false
                    memberTabListAdapter?.submitList(it.data?.toMutableList())
                }
            }
        })
    }

    /**
     * Start Observing user Details LiveData
     */
    private fun observeUserDetailsLiveData() {
        viewModel.userDetailsLiveData.observe(viewLifecycleOwner, Observer {
            binding.user = it
        })
    }

    /**
     * Start observing Navigation LiveData
     */
    private fun observeNavigationLiveData() {
        viewModel.navigateLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(it)
        })
    }

    /**
     * Start observing LiveData which triggers Fab button animation
     */
    private fun observeFabAnimTriggerLiveData() {
        viewModel.animFabLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.startAnimationFlag = it
        })
    }

    /**
     * Start observing Dashboard Main Fab Button state
     */
    private fun observeDashboardMainFabButtonStateLiveData() {
        viewModel.mainFabButtonStateLiveData.observe(viewLifecycleOwner, Observer {
            binding.fabButtonState = it
            fabButtonState = it
            if (isFabMenuExpanded())
                binding.dashFabMenu.rootFabOptions.background =
                    ContextCompat.getDrawable(requireContext(), R.color.color_bg_progress)
            else
                binding.dashFabMenu.rootFabOptions.background =
                    ContextCompat.getDrawable(requireContext(), android.R.color.transparent)

        })
    }

    /**
     * Convert expense data list to data set required by BarChart
     */
    private fun handleUserExpenseListForCurrentYr(data: List<Expense>?) {
        data?.let {
            val dataMap = convertExpenseListToMonthlyExpenseMap(it)
            addBarDataSet(dataMap)
        }
    }


    /**
     * Handles BottomNavigation and DashFabMenu translation based on AppBarLayout scroll
     */
    private fun handleDependentViewsTranslation(percentage: Float) {
        bottomNavigationView.translationY = bottomNavigationView.height.toFloat() * percentage
        binding.dashFabMenu.rootFabOptions.translationY =
            bottomNavigationView.height.toFloat() * percentage
    }

    /**
     * Handles Dependent views visibility on appBarLayout scroll
     */
    private fun handleDependentViewsVisibility(percentage: Float) {
        if (percentage > MIN_TOOLBAR_MENU_VISIBILITY) {
            binding.toolbarDashboard.menu.setGroupVisible(R.id.dashboard_menu, true)
            binding.toolbarDashboard.invalidate()
        } else {
            binding.toolbarDashboard.menu.setGroupVisible(R.id.dashboard_menu, false)
            binding.toolbarDashboard.invalidate()
        }

        binding.tvToolbarUsername.alpha = percentage
        binding.dashFabMenu.fabShowOptions.isEnabled = percentage == 0f
        binding.dashFabMenu.rootFabOptions.alpha = 1 - percentage
    }

    /**
     * Check if menu options are expanded
     */
    private fun isFabMenuExpanded() = fabButtonState == ButtonState.CLICKED

    /**
     * Setting up Dashboard BarChart binding
     */
    private fun setUpDashboardBarChart() {
        val noExpenseDesc = resources.getString(R.string.no_expense_found_in_current_yr)
        binding.barChart.apply {
            val paint = getPaint(Chart.PAINT_INFO)
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.color = requireContext().getColor(R.color.colorPrimary)

            setNoDataText(noExpenseDesc)
            setMaxVisibleValueCount(60)
            setDrawBarShadow(false)
            setDrawGridBackground(false);
            setPinchZoom(false)
            setScaleEnabled(false)
            setDescription(null)

            val xAxis: XAxis = this.xAxis
            xAxis.position = XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)

            axisLeft.setDrawGridLines(false)

            animateXY(1000, 1500)
        }
    }

    /**
     * Convert expense list to map of expense with key as month and value as expense amount
     */
    private fun convertExpenseListToMonthlyExpenseMap(listOfExpense: List<Expense>): MutableMap<Int, Float> {
        val mapOfExpenseByMonth = mutableMapOf<Int, Float>()
        listOfExpense.forEach { expense ->
            val month = Calendar.getInstance().run {
                timeInMillis = expense.expenseDate
                this.get(Calendar.MONTH)
            }
            if (mapOfExpenseByMonth.containsKey(month)) {
                val newAmount = mapOfExpenseByMonth[month]?.plus(expense.amountPaid)
                mapOfExpenseByMonth[month] = newAmount ?: expense.amountPaid
            } else {
                mapOfExpenseByMonth[month] = expense.amountPaid
            }
        }
        return mapOfExpenseByMonth
    }


    /**
     * Add monthly expense data map to BarDataSet and notifies the BarChart
     */
    private fun addBarDataSet(listOfMonthlyExpenseMap: MutableMap<Int, Float>) {
        val barChartDesc = resources.getString(R.string.bar_chart_desc)
        val listOfBarEntry = mutableListOf<BarEntry>()
        repeat(12) {
            listOfBarEntry.add(BarEntry(listOfMonthlyExpenseMap[it] ?: 0f, it))
        }
        val barDataSet = BarDataSet(listOfBarEntry, barChartDesc)
        val barData = BarData(listOfMonthsInShortFormat, barDataSet)

        barDataSet.setColors(CUSTOM_LIBERTY_COLORS)
        binding.barChart.apply {
            data = barData
            notifyDataSetChanged()
            invalidate()
        }
    }


    companion object {
        private const val MIN_TOOLBAR_MENU_VISIBILITY = 0.9f

        private val listOfMonthsInShortFormat = mutableListOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        private val CUSTOM_LIBERTY_COLORS = intArrayOf(
            Color.rgb(207, 248, 100),
            Color.rgb(148, 212, 100),
            Color.rgb(136, 180, 100),
            Color.rgb(118, 174, 100),
            Color.rgb(42, 109, 100)
        )
    }
}

