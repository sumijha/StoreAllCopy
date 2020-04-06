package com.viceboy.babble.ui.screens.captureExpense

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentCaptureExpensePreviewBinding
import com.viceboy.babble.ui.util.ImageUtil

class CaptureExpensePreviewFragment : Fragment() {

    private val args by navArgs<CaptureExpensePreviewFragmentArgs>()
    private val handler = Handler(Looper.getMainLooper())

    private var currentDecorVisibility: Int = 0
    private var argsImageUri: Uri? = null

    private lateinit var dataBinding: FragmentCaptureExpensePreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        currentDecorVisibility = activity?.window?.decorView?.systemUiVisibility ?: 0
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentCaptureExpensePreviewBinding>(
            inflater,
            R.layout.fragment_capture_expense_preview,
            container,
            false
        )
        dataBinding = binding

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.update_image)

        argsImageUri = args.photoFile
        val listener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean = false

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                startPostponedEnterTransition()
                return false
            }
        }

        handler.postDelayed(1000) {
            startPostponedEnterTransition()
        }

        postponeEnterTransition()

        dataBinding.imageRequestListener = listener
        dataBinding.imageFile = argsImageUri
        dataBinding.tvExpenseImageName.text = argsImageUri?.lastPathSegment.toString()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpBinding()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        ImageUtil.deleteFileFromUri(requireContext(),argsImageUri)
        super.onDestroy()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onBackButtonPress(view: View) {
        findNavController().popBackStack()
    }

    private fun setUpBinding() {
        dataBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            presenter = this@CaptureExpensePreviewFragment
        }
    }
}
