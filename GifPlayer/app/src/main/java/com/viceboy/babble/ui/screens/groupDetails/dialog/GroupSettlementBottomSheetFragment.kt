package com.viceboy.babble.ui.screens.groupDetails.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.viceboy.babble.R
import com.viceboy.babble.databinding.BottomsheetGroupSettlementListBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.screens.groupDetails.GroupDetailsViewModel
import com.viceboy.babble.ui.state.Resource
import javax.inject.Inject

class GroupSettlementBottomSheetFragment : BottomSheetDialogFragment(),Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var groupSettlementListAdapter: GroupSettlementListAdapter
    private lateinit var viewModel: GroupDetailsViewModel
    private lateinit var binding: BottomsheetGroupSettlementListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottomsheet_group_settlement_list,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvSettlementPair.adapter = groupSettlementListAdapter
        observeSettlementPairLiveData()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL,R.style.SheetDialog)
        initViewModel()
        initAdapter()
        return super.onCreateDialog(savedInstanceState)
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(GroupDetailsViewModel::class.java)
    }

    private fun initAdapter() {
        groupSettlementListAdapter = GroupSettlementListAdapter(requireFragmentManager()){
            dismiss()
            viewModel.setSelectedSettlementPair(this)
        }
    }

    private fun observeSettlementPairLiveData() {
        if (viewModel.listOfSettlementPairLiveData.hasObservers()) viewModel.listOfSettlementPairLiveData.removeObservers(
            viewLifecycleOwner
        )
        viewModel.listOfSettlementPairLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    groupSettlementListAdapter.submitList(it.data)
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Error occured", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    companion object {
        fun newInstance() =
            GroupSettlementBottomSheetFragment()
    }
}