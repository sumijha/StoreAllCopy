package com.viceboy.babble.ui.screens.addExpense

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentAddExpenseBinding
import com.viceboy.babble.databinding.LayoutSplitAmountBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.custom.widgets.CustomExpandableRecyclerView
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.*
import com.viceboy.data_repo.model.dataModel.ForExpenseShared
import com.viceboy.data_repo.model.dataModel.Groups
import com.viceboy.data_repo.model.dataModel.TotalExpenseShared

class AddExpenseFragment : BaseHomeFragment<AddExpenseViewModel, FragmentAddExpenseBinding>(),
    Injectable {

    private var listOfUncheckedFields = ArrayList<Int>()
    private var listOfParticipantsShare = ArrayList<Float>()
    private var amountPaid = 0f

    private var currentNoOfParticipants: Int? = null
    private var selectedGroup: Groups? = null
    private var argimageUri: Uri? = null
    private var popUpWindow: PopupWindow? = null
    private var groupListAdapter: GroupListAdapter? = null
    private var expensePaidByAdapter: ExpenseOwnerListAdapter? = null
    private var expensePaidForAdapter: ExpensePaidForListAdapter? = null

    private val emptyString = ""
    private val args by navArgs<AddExpenseFragmentArgs>()

    override val hasBottomNavigationView: Boolean = false
    override val viewModelClass: Class<AddExpenseViewModel> = AddExpenseViewModel::class.java

    override fun onCreateView() = Unit

    override fun layoutRes(): Int = R.layout.fragment_add_expense

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpBinding()
        setUpSplitByPopupWindow()
        setUpExpenseImage()
        setUpGroupNamesAdapter()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        hideSplitByPopUp()
        super.onPause()
    }

    override fun observeLiveData(
        viewModel: AddExpenseViewModel,
        binding: FragmentAddExpenseBinding
    ) {
        viewModel.groupsLiveData().observe(viewLifecycleOwner, Observer { groupResource ->
            if (groupResource != null) {
                when (groupResource) {
                    is Resource.Success -> {
                        groupListAdapter?.submitList(groupResource.data)
                    }

                    is Resource.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Failed to retrieve group list : ${groupResource.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                groupListAdapter?.submitList(emptyList())
            }
        })

        viewModel.datePickerLiveData.observe(viewLifecycleOwner, Observer {
            binding.tvSelectDate.text = it
        })

        viewModel.imageUriLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                argimageUri = it
                binding.imageFile = it
                binding.tvFileName.text = it.lastPathSegment ?: emptyString
            }
        })

        viewModel.mutableAmountPaidLiveData.observe(viewLifecycleOwner, Observer {
            amountPaid = if (it.isNullOrEmpty()) 0f else it.toFloat()
            dispatchUpdateToExpenseParticipantsAdapter()
        })

        viewModel.participantsCountLiveData.observe(viewLifecycleOwner, Observer {
            currentNoOfParticipants = it
        })

        viewModel.participantsShareLiveData.observe(viewLifecycleOwner, Observer {
            listOfParticipantsShare = it
        })

        viewModel.requestFocusLiveData.observe(viewLifecycleOwner, Observer {
            if (it) handleOnScreenFocus()
        })

        viewModel.listOfMissingFieldsLiveData.observe(viewLifecycleOwner, Observer {
            binding.listOfMissingFields = it
        })

        viewModel.selectedGroupLiveData.observe(viewLifecycleOwner, Observer {
            it?.let { onGroupSelected(it) }
        })

        viewModel.listOfUncheckedParticipantsLiveData.observe(viewLifecycleOwner, Observer {
            listOfUncheckedFields = it
            expensePaidForAdapter?.setListOfUncheckedParticipants(it)
        })

        viewModel.selectedExpenseOwnerLiveData.observe(viewLifecycleOwner, Observer {
            binding.expenseOwner = it
        })

        viewModel.groupMembersLiveData.observe(viewLifecycleOwner, Observer { userResource ->
            if (userResource != null) {
                when (userResource) {
                    is Resource.Success -> {
                        expensePaidByAdapter?.submitList(userResource.data)
                        val data: MutableList<ForExpenseShared>? =
                            userResource.data?.toMutableList()
                        data?.add(TotalExpenseShared())
                        expensePaidForAdapter?.submitList(data)
                    }

                    is Resource.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Failed to retrieve users list : ${userResource.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                expensePaidByAdapter?.submitList(emptyList())
            }
        })
    }

    override fun onDestroy() {
        deleteImageFile()
        clearTextFields()
        //showBottomNavigation()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.RC_IMAGE -> handleImageRequestResult(data)
            }
        }
    }

    // Setup click listener on image view
    @Suppress("UNUSED_PARAMETER")
    fun onImageClick(view: View) {
        if (argimageUri != null) {
            argimageUri?.let {
                navigateToPreviewScreen(it)
            }
        } else {
            setUpImagePicker()
        }
    }

    // Setup click listener on select date dialog
    @Suppress("UNUSED_PARAMETER")
    fun onSelectDateTextViewClick(view: View) =
        findNavController().navigate(AddExpenseFragmentDirections.actionAddExpenseFragmentToSelectDateFragment())

    //Setup click listener on Cancel Add Expense
    @Suppress("UNUSED_PARAMETER")
    fun onCancelExpense(view: View) {
        findNavController().popBackStack()
    }

    // Clear text fields when fragment is popped from stack
    private fun clearTextFields() {
        binding.etAddExpenseItemDesc.text = emptyString.toEditable()
        binding.etAddExpenseAmountPaid.text = emptyString.toEditable()
    }

    // Delete image file from uri when the view is destroyed in order to avoid saving it in local storage
    private fun deleteImageFile() = ImageUtil.deleteFileFromUri(requireContext(), argimageUri)

    // Handle image request result received from image picker/gallery
    private fun handleImageRequestResult(data: Intent?) {
        val imageUri: Uri = data?.data ?: return
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val inputBitmap = BitmapFactory.decodeStream(inputStream)
        val outputFile = ImageUtil.writeBitmapToFile(inputBitmap, requireContext())

        viewModel.setMutableImageUri(Uri.fromFile(outputFile))
    }

    // Setup binding variables for layout databinding
    private fun setUpBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            presenter = this@AddExpenseFragment
            addExpenseViewModel = viewModel
        }
    }

    // Setup image picker from local storage or gallery
    private fun setUpImagePicker() {
        val chooseIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        chooseIntent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(
                chooseIntent,
                resources.getString(R.string.select_image)
            ), Constants.RC_IMAGE
        )
    }

    // Show image preview screen if image is applied to image view
    private fun navigateToPreviewScreen(argImageUri: Uri) {
        val extras = FragmentNavigatorExtras(
            binding.ivItemImageGlide to argImageUri.scheme.toString(),
            binding.tvFileName to argImageUri.encodedPath.toString()
        )
        findNavController().navigate(
            AddExpenseFragmentDirections.actionAddExpenseFragmentToCaptureExpensePreviewFragment(
                argImageUri
            ), extras
        )
    }

    // Setup split by popup window and set click listener for popup
    private fun setUpSplitByPopupWindow() {
        createSplitByPopUpWindow()
        binding.ibSplitAmountBy.setOnClickListener {
            showSplitByPopUp(it)
        }
    }

    // Setup Expense image on image view when navigated from Camera capture or image already applied from local storage
    private fun setUpExpenseImage() {
        arguments?.let {
            try {
                if (argimageUri == null)
                    argimageUri = args.photoFile
                argimageUri?.let { viewModel.setMutableImageUri(it) }
            } catch (ignored: Exception) {
            }
        }
    }

    // Set up adapter for Group List view to select group name
    private fun setUpGroupNamesAdapter() {
        this.groupListAdapter = GroupListAdapter {
            Toast.makeText(
                requireContext(),
                "Selected Group is ${it.groupName}",
                Toast.LENGTH_LONG
            ).show()
            resetDependentViewsAndData()
            viewModel.onGroupNameSelected(it)
        }

        groupListAdapter?.let { binding.customExpandGroupNames.setRecyclerAdapter(it) }
    }

    // Setup Expense paid by adapter to set up Expense Owner list
    private fun setUpExpensePaidByAdapter() {
        this.expensePaidByAdapter =
            ExpenseOwnerListAdapter(selectedGroup?.groupAdmin?.keys?.elementAt(0) ?: emptyString) {
                handleOnScreenFocus()
                binding.customExpandExpensePaidBy.collapseView()
                viewModel.onExpenseOwnerSelected(it.name)
            }

        expensePaidByAdapter?.let { binding.customExpandExpensePaidBy.setRecyclerAdapter(it) }
    }

    // Setup Expense shared adapter for list of users and distribute contributions equally/unequally
    private fun setUpExpensePaidForAdapter() {
        this.expensePaidForAdapter =
            ExpensePaidForListAdapter(
                viewLifecycleOwner,
                viewModel,
                listOfParticipantsShare
            )

        expensePaidForAdapter?.let {
            binding.customExpandExpensePaidFor.setRecyclerAdapter(it)
            binding.customExpandExpensePaidFor.setCollapsedViewListener(object :
                CustomExpandableRecyclerView.CustomCollapsedViewListener {
                override fun onViewExpandStart() = Unit
                override fun onViewCollapsed() = Unit
                override fun onViewExpanded() {
                    it.notifyDataSetChanged()
                }
            })
        }
    }

    // Split amount equally among no of checked participants
    private fun shareAmountPaidToParticipants() {
        currentNoOfParticipants?.let { currentNoOfParticipants ->
            val amountPerParticipant = amountPaid / currentNoOfParticipants
            if (listOfParticipantsShare.isNullOrEmpty()) {
                for (i in 0 until currentNoOfParticipants)
                    listOfParticipantsShare.add(amountPerParticipant)
            } else {
                listOfParticipantsShare.forEachIndexed { index, _ ->
                    if (!listOfUncheckedFields.contains(index))
                        listOfParticipantsShare[index] = amountPerParticipant
                }
            }
        }

        viewModel.setParticipantsShareList(listOfParticipantsShare)
    }

    // Called when group name is selected from the Group list 
    private fun onGroupSelected(group: Groups) {
        initValuesForDependentViewSetup(group)
        setUpExpensePaidByAdapter()
        setUpExpensePaidForAdapter()
        handleOnScreenFocus()
        observeSplitByLiveData()
    }

    // Observe split amount by LiveData changes and performs following tasks on changes (clear focus, split amount)
    private fun observeSplitByLiveData() {
        viewModel.splitAmountByLiveData.observe(viewLifecycleOwner, Observer {
            handleOnScreenFocus()
            shareAmountPaidToParticipants()
            notifyExpensePaidForAdapter(it)
        })
    }

    // Clear focus from screen window, if any
    private fun handleOnScreenFocus() = requireActivity().clearFocus()

    // Reset all dependent views on new group selected
    private fun resetDependentViewsAndData() {
        binding.customExpandGroupNames.collapseView()
        binding.customExpandExpensePaidBy.collapseView()
        binding.customExpandExpensePaidFor.collapseView()
        viewModel.resetParticipantsCountLiveData()
        viewModel.resetParticipantShareLiveData()
        viewModel.resetExpenseOwnerLiveData()
    }

    // Initialize LiveData, binding variables and local variables on new group selected
    private fun initValuesForDependentViewSetup(group: Groups) {
        initParticipantsSharedLiveData(group)
        initSelectedGroup(group)
        initCurrencyCode(group)
        initAdminUserId()
    }

    // Initialize no of participants count and initialize participants list (+1 Including Admin)
    private fun initParticipantsSharedLiveData(group: Groups) {
        val participantsCount = if (currentNoOfParticipants == null)
            group.groupMembers.keys.size + 1
        else
            currentNoOfParticipants
        val newListOfParticipantShare: ArrayList<Float> = if (listOfParticipantsShare.isNotEmpty())
            listOfParticipantsShare
        else
            ArrayList()
        viewModel.setParticipantsCount(participantsCount ?: 0)
        viewModel.setParticipantsShareList(newListOfParticipantShare)
    }

    // Initialize selected group
    private fun initSelectedGroup(group: Groups) {
        selectedGroup = group
    }

    // Initialize DataBinding currency variable
    private fun initCurrencyCode(group: Groups) {
        binding.currency = group.currency
    }

    // Initialize admin id in Expense paid by adapter in order to set Admin tag in Expense owner list
    private fun initAdminUserId() {
        expensePaidByAdapter?.setAdminUserId(
            selectedGroup?.groupAdmin?.keys?.elementAtOrNull(0) ?: emptyString
        )
    }

    // Dispatch base amount changes to Expense Paid for adapter and updates the list
    private fun dispatchUpdateToExpenseParticipantsAdapter() {
        shareAmountPaidToParticipants()
        expensePaidForAdapter?.notifyDataSetChanged()
    }

    // Dispatch update to Expense Paid for adapter for any split by changes
    private fun notifyExpensePaidForAdapter(splitBy: String) {
        expensePaidForAdapter?.splitBy = splitBy
        if (!binding.customExpandExpensePaidFor.isCollapsed)
            expensePaidForAdapter?.notifyDataSetChanged()
    }

    // Create Split By popup window and attach on click listener
    private fun createSplitByPopUpWindow() {
        val popupWidth = resources.getDimension(R.dimen.split_menu_width).toInt()
        val popUpBinding = DataBindingUtil.inflate<LayoutSplitAmountBinding>(
            layoutInflater,
            R.layout.layout_split_amount,
            null,
            false
        )

        popUpWindow = PopupWindow(
            popUpBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        popUpWindow?.apply {
            isOutsideTouchable = true
            animationStyle = R.style.popup_split
            width = popupWidth
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_popup_menu
                )
            )
        }

        popUpBinding.apply {
            addExpenseViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            radEqually.setOnClickListener { hideSplitByPopUp() }
            radUnequally.setOnClickListener { hideSplitByPopUp() }
        }
    }

    // Show Split by popup window
    private fun showSplitByPopUp(btnSplit: View) {
        val loc = btnSplit.locateView()
        popUpWindow?.showAtLocation(
            view,
            Gravity.TOP or Gravity.CENTER,
            btnSplit.width * 2,
            loc.bottom
        )
        popUpWindow?.showAsDropDown(btnSplit)
    }

    // Dismiss split by popup window if displayed
    private fun hideSplitByPopUp() = popUpWindow?.dismiss()
}
