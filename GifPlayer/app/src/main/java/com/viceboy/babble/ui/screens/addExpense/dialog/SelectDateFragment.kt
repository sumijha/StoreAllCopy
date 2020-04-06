package com.viceboy.babble.ui.screens.addExpense.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.screens.addExpense.AddExpenseViewModel
import com.viceboy.babble.ui.util.toDate
import java.util.*
import javax.inject.Inject

class SelectDateFragment : DialogFragment(), DatePickerDialog.OnDateSetListener, Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AddExpenseViewModel
    private lateinit var selectedDate: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        initViewModel()
        return getDatePickerDialogWithMaxLimit()
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(AddExpenseViewModel::class.java)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        setSelectedDate(year, month + 1, dayOfMonth)
    }

    private fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        val date = "$dayOfMonth/$month/$year".toDate("dd/MM/yyyy")
        selectedDate = DateFormat.format("dd/MMM/yyyy", date).toString()
        viewModel.setMutableDate(selectedDate)
    }

    private fun getDatePickerDialogWithMaxLimit(): DatePickerDialog {
        Calendar.getInstance().apply {
            val yy = get(Calendar.YEAR)
            val mm = get(Calendar.MONTH)
            val dd = get(Calendar.DAY_OF_MONTH)
            val dialog = DatePickerDialog(requireActivity(), this@SelectDateFragment, yy, mm, dd)
            dialog.datePicker.maxDate = System.currentTimeMillis()
            return dialog
        }
    }

}