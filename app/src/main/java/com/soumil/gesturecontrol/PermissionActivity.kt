package com.soumil.gesturecontrol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.soumil.gesturecontrol.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            getCameraPermission()
        }
    }

    private fun getCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
        }
        else{
            cameraPermissionGranted()
        }
    }

    private fun cameraPermissionGranted() {
        Snackbar.make(binding.root, "Permission Granted", Snackbar.LENGTH_SHORT).show()
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finishAffinity()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            cameraPermissionGranted()
        }
        else{
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog)
            .setTitle("Camera Permission Denied")
            .setMessage("This app requires access of device's camera in order to function properly. Please grant in the settings.")
            .setPositiveButton("Grant"){ dialog, _ ->
                dialog.dismiss()
                openAppInfo()
            }
            .setNegativeButton("Deny"){ dialog, _ ->
                dialog.dismiss()
                Snackbar.make(binding.root, "Permission Denied", Snackbar.LENGTH_SHORT).show()
            }
            .setCancelable(true)
            .show()
    }

    private fun openAppInfo() {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(i)
    }
}