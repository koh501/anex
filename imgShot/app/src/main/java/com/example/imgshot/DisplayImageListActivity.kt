package com.example.imgshot

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DisplayImageListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_image_list)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val imagePaths = intent.getStringArrayListExtra("image_paths")
        val imageList = imagePaths?.map { BitmapFactory.decodeFile(it) } ?: listOf()

        val adapter = ImageAdapter(imageList, {}, { _, _ -> })
        recyclerView.adapter = adapter
    }
}
