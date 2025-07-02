package br.edu.puccampinas.fitjourney

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PhotoFullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_full_screen)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val imageView = findViewById<ImageView>(R.id.imgFullScreen)
        val url = intent.getStringExtra("imageUrl")

        if (url != null) {
            Glide.with(this).load(url).into(imageView)
        }

        val btnBack = findViewById<ImageView>(R.id.comeBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
