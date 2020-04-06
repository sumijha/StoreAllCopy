package com.viceboy.babble.ui.screens.captureExpense


import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.viceboy.babble.R

class PermissionsFragment : Fragment() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val permissionsRequired = arrayOf(android.Manifest.permission.CAMERA)

        //Check if all required permissions are granted
        fun hasPermissions(context: Context) = permissionsRequired.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions(requireContext()))
            requestPermissions(permissionsRequired, PERMISSIONS_REQUEST_CODE)
        else
            findNavController().navigate(R.id.action_permissionsFragment_to_captureExpense)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                findNavController().navigate(R.id.action_permissionsFragment_to_captureExpense)
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.on_permissions_denied),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }
}
