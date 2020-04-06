package com.viceboy.babble.ui.screens.addExpense

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemNewExpenseAmountPaidForListBinding
import com.viceboy.babble.databinding.ItemNewExpenseAmountPaidForSumBinding
import com.viceboy.babble.ui.base.DataBoundViewHolder
import com.viceboy.babble.ui.util.Constants.KEY_EQUAL
import com.viceboy.babble.ui.util.Constants.KEY_UNEQUAL
import com.viceboy.babble.ui.util.toEditable
import com.viceboy.data_repo.model.dataModel.ForExpenseShared
import com.viceboy.data_repo.model.dataModel.TotalExpenseShared
import com.viceboy.data_repo.model.dataModel.User

class ExpensePaidForListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val addExpenseViewModel: AddExpenseViewModel,
    private var listOfParticipantShare: ArrayList<Float>
) :
    BaseDataBoundAdapter<ForExpenseShared>(
        diffCallBack = object : DiffUtil.ItemCallback<ForExpenseShared>() {
            override fun areItemsTheSame(
                oldItem: ForExpenseShared,
                newItem: ForExpenseShared
            ): Boolean = false

            override fun areContentsTheSame(
                oldItem: ForExpenseShared,
                newItem: ForExpenseShared
            ): Boolean = false
        }
    ) {

    private var listOfUncheckedFields = ArrayList<Int>()

    private var mRecyclerView: RecyclerView? = null
    private var mHasPreviousEditTextFocus = false
    private var totalAmount = 0f
    private var noOfParticipants = 0

    var splitBy: String = ""

    override fun inflateBinding(viewGroup: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        observeLiveDataForInitialViewSetup()
        return when (viewType) {
            TYPE_PARTICIPANT -> createParticipantViewHolder(viewGroup)
            else -> createTotalExpenseViewHolder(viewGroup)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<*>,
        position: Int
    ) {
        when (holder) {
            is ParticipantViewHolder -> {
                setParticipantShareText(holder, position)
                setUpViewBasedOnSplitBy(holder)
                initCheckBoxListener(holder)
                initTextFocusChangeListener(holder)
            }

            is TotalAmountViewHolder -> {
                val total = listOfParticipantShare.sum()
                holder.binding.cumulativeAmount = total.toString()
            }
        }
        super.onBindViewHolder(holder, position)
    }

    override fun <V : ViewDataBinding> bind(binding: V, item: ForExpenseShared) {
        when (binding) {
            is ItemNewExpenseAmountPaidForListBinding -> binding.user = item as User
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TotalExpenseShared -> TYPE_TOTAL
            else -> TYPE_PARTICIPANT
        }
    }

    // Start Observing LiveData required for Initial Adapter Setup
    private fun observeLiveDataForInitialViewSetup() {
        addExpenseViewModel.participantsCountLiveData.observe(viewLifecycleOwner, Observer {
            noOfParticipants = it ?: 0
        })

        addExpenseViewModel.mutableAmountPaidLiveData.observe(viewLifecycleOwner, Observer {
            totalAmount = if (it.isNullOrEmpty()) 0f else it.toFloat()
        })

        addExpenseViewModel.participantsShareLiveData.observe(viewLifecycleOwner, Observer {
            listOfParticipantShare = it
        })
    }

    // Create a Participant View Holder to set up contributors list
    private fun createParticipantViewHolder(viewGroup: ViewGroup): ParticipantViewHolder {
        val participantBinding = DataBindingUtil.inflate<ItemNewExpenseAmountPaidForListBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_new_expense_amount_paid_for_list,
            viewGroup,
            false
        )
        return ParticipantViewHolder(participantBinding)
    }

    // Create a TotalExpense View Holder to set up row for cumulative expense
    private fun createTotalExpenseViewHolder(viewGroup: ViewGroup): TotalAmountViewHolder {
        val totalBinding = DataBindingUtil.inflate<ItemNewExpenseAmountPaidForSumBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_new_expense_amount_paid_for_sum,
            viewGroup,
            false
        )
        return TotalAmountViewHolder(totalBinding)
    }

    // Setup checkbox listener and handle the appropriate action as per the split by option selected
    private fun initCheckBoxListener(holder: DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>) {
        holder.binding.cbAmountPaidForSelectParticipant.setOnCheckedChangeListener { _, isChecked ->
            onCheckboxUpdate(isChecked, holder.adapterPosition)
            handleSplit(holder, isChecked)
        }
    }

    // Setup text change listener for unequal split and update the cumulative amount respectively
    private fun initTextFocusChangeListener(holder: ParticipantViewHolder) {
        holder.binding.etAmountPaidForParticipantShare.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = handleTextChange(holder, s)

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mHasPreviousEditTextFocus)
                    notifyItemChanged(itemCount - 1)
            }
        })

        holder.binding.etAmountPaidForParticipantShare.setOnFocusChangeListener { _, hasFocus ->
            mHasPreviousEditTextFocus = !(!hasFocus && mHasPreviousEditTextFocus)
        }
    }

    // Update List of unchecked fields and participants count after checkbox is checked or unchecked
    private fun onCheckboxUpdate(isChecked: Boolean, position: Int) {
        if (!isChecked) {
            noOfParticipants--
            if (!listOfUncheckedFields.contains(position))
                listOfUncheckedFields.add(position)
        } else {
            noOfParticipants++
            if (listOfUncheckedFields.contains(position))
                listOfUncheckedFields.remove(position)
        }

        addExpenseViewModel.setParticipantsCount(noOfParticipants)
        addExpenseViewModel.setMutableListOfUncheckedFields(listOfUncheckedFields)
    }

    // Handle amount split equally/unequally as per the checkbox update
    private fun handleSplit(
        holder: DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>,
        isChecked: Boolean
    ) {
        when (splitBy) {
            KEY_EQUAL -> handleSplitEqually(holder, isChecked)
            KEY_UNEQUAL -> handleSplitUnequally(holder, isChecked)
        }
    }

    // Handle split amount equally and update the list of participants share
    private fun handleSplitEqually(
        holder: DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>,
        isChecked: Boolean
    ) {
        if (isChecked) {
            if (noOfParticipants < itemCount)
                resetSplitAmount(holder.adapterPosition, isChecked)
        } else {
            resetSplitAmount(holder.adapterPosition, isChecked)
        }
    }

    // Handle split amount unequally and update the list of participants share
    private fun handleSplitUnequally(
        holder: DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>,
        isChecked: Boolean
    ) {
        val etAmountPerParticipant = holder.binding.etAmountPaidForParticipantShare
        if (!isChecked) {
            listOfParticipantShare[holder.adapterPosition] = 0f
            etAmountPerParticipant.clearFocus()
            etAmountPerParticipant.isEnabled = false
            addExpenseViewModel.setParticipantsShareList(listOfParticipantShare)
            addExpenseViewModel.setMutableRequestFocus(true)
            if (mRecyclerView?.isComputingLayout == false) notifyDataSetChanged()
        } else {
            if (!etAmountPerParticipant.isEnabled)
                etAmountPerParticipant.isEnabled = true
        }
    }

    // Handle text change for unequal split and update the list of participant share
    private fun handleTextChange(
        holder: ParticipantViewHolder,
        s: Editable?
    ) {
        val resetValue = 0f
        if (splitBy == KEY_UNEQUAL) {
            listOfParticipantShare[holder.adapterPosition] = if (s.isNullOrEmpty())
                resetValue
            else
                try {
                    s.toString().toFloat()
                } catch (nfe: NumberFormatException) {
                    resetValue
                }
            addExpenseViewModel.setParticipantsShareList(listOfParticipantShare)
        }
    }

    // Reset the total amount among the checked no of participants for equal split
    private fun resetSplitAmount(position: Int, checked: Boolean) {
        val perShare: Float
        if (checked) {
            perShare = totalAmount / noOfParticipants
            listOfParticipantShare[position] = perShare
        } else {
            listOfParticipantShare[position] = 0f
            perShare = totalAmount / noOfParticipants
        }

        listOfParticipantShare.forEachIndexed { index, fl ->
            if (fl != 0f) listOfParticipantShare[index] = perShare
        }

        notifyObservers(listOfParticipantShare)
    }

    // Set Expense participant share text to user contribution field view
    private fun setParticipantShareText(
        holder: DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>,
        position: Int
    ) {
        holder.binding.etAmountPaidForParticipantShare.text =
            listOfParticipantShare[position].toString().toEditable()
    }

    // Set up checkbox and contribution field views based on split by
    private fun setUpViewBasedOnSplitBy(holder: DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>) {
        val etParticipant = holder.binding.etAmountPaidForParticipantShare
        val cbParticipant = holder.binding.cbAmountPaidForSelectParticipant

        cbParticipant.isChecked = !listOfUncheckedFields.contains(holder.adapterPosition)

        if (splitBy == KEY_EQUAL) {
            etParticipant.isEnabled = false
        } else {
            etParticipant.clearFocus()
            if (!etParticipant.isEnabled && cbParticipant.isChecked)
                etParticipant.isEnabled = true
        }
    }

    // Update the participant share list and reload the list
    private fun notifyObservers(listOfParticipantShare: ArrayList<Float>) {
        addExpenseViewModel.setParticipantsShareList(listOfParticipantShare)
        if (mRecyclerView?.isComputingLayout == false) notifyDataSetChanged()
    }

    // Set list of unchecked participant list from view model if not empty
    fun setListOfUncheckedParticipants(list : ArrayList<Int>) {
        listOfUncheckedFields = list
    }

    companion object {
        private const val TYPE_PARTICIPANT = 1
        private const val TYPE_TOTAL = 2
    }

    /**
     * Total Amount ViewHolder in order to display TotalAmount
     */
    class TotalAmountViewHolder(binding: ItemNewExpenseAmountPaidForSumBinding) :
        DataBoundViewHolder<ItemNewExpenseAmountPaidForSumBinding>(binding) {
    }

    /**
     * Participants ViewHolder in order to display Participants List
     */
    class ParticipantViewHolder(binding: ItemNewExpenseAmountPaidForListBinding) :
        DataBoundViewHolder<ItemNewExpenseAmountPaidForListBinding>(binding) {
    }
}