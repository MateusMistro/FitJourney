package br.edu.puccampinas.fitjourney

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityAnthropometricAssessmentBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class AnthropometricAssessmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnthropometricAssessmentBinding
    private lateinit var layoutEvaluatios: LinearLayout

    // Código de requisição para identificar retorno da seleção de arquivos
    private val PICK_FILES_REQUEST = 102

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide() // Oculta a ActionBar

        binding = ActivityAnthropometricAssessmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialização do Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Referência ao layout onde as avaliações serão listadas
        layoutEvaluatios = binding.layoutEvaluations

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }

        // Botão para adicionar (selecionar) arquivos
        binding.btnAdd.setOnClickListener {
            startUpload()
        }

        // Lista avaliações ao abrir a tela
        listReviews()
    }

    // Trata o retorno da seleção de arquivos
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILES_REQUEST && resultCode == Activity.RESULT_OK) {
            val uris = mutableListOf<Uri>()

            // Verifica se múltiplos arquivos foram selecionados
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            } ?: data?.data?.let {
                // Caso apenas um arquivo seja selecionado
                uris.add(it)
            }

            // Envia os arquivos selecionados
            if (uris.isNotEmpty()) {
                uploadFiles(uris)
            }
        }
    }

    // Inicia seleção de arquivos (imagens ou PDFs)
    private fun startUpload(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf"))
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Selecionar arquivos"), PICK_FILES_REQUEST)
    }

    // Faz upload dos arquivos selecionados para o Firebase Storage e salva URLs no Firestore
    private fun uploadFiles(files: List<Uri>) {
        val userId = auth.currentUser?.uid ?: return
        val actualDate = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
        val urls = mutableListOf<String>()

        val storageRef = storage.reference.child("avaliacoes")
        val total = files.size
        var finished = 0

        for (uri in files) {
            val fileName = "${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val fileRef = storageRef.child(fileName)

            // Faz upload do arquivo
            fileRef.putFile(uri).addOnSuccessListener {
                // Obtém a URL de download após upload
                fileRef.downloadUrl.addOnSuccessListener { url ->
                    urls.add(url.toString())
                    finished++

                    if (finished == total) {
                        val data = hashMapOf(
                            "UserId" to userId,
                            "data" to actualDate,
                            "urls" to urls
                        )
                        db.collection("anthropometricAssessments")
                            .add(data)
                            .addOnSuccessListener {
                                positiveMessage("Arquivos enviados com sucesso!")
                                listReviews()
                            }
                    }
                }
            }.addOnFailureListener {
                negativeMessage("Erro ao enviar arquivos.")
            }
        }
    }

    // Lista todas as avaliações já enviadas pelo usuário atual
    private fun listReviews() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("anthropometricAssessments")
            .whereEqualTo("UserId", userId)
            .get()
            .addOnSuccessListener { documents ->
                layoutEvaluatios.removeAllViews() // Limpa layout antes de listar
                val docsList = documents.documents.sortedByDescending { doc ->
                    val dateStr = doc.getString("data") ?: "0000-01-01"
                    LocalDate.parse(dateStr)
                }
                for (doc in docsList) {
                    val date = doc.getString("data") ?: "Sem data"
                    val urls = doc.get("urls") as? List<*> ?: continue

                    // Container visual de uma avaliação
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
                        text = "Avaliação de: ${formatDateBr(date)}"
                        textSize = 18f
                        setTextColor(Color.BLACK)
                        setTypeface(null, Typeface.BOLD)
                    }
                    container.addView(title)

                    for (url in urls) {
                        val urlStr = url.toString()
                        val fileName = Uri.parse(urlStr).lastPathSegment ?: ""

                        if (fileName.contains(".pdf", ignoreCase = true)) {
                            // Se for PDF, exibe botão de visualização
                            val buttonPdf = TextView(this).apply {
                                text = "Ver PDF"
                                setTextColor(Color.BLUE)
                                textSize = 16f
                                setPadding(0, 16, 0, 8)
                                setOnClickListener {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(Uri.parse(urlStr), "application/pdf")
                                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                    startActivity(intent)
                                }
                            }

                            val textDate = TextView(this).apply {
                                text = "Enviado em: ${formatDateBr(date)}"
                                textSize = 16f
                                setTextColor(Color.DKGRAY)
                            }

                            container.addView(textDate)
                            container.addView(buttonPdf)
                        } else {
                            // Se for imagem, exibe na tela com Glide
                            val imageView = ImageView(this).apply {
                                layoutParams = LinearLayout.LayoutParams(500, 500)
                                setPadding(0, 8, 0, 8)
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                setOnClickListener {
                                    val intent = Intent(this@AnthropometricAssessmentActivity, PhotoFullScreenActivity::class.java)
                                    intent.putExtra("imageUrl", url.toString())
                                    startActivity(intent)
                                }
                            }
                            Glide.with(this).load(urlStr).into(imageView)
                            container.addView(imageView)
                        }
                    }

                    layoutEvaluatios.addView(container)
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

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    fun formatDateBr(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // formato original
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // formato brasileiro
            val date = parser.parse(dateString)
            formatter.format(date!!)
        } catch (e: Exception) {
            dateString // retorna o original se falhar
        }
    }
}
