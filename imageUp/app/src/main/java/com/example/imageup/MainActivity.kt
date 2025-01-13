package  com.example.imageup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGES_REQUEST = 1
    private lateinit var selectedImagesUris: MutableList<Uri>
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        selectedImagesUris = mutableListOf()
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        imageAdapter = ImageAdapter(this, selectedImagesUris)
        recyclerView.adapter = imageAdapter

        val buttonSelect: Button = findViewById(R.id.button_select)
        val buttonUpload: Button = findViewById(R.id.button_upload)

        buttonSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, PICK_IMAGES_REQUEST)
        }

        buttonUpload.setOnClickListener {
            if (selectedImagesUris.isNotEmpty()) {
                uploadImages()
            } else {
                Toast.makeText(this, "이미지를 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            selectedImagesUris.clear()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    selectedImagesUris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                selectedImagesUris.add(data.data!!)
            }
            imageAdapter.notifyDataSetChanged()
            Toast.makeText(this, "선택된 이미지: ${selectedImagesUris.size}개", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImages() {
        // 업로드 코드 구현 (이전 예제 참고)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.4/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val parts = mutableListOf<MultipartBody.Part>()
        for (uri in selectedImagesUris) {
            val imageFile = File(getRealPathFromURI(uri))
            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
            val imagePart = MultipartBody.Part.createFormData("images[]", imageFile.name, requestBody)
            parts.add(imagePart)
        }

        val call = service.uploadImages(parts)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "이미지 업로드 성공!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "이미지 업로드 실패!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "네트워크 오류!", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getRealPathFromURI(uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }
}

interface ApiService {
    @Multipart
    @POST("upload2.php")
    fun uploadImages(@Part images: List<MultipartBody.Part>): Call<ResponseBody>
}