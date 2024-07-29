package com.nht.nhtcamera.camera

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.nht.nhtcamera.R
import android.provider.Settings
import android.util.Log

class CameraMainScreen : AppCompatActivity() {
    private lateinit var viewModel: CameraViewModel
    private lateinit var deviceOrientationListener: DeviceOrientationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_sceen)

        initControl()
    }

    private fun initControl() {
        deviceOrientationListener = DeviceOrientationListener(this)
        deviceOrientationListener.setOnOrientationChangeListener(onOrientationListener)
        if (arePermissionsGranted()) {
            Log.d("TUANNH","show camera Fragment")
            // All permissions are granted, proceed with your functionality
            showCameraFragment()
        } else {
            Log.d("TUANNH","requestPermissions")
            // Request permissions
            requestPermissions()
        }
    }

    fun showFragment() {
        //TODO: SHOW EDIT SCREEN
//        val fragment = EditImageFragment.newInstance()
//        supportFragmentManager.beginTransaction()
//            .add(R.id.frmCameraContent, fragment)
//            .addToBackStack(fragment.tag)
//            .commit()
    }

    private val onOrientationListener = object :
        DeviceOrientationListener.OnOrientationChangeListener {
        override fun onOrientationChange(rotation: Int) {
            viewModel.onOrientationChange(rotation)
        }
    }

    private fun arePermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_MEDIA_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_MEDIA_LOCATION
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Check if all permissions are granted
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                // All permissions granted, proceed with your functionality
                showCameraFragment()
            } else {
                // Handle the case where permissions are not granted
                showPermissionRationaleDialog()
            }
        }
    }

    private fun showCameraFragment() {
        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        deviceOrientationListener = DeviceOrientationListener(this)
        deviceOrientationListener.setOnOrientationChangeListener(onOrientationListener)

        val isRequireLocation = intent.getBooleanExtra(REQUIRE_LOCATION, false)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frmCameraContent, CameraFragment.newInstance(isRequireLocation))
            .commit()
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Please assign permission to use this feature.")
            .setCancelable(false) // Make the dialog non-cancelable
            .setPositiveButton("Settings") { _, _ ->
                // Open app settings to allow the user to grant permissions manually
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .show()
    }

    override fun onStart() {
        deviceOrientationListener.enable()
        super.onStart()
    }

    override fun onStop() {
        deviceOrientationListener.disable()
        super.onStop()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            viewModel.onCameraCancelClick()
            super.onBackPressed()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val REQUIRE_LOCATION = "REQUIRE_LOCATION"

        @JvmStatic
        fun startInstance(context: Context, isRequireLocation: Boolean) {
            val intent = Intent(context, CameraMainScreen::class.java)
            intent.putExtra(REQUIRE_LOCATION, isRequireLocation)
            context.startActivity(intent)
        }
    }
}