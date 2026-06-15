package com.snokonoko.app.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PhotoViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val photoPath = intent.getStringExtra("photo_path") ?: run {
            finish()
            return
        }

        val imageView = ImageView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(android.graphics.Color.BLACK)
            setOnClickListener { finish() }
        }

        setContentView(imageView)

        // Load and display the photo
        val bitmap = BitmapFactory.decodeFile(photoPath)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            finish()
        }
    }
}
