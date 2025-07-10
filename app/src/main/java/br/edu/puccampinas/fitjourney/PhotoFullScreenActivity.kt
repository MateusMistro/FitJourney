package br.edu.puccampinas.fitjourney

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PhotoFullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_photo_full_screen)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Recupera o ImageView da tela
        val imageView = findViewById<ImageView>(R.id.imgFullScreen)

        // Recupera a URL da imagem passada pela intent
        val url = intent.getStringExtra("imageUrl")

        // Carrega a imagem da URL no ImageView usando Glide
        if (url != null) {
            Glide.with(this).load(url).into(imageView)
        }

        val btnBack = findViewById<ImageView>(R.id.comeBack)
        btnBack.setOnClickListener {
            finish()
        }
    }

    // Trata o clique no botão da ActionBar (voltar)
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
