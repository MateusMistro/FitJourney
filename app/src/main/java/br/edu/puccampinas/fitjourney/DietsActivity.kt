package br.edu.puccampinas.fitjourney

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityDietsBinding
import br.edu.puccampinas.fitjourney.databinding.ActivityMenuBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DietsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDietsBinding
    val PICK_PDF_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDietsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listarPDFs()

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }

        binding.btnAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, PICK_PDF_REQUEST)
        }
    }

    // Resultado do PDF escolhido
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val pdfUri = data.data
            pdfUri?.let { uploadPDF(it) }
        }
    }

    fun uploadPDF(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "pdfs/${System.currentTimeMillis()}.pdf"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { url ->

                    val fileNameOriginal = getFileNameFromUri(uri)
                    val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "sem_usuario"

                    // Salva metadados no Firestore na coleção "diets"
                    val pdfInfo = hashMapOf(
                        "nome" to fileNameOriginal,
                        "url" to url.toString(),
                        "UserId" to userId,
                        "data" to dataAtual
                    )

                    FirebaseFirestore.getInstance().collection("diets")
                        .add(pdfInfo)
                        .addOnSuccessListener {
                            Toast.makeText(this, "PDF enviado com sucesso!", Toast.LENGTH_SHORT).show()
                            listarPDFs()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Falha ao salvar metadados", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao enviar PDF", Toast.LENGTH_SHORT).show()
            }
    }

    fun listarPDFs() {
        val pdfList = mutableListOf<Triple<String, String, String>>() // nome, url, data
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("diets")
            .whereEqualTo("UserId", userId) // só PDFs do usuário atual
            .get()
            .addOnSuccessListener { documents ->
                val docsList = documents.documents.reversed()
                for (doc in docsList) {
                    val nome = doc.getString("nome") ?: "Sem nome"
                    val url = doc.getString("url") ?: ""
                    val data = doc.getString("data") ?: "Data desconhecida"
                    pdfList.add(Triple(nome, url, data))
                }

                mostrarListaPDFs(pdfList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar PDFs", Toast.LENGTH_SHORT).show()
            }
    }

    fun mostrarListaPDFs(lista: List<Triple<String, String, String>>) {
        val layout = binding.layoutDiets
        layout.removeAllViews()

        for ((nome, url, data) in lista) {
            // Card container
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = getDrawable(R.drawable.borda)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 24, 0, 60) // espaço entre cards
                layoutParams = params
            }

            // Nome do PDF
            val titulo = TextView(this).apply {
                text = nome
                textSize = 18f
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            // Data
            val dataTexto = TextView(this).apply {
                text = "Enviado em: $data"
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }

            // Botão "Ver PDF"
            val botao = TextView(this).apply {
                text = "Ver PDF"
                setTextColor(Color.BLUE)
                setPadding(0, 16, 0, 0)
                textSize = 16f
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(url), "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(intent)
                }
            }

            // Adiciona todos ao card
            card.addView(titulo)
            card.addView(dataTexto)
            card.addView(botao)

            // Adiciona o card à lista
            layout.addView(card)
        }
    }

    fun getFileNameFromUri(uri: Uri): String {
        var name = "PDF"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }


    private fun mensagemNegativa(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#F3787A"))
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun mensagemPositiva(msg: String) {
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
