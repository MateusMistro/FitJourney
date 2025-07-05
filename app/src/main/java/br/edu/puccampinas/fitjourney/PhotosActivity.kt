package br.edu.puccampinas.fitjourney

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityPhotosBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotosBinding

    private lateinit var layoutListPhotos: LinearLayout

    // Código para identificar a ação de escolha de imagens no Intent
    private val PICK_IMAGES_REQUEST = 101

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ativa o edge-to-edge para layout moderno
        enableEdgeToEdge()

        supportActionBar?.hide()

        binding = ActivityPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        layoutListPhotos = binding.layoutPhotos

        val btnUpload = binding.btnAdd
        btnUpload.setOnClickListener {
            startUpload()
        }

        listPhotos()
    }

    // Recebe o resultado da seleção de imagens
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK) {
            val uris = mutableListOf<Uri>()

            // Permite selecionar múltiplas imagens (clipData) ou apenas uma (data)
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            } ?: data?.data?.let {
                uris.add(it)
            }

            if (uris.isNotEmpty()) {
                photosUpload(uris)
            }
        }
    }

    // Abre a galeria para selecionar uma ou várias imagens
    private fun startUpload(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // Tipos aceitos: imagens
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Permite múltipla seleção
        startActivityForResult(Intent.createChooser(intent, "Selecionar fotos"), PICK_IMAGES_REQUEST)
    }

    private fun photosUpload(fotos: List<Uri>) {
        val userId = auth.currentUser?.uid ?: return
        val actualDate = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
        val urls = mutableListOf<String>()

        val storageRef = storage.reference.child("fotos")
        val total = fotos.size
        var finished = 0

        for (uri in fotos) {
            val fileRef = storageRef.child("${System.currentTimeMillis()}_${uri.lastPathSegment}")
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { url ->
                        urls.add(url.toString())
                        finished++

                        if (finished == total) {
                            val info = hashMapOf(
                                "UserId" to userId,
                                "data" to actualDate,
                                "urls" to urls
                            )

                            db.collection("photos")
                                .add(info)
                                .addOnSuccessListener {
                                    positiveMessage("Fotos enviadas com sucesso!")
                                    listPhotos()
                                }
                        }
                    }
                }
        }
    }

    private fun listPhotos() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("photos")
            .whereEqualTo("UserId", userId)
            .get()
            .addOnSuccessListener { documents ->
                layoutListPhotos.removeAllViews()

                val docsList = documents.documents.reversed() // Inverte para mostrar as mais recentes primeiro
                for (doc in docsList) {
                    val date = doc.getString("data") ?: "Sem data"
                    val urls = doc.get("urls") as? List<*> ?: continue

                    // Container para agrupar fotos e data
                    val container = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16, 16, 16, 16)
                        background = getDrawable(R.drawable.borda)
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 24, 0, 60)
                        layoutParams = params
                    }

                    val title = TextView(this).apply {
                        text = "Fotos de: $date"
                        textSize = 18f
                        setTextColor(Color.BLACK)
                        setTypeface(null, Typeface.BOLD)
                    }
                    container.addView(title)

                    for (url in urls) {
                        val imageView = ImageView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(500, 500)
                            setPadding(0, 8, 0, 8)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setOnClickListener {
                                // Ao clicar, abre em tela cheia passando a url da imagem
                                val intent = Intent(this@PhotosActivity, PhotoFullScreenActivity::class.java)
                                intent.putExtra("imageUrl", url.toString())
                                startActivity(intent)
                            }
                        }
                        Glide.with(this).load(url.toString()).into(imageView)
                        container.addView(imageView)
                    }

                    layoutListPhotos.addView(container) // Adiciona container com fotos à lista
                }
            }
    }

    private fun negativeMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#F3787A"))
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun positiveMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#78F37A"))
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun goToMenu(){
        startActivity(Intent(this,MenuActivity::class.java))
        finish()
    }
}
