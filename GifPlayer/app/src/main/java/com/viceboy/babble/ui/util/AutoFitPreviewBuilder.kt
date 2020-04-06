package com.viceboy.babble.ui.util

import android.content.Context
import android.graphics.Matrix
import android.hardware.display.DisplayManager
import android.util.Size
import android.view.*
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.roundToInt

@Suppress("NAME_SHADOWING")
class AutoFitPreviewBuilder private constructor(
    config: PreviewConfig,
    viewFinder: WeakReference<TextureView>
) {

    private val usecase: Preview

    private var bufferRotation: Int = 0
    private var viewFinderRotation: Int? = null
    private var bufferDimens: Size = Size(0, 0)
    private var viewFinderDimens: Size = Size(0, 0)
    private var viewFinderDisplay: Int? = -1

    private lateinit var displayManager: DisplayManager

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayChanged(displayId: Int) {
            val viewFinder = viewFinder.get() ?: return
            if (displayId == viewFinderDisplay) {
                val display = displayManager.getDisplay(displayId)
                val rotation = getDisplaySurfaceRotation(display)
                updateTransform(viewFinder, rotation, bufferDimens, viewFinderDimens)
            }
        }

        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
    }

    /**
     * Helper method that fits preview into SurfaceTexture
     */
    private fun updateTransform(
        viewFinder: TextureView?,
        rotation: Int?,
        newBufferDimens: Size?,
        newViewFinderDimens: Size?
    ) {
        val textureView = viewFinder ?: return

        if (rotation == viewFinderRotation
            && Objects.equals(newBufferDimens, bufferDimens)
            && Objects.equals(newViewFinderDimens, viewFinderDimens)
        ) {
            //No change has been taken place so no need to transform
            return
        }

        //Check for invalid rotation and update internal field for valid rotation
        if (rotation == null) return
        else viewFinderRotation = rotation

        //Checks for Invalid buffer dimension and update internal field and wait for valid buffer dimen
        if (newBufferDimens == null || newBufferDimens.width == 0) return
        else bufferDimens = newBufferDimens

        //Checks for invalid viewFinderBufferDimen and update internal field for valid viewFinderBufferDimen
        if (newViewFinderDimens == null || newViewFinderDimens.width == 0) return
        else viewFinderDimens = newViewFinderDimens

        Matrix().apply {
            //Applying output transformation after changes
            Timber.i(
                "Applying output transformations\n" +
                        "View Finder Size : $viewFinderDimens\n" +
                        "Preview output size : $bufferDimens\n" +
                        "View finder rotation : $viewFinderRotation" +
                        "Preview output rotation : $bufferRotation"
            )

            //Get the center of ViewFinder
            val centerX = viewFinderDimens.width / 2f
            val centerY = viewFinderDimens.height / 2f

            postRotate(viewFinderRotation!!.toFloat(), centerX, centerY)

            //Buffers are rotated based on device orientation
            val bufferRatio = viewFinderDimens.width.toFloat() / viewFinderDimens.height.toFloat()

            val scaledHeight: Int
            val scaledWidth: Int
            //Apply center crop transformations
            if (viewFinderDimens.width > viewFinderDimens.height) {
                scaledHeight = viewFinderDimens.width
                scaledWidth = (viewFinderDimens.width * bufferRatio).roundToInt()
            } else {
                scaledHeight = viewFinderDimens.height
                scaledWidth = (viewFinderDimens.height * bufferRatio).roundToInt()
            }


            //Calculate relative scale value
            val xScale = scaledWidth / viewFinderDimens.width.toFloat()
            val yScale = scaledHeight / viewFinderDimens.height.toFloat()

            preScale(xScale, yScale, centerX, centerY)

            textureView.setTransform(this)

            mutableOnPreviewReadyLiveData.value = true
        }
    }

    init {
        //Initialize viewFinderDisplay & viewFinderRotation
        mutableOnPreviewReadyLiveData.value = false
        val viewFinderRef = viewFinder.get()
            ?: throw IllegalArgumentException("Invalid reference to viewFinder is used")

        viewFinderDisplay = viewFinderRef.display.displayId
        viewFinderRotation = getDisplaySurfaceRotation(viewFinderRef.display) ?: 0

        usecase = Preview(config)

        usecase.onPreviewOutputUpdateListener = Preview.OnPreviewOutputUpdateListener {
            val parent = viewFinderRef.parent as ViewGroup
            parent.removeView(viewFinderRef)
            parent.addView(viewFinderRef, 0)

            //Update Internal texture
            viewFinderRef.surfaceTexture = it.surfaceTexture

            bufferRotation = it.rotationDegrees
            val rotation = getDisplaySurfaceRotation(viewFinderRef.display)
            updateTransform(viewFinderRef, rotation, it.textureSize, viewFinderDimens)
        }

        //Setting up listener when ViewFinder layout changes
        viewFinderRef.addOnLayoutChangeListener { v, left, top, right, bottom, _, _, _, _ ->
            val updatedViewFinderRef = v as TextureView
            val newViewFinderDimens = Size(right - left, bottom - top)
            val rotation = getDisplaySurfaceRotation(updatedViewFinderRef.display)
            updateTransform(updatedViewFinderRef, rotation, bufferDimens, newViewFinderDimens)
        }

        displayManager =
            viewFinderRef.context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        //Remove the display listener when the view is detached
        viewFinderRef.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                displayManager.unregisterDisplayListener(displayListener)
            }

            override fun onViewAttachedToWindow(v: View?) {
                displayManager.registerDisplayListener(displayListener, null)
            }

        })
    }

    companion object {

        private val mutableOnPreviewReadyLiveData = MutableLiveData<Boolean>(false)

        fun getDisplaySurfaceRotation(display: Display): Int? {
            return when (display.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                Surface.ROTATION_90 -> 90
                else -> null
            }
        }

        fun onPreviewReadyLiveData(): LiveData<Boolean> = mutableOnPreviewReadyLiveData

        fun build(config: PreviewConfig, viewFinder: TextureView) =
            AutoFitPreviewBuilder(config, WeakReference(viewFinder)).usecase
    }
}