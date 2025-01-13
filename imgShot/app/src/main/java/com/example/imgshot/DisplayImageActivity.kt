package com.example.imgshot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView

class DisplayImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_image)

        val imageView: ImageView = findViewById(R.id.display_image_view)
        val imagePath = intent.getStringExtra("image_path")
        val bitmap = BitmapFactory.decodeFile(imagePath)

        imageView.setImageBitmap(bitmap)
    }
}
