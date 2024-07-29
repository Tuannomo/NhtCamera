package com.nht.nhtcamerademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nht.nhtcamera.CameraManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CameraManager().initializeCamera(this)
    }
}