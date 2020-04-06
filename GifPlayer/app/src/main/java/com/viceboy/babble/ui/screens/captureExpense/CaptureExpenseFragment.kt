package com.viceboy.babble.ui.screens.captureExpense

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.viceboy.babble.R
import com.viceboy.babble.databinding.CameraUiContainerBinding
import com.viceboy.babble.databinding.FragmentCaptureExpenseBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseHomeFragment
import com.viceboy.babble.ui.util.*
import io.reactivex.Observable
import java.io.File

class CaptureExpenseFragment :
    BaseHomeFragment<CaptureExpenseViewModel, FragmentCaptureExpenseBinding>(), Injectable {

    private lateinit var outputDir: File

    private var displayId = -1
    private var currentCameraLens = CameraX.LensFacing.BACK
    private var containerUiVisibility: Int = 0
    private var preview: Preview? = null
    private var displayManager: DisplayManager? = null
    private var outputPhotoFile: File? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null

    /**
     * Setting up a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifests for 180 rotation
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayChanged(displayId: Int) {
            view?.let {
                if (displayId == this@CaptureExpenseFragment.displayId) {
                    preview?.setTargetRotation(it.display.rotation)
                    imageCapture?.setTargetRotation(it.display.rotation)
                    imageAnalysis?.setTargetRotation(it.display.rotation)
                }
            }
        }

        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onImageSaved(file: File) {
            outputPhotoFile = file
            sendImageFileToAddExpense(file)
        }

        override fun onError(
            useCaseError: ImageCapture.UseCaseError,
            message: String,
            cause: Throwable?
        ) = Toast.makeText(
            requireContext(),
            "Failed to capture image $message",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun layoutRes(): Int = R.layout.fragment_capture_expense

    override val viewModelClass: Class<CaptureExpenseViewModel> =
        CaptureExpenseViewModel::class.java
    override val hasBottomNavigationView: Boolean = false

    override fun onCreateView() = Unit

    override fun observeLiveData(
        viewModel: CaptureExpenseViewModel,
        binding: FragmentCaptureExpenseBinding
    ) {
        AutoFitPreviewBuilder.onPreviewReadyLiveData().observe(viewLifecycleOwner, Observer {
            viewModel.enableShutterButtonClick()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!hasRequiredPermissions()) return
        initOutputDir()
        //setUpBottomNavVisibility()
        setUpBinding()
        setUpDisplayManager()
        setUpCameraUi()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        containerUiVisibility = binding.cameraContainer.systemUiVisibility
        binding.cameraContainer.postDelayed(
            { binding.cameraContainer.systemUiVisibility = FLAGS_FULLSCREEN }, 500L
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        outputPhotoFile?.delete()
        activity?.window?.decorView?.systemUiVisibility = containerUiVisibility
        displayManager?.unregisterDisplayListener(displayListener)
    }


    //Setting up on Shutter Button Click
    @Suppress("UNUSED_PARAMETER")
    fun onShutterButtonClick(view: View) {
        imageCapture?.let {
            //Create output file to hold the image
            val photoFile = ImageUtil.createFile(outputDir)

            //Flip image if using front camera
            val metaData = ImageCapture.Metadata().apply {
                isReversedHorizontal = currentCameraLens == CameraX.LensFacing.FRONT
            }

            //Setup image capture listener triggered after image is captured
            it.takePicture(photoFile, imageSavedListener, metaData)

            //Disable Shutter Button after click
            viewModel.disableShutterButtonClick()

            //Show the white flash on capture image
            binding.cameraContainer.postDelayed({
                binding.cameraContainer.foreground = ColorDrawable(Color.WHITE)
                binding.cameraContainer.postDelayed({
                    binding.cameraContainer.foreground = null
                }, ANIMATION_FAST_MILLIS)
            }, ANIMATION_SLOW_MILLIS)
        }
    }

    //Setting up Switch Camera Button Listener
    @SuppressLint("RestrictedApi")
    fun onSwitchCameraButtonClick(view: View) {
        view.post {
            AnimationUtil.rotateViewInAntiClockwiseDir(view)
        }
        currentCameraLens = if (currentCameraLens == CameraX.LensFacing.FRONT)
            CameraX.LensFacing.BACK
        else
            CameraX.LensFacing.FRONT

        try {
            //Check if camera has lens facing
            CameraX.getCameraWithLensFacing(currentCameraLens)
            CameraX.unbindAll()

            bindCameraUseCases()
        } catch (ignored: Exception) {
        }
        // viewModel.enableSwitchCameraButtonClick()
    }

    private fun initOutputDir() {
        outputDir = ImageUtil.getMediaDirectory(requireContext())
    }

    private fun setUpCameraUi() {
        binding.viewFinder.post {
            //Keep track of the display in which view is attached
            displayId = binding.viewFinder.display.displayId

            //Build UI Controls and bind all camera usecases
            updateCameraUI()
            bindCameraUseCases()
        }
    }

    private fun updateCameraUI() {
        //Remove previous UI, if any
        binding.cameraContainer.findViewById<ConstraintLayout>(R.id.camera_container)?.let {
            binding.cameraContainer.removeView(it)
        }

        val cameraUiBinding = DataBindingUtil.inflate<CameraUiContainerBinding>(
            layoutInflater,
            R.layout.camera_ui_container,
            binding.cameraContainer,
            true
        )

        cameraUiBinding.apply {
            presenter = this@CaptureExpenseFragment
            lifecycleOwner = viewLifecycleOwner
            captureExpenseViewModel = viewModel
        }
    }

    // Declare bind, preview, capture and analyze use cases
    private fun bindCameraUseCases() {
        setUpCameraConfig()
        CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageCapture)
    }

    //Compress the image file and send to add expense fragment
    private fun sendImageFileToAddExpense(file: File) {
        Observable.create<Uri> { emitter ->
                emitter.onNext(ImageUtil.compressImage(file, outputDir))
            }
            .scheduleOnBackAndOutOnMain()
            .addToCompositeDisposable(compositeDisposable, {
                findNavController().navigate(
                    CaptureExpenseFragmentDirections.actionGlobalAddExpense(
                        it, Constants.KEY_FROM_CAPTURE_FRAGMENT
                    )
                )
            }, {
                Toast.makeText(
                    requireContext(),
                    "${resources.getString(R.string.on_capture_expense_error)}: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            })
    }

    private fun hasRequiredPermissions(): Boolean {
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            findNavController().navigate(R.id.action_captureExpense_to_permissionsFragment)
            return false
        }
        return true
    }

    //Setting up required CameraX configs such as Preview and ImageCapture
    private fun setUpCameraConfig() {
        //Get Screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val viewFinderConfig = PreviewConfig.Builder().apply {
            setLensFacing(currentCameraLens)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(binding.viewFinder.display.rotation)
        }.build()

        preview = AutoFitPreviewBuilder.build(viewFinderConfig, binding.viewFinder)

        //Set up capture use case to allow users to take photos
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(currentCameraLens)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(binding.viewFinder.display.rotation)
        }.build()

        imageCapture = ImageCapture(imageCaptureConfig)
        viewModel.flashModeLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            imageCapture?.flashMode = it
        })
    }

    private fun setUpBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            captureViewModel = viewModel
        }
    }

    private fun setUpDisplayManager() {
        displayManager =
            binding.viewFinder.context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager?.registerDisplayListener(displayListener, null)
    }

    companion object {

        private const val ANIMATION_FAST_MILLIS = 50L
        private const val ANIMATION_SLOW_MILLIS = 100L
        private const val FLAGS_FULLSCREEN = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN
    }
}
