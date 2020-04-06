package com.viceboy.babble.ui.screens.groupDetails.dialog

import android.app.Dialog
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.lifecycle.Observer
import com.viceboy.babble.R
import com.viceboy.babble.databinding.DialogPaymentConfirmationBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseBottomSheetFragment
import com.viceboy.babble.ui.screens.groupDetails.GroupDetailsViewModel
import com.viceboy.data_repo.model.uiModel.SettlementPair

class GroupSettlementConfirmBottomSheetFragment :
    BaseBottomSheetFragment<DialogPaymentConfirmationBinding, GroupDetailsViewModel>(), Injectable {

    override val layoutRes: Int = R.layout.dialog_payment_confirmation
    override val viewModelClass: Class<GroupDetailsViewModel> = GroupDetailsViewModel::class.java

    override fun onCreateView(binding: DialogPaymentConfirmationBinding) {
        viewModel.settlementPairLiveData.observe(viewLifecycleOwner, Observer {
            binding.tvMsgConfirmation.text = toConfirmationText(it)
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.SheetDialog)
        return super.onCreateDialog(savedInstanceState)
    }

    private fun toConfirmationText(settlementPair: SettlementPair): SpannableStringBuilder {
        var reqText = resources.getString(R.string.payment_confirmation)
        reqText = reqText.replace("FROM_USER", settlementPair.fromUser)
            .replace("TO_USER", settlementPair.toUser)
            .replace("AMOUNT", settlementPair.currency+settlementPair.amount.toString())
        return SpannableStringBuilder(reqText).apply {
            setSpan(
                StyleSpan(BOLD),
                reqText.indexOf(settlementPair.amount.toString()).minus(1),
                reqText.indexOf("from"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

            setSpan(
                StyleSpan(BOLD),
                reqText.indexOf(settlementPair.fromUser),
                reqText.indexOf("to"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

            setSpan(
                StyleSpan(BOLD),
                reqText.indexOf(settlementPair.toUser),
                reqText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    companion object {
        fun newInstance() = GroupSettlementConfirmBottomSheetFragment()
    }

}