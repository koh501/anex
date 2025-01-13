package  com.example.imgshot

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class FullscreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        //val imageList = intent.getSerializableExtra("images") as List<Bitmap>
        val imageList = intent.getParcelableArrayListExtra<Bitmap>("images")?.toList() ?: listOf()

        // 최신 이미지 순서로 정렬
        val sortedImageList = imageList.reversed()

        val adapter = ImagePagerAdapter(sortedImageList)
        //val adapter = ImagePagerAdapter(imageList)
        viewPager.adapter = adapter

        val initialPosition = intent.getIntExtra("position", 0)
        viewPager.setCurrentItem(initialPosition, false)
    }
}
