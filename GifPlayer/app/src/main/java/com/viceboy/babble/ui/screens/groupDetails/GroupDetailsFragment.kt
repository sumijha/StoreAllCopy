package com.viceboy.babble.ui.screens.groupDetails

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.google.android.material.tabs.TabLayoutMediator
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentGroupDetailsBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.screens.groupDetails.dialog.GroupSettlementBottomSheetFragment
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.statusBarHeight
import com.viceboy.babble.ui.util.toEditable
import com.viceboy.babble.ui.util.toggleVisibility


class GroupDetailsFragment :
    BaseHomeFragment<GroupDetailsViewModel, FragmentGroupDetailsBinding>(), Injectable {

    private lateinit var pieMenuItem: MenuItem
    private lateinit var groupTabAdapter: GroupViewPagerAdapter


    private val emptyString = ""
    private val listOfTabTitles = listOf("Members", "Expenses", "Transactions")

    private val navArgs by navArgs<GroupDetailsFragmentArgs>()

    //TODO: Update while setting up toolbar
    private val toolbarMenuItemListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.pieVisibility -> {
                binding.containerGraph.toggleVisibility() {
                    updateMenuIcon(this)
                    updateViewpagerHeight(this)
                }
            }
            R.id.settle -> {
                GroupSettlementBottomSheetFragment.newInstance()
                    .show(requireFragmentManager(), "Settlement")
            }
            else -> Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
        }
        true
    }

    override fun layoutRes(): Int = R.layout.fragment_group_details

    override fun onCreateView() = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initMenuItem()
        initSelectedGroupValue()
        initGroupDetailsTabAdapter()
        setUpBinding()
        setUpViewPager()
        setUpPieChart()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(
        viewModel: GroupDetailsViewModel,
        binding: FragmentGroupDetailsBinding
    ) {
        viewModel.groupLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.initDataLoad(it)
        })

        viewModel.groupExpenseLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    val listOfGroupMembers = it.data
                    val mapOfTotalExpenseByUser = mutableMapOf<String, Float>()
                    listOfGroupMembers?.forEach { groupExpense ->
                        val amount =
                            listOfGroupMembers.filter { it.expenseOwner == groupExpense.expenseOwner }
                                .sumByDouble {
                                    it.itemAmount.toDouble()
                                }
                        mapOfTotalExpenseByUser[groupExpense.expenseOwner] = amount.toFloat()
                    }
                    if (mapOfTotalExpenseByUser.isNotEmpty()) addDataSet(
                        mapOfTotalExpenseByUser,
                        listOfGroupMembers?.first()?.currencySymbol ?: emptyString
                    )
                }
            }
        })
    }

    override val viewModelClass: Class<GroupDetailsViewModel> = GroupDetailsViewModel::class.java
    override val hasBottomNavigationView: Boolean = false

    private fun setUpBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            menuListener = toolbarMenuItemListener
        }
    }

    private fun initMenuItem() {
        pieMenuItem = binding.toolbarGroupDetails.menu.findItem(R.id.pieVisibility)
    }

    private fun initSelectedGroupValue() {
        arguments?.let {
            val group = navArgs.group
            viewModel.setSelectedGroup(group)
            binding.tvGroupDes.text = group.groupDesc
            binding.tvGroupName.text = group.groupName.toEditable()
        }
    }

    private fun initGroupDetailsTabAdapter() {
        groupTabAdapter = GroupViewPagerAdapter(requireActivity())
    }

    private fun updateMenuIcon(visibility: Int) {
        when (visibility) {
            View.VISIBLE -> pieMenuItem.setIcon(R.drawable.ic_pie_chart_outlined_white)
            View.GONE -> pieMenuItem.setIcon(R.drawable.ic_pie_chart)
        }
    }


    private fun setUpViewPager() {
        binding.groupTabsPager.adapter = groupTabAdapter

        TabLayoutMediator(binding.groupDetailsTabLayout, binding.groupTabsPager) { tab, position ->
            tab.text = listOfTabTitles[position]
        }.attach()

        binding.groupTabsPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.groupTabsPager.requestLayout()
                viewModel.setPageIndex(position)
                super.onPageSelected(position)
            }
        })

        binding.groupTabsPager.post {
            binding.groupTabsPager.setCurrentItem(1, false)
        }
    }

    private fun addDataSet(mapOfExpenseByUser: MutableMap<String, Float>, currency: String) {
        var totalGroupAmount: Float = 0f
        val textDesc = resources.getString(R.string.amount_paid_in_each_group)
        val listOfEntry = mapOfExpenseByUser.keys.mapIndexed { index, key ->
            val amount = mapOfExpenseByUser[key]
            totalGroupAmount += amount ?: 0f
            Entry(amount ?: 0f, index)
        }
        val dataSet = PieDataSet(listOfEntry, textDesc)
        dataSet.apply {
            setColors(CUSTOM_PIE_CHART_COLOR)
            sliceSpace = 4f
            selectionShift = 5f
        }
        val groupName = mapOfExpenseByUser.keys.map { it.split(" ").first() }.toList()
        val data = PieData(groupName, dataSet)
        data.apply {
            setValueTextSize(12f)
            setValueTypeface(Typeface.DEFAULT_BOLD)
            setValueTypeface(Typeface.SANS_SERIF)
            setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
        binding.expensePieChart.apply {
            this.centerText = currency + totalGroupAmount.toString()
            this.data = data
            invalidate()
        }
    }

    private fun setUpPieChart() {
        binding.expensePieChart.apply {
            setDescription(null)
            setUsePercentValues(false)
            setExtraOffsets(5f, 10f, 5f, 10f)
            setHoleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 64f
            transparentCircleRadius = 64f;
            setCenterTextSize(20f)
            setCenterTextColor(Color.rgb(80, 150, 80))
            setDrawCenterText(true)
            isDrawHoleEnabled = true

            legend.isEnabled = false
        }
        binding.expensePieChart.animateXY(1000, 1000)
    }

    private fun updateViewpagerHeight(visibility: Int) {
        val layoutParam = if (visibility == View.VISIBLE)
            getViewPagerLayoutParam(ViewPagerHeight.NOT_FULLSCREEN)
        else
            getViewPagerLayoutParam(ViewPagerHeight.FULLSCREEN)
        binding.groupTabsPager.layoutParams = layoutParam
    }

    private fun getViewPagerLayoutParam(viewPagerHeight: ViewPagerHeight): ViewGroup.LayoutParams? {
        return when (viewPagerHeight) {
            ViewPagerHeight.FULLSCREEN -> getLayoutParamInFullScreen()
            ViewPagerHeight.NOT_FULLSCREEN -> getLayoutParamInNonFullScreen()
        }
    }

    private fun getLayoutParamInFullScreen(): ViewGroup.LayoutParams? {
        val layoutParam = binding.groupTabsPager.layoutParams
        val displayMetrics = DisplayMetrics()
        val otherCompHeight =
            binding.toolbarGroupDetails.height.plus(binding.groupDetailsTabLayout.height)
                .plus(requireContext().statusBarHeight())

        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels.minus(otherCompHeight)
        val width = displayMetrics.widthPixels
        layoutParam.height = height
        layoutParam.width = width
        return layoutParam
    }

    private fun getLayoutParamInNonFullScreen(): ViewGroup.LayoutParams? {
        val layoutParam = binding.groupTabsPager.layoutParams
        layoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParam.height = ViewGroup.LayoutParams.MATCH_PARENT
        return layoutParam
    }

    companion object {
        private val CUSTOM_PIE_CHART_COLOR = intArrayOf(
            Color.rgb(150, 220, 120),
            Color.rgb(130, 212, 110),
            Color.rgb(145, 200, 100),
            Color.rgb(140, 174, 100),
            Color.rgb(146, 179, 100)
        )
    }

    enum class ViewPagerHeight {
        FULLSCREEN, NOT_FULLSCREEN
    }
}

