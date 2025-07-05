package br.edu.puccampinas.fitjourney

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityDietsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DietsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDietsBinding
    val PICK_PDF_REQUEST = 1 // Código para identificar retorno da seleção de PDF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDietsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listPDFs()

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }

        binding.btnAdd.setOnClickListener {
            startUpload()
        }
    }

    // Trata o retorno da escolha do arquivo PDF
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val pdfUri = data.data // URI do arquivo escolhido
            pdfUri?.let { uploadPDF(it) } // Faz upload se não for nulo
        }
    }

    // Abre seletor de arquivos para PDF
    fun startUpload() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_REQUEST)
    }

    fun uploadPDF(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "pdfs/${System.currentTimeMillis()}.pdf"
        val fileRef = storageRef.child(fileName)

        fileRef.putFile(uri)
            .addOnSuccessListener {
                // Obtém URL de download após upload
                fileRef.downloadUrl.addOnSuccessListener { url ->
                    val originalFileName = getFileNameFromUri(uri)
                    val actualDate = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "sem_usuario"

                    val pdfInfo = hashMapOf(
                        "nome" to originalFileName,
                        "url" to url.toString(),
                        "UserId" to userId,
                        "data" to actualDate
                    )

                    FirebaseFirestore.getInstance().collection("diets")
                        .add(pdfInfo)
                        .addOnSuccessListener {
                            positiveMessage("PDF enviado com sucesso!")
                            listPDFs() // Recarrega a lista
                        }
                        .addOnFailureListener {
                            negativeMessage("Falha ao salvar metadados")
                        }
                }
            }
            .addOnFailureListener {
                negativeMessage("Falha ao enviar PDF")
            }
    }

    // Lista os PDFs enviados pelo usuário atual
    fun listPDFs() {
        val pdfList = mutableListOf<Triple<String, String, String>>() // Lista com nome, url e data
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("diets")
            .whereEqualTo("UserId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val docsList = documents.documents.reversed() // Mostra do mais recente ao mais antigo
                for (doc in docsList) {
                    val name = doc.getString("nome") ?: "Sem nome"
                    val url = doc.getString("url") ?: ""
                    val date = doc.getString("data") ?: "Data desconhecida"
                    pdfList.add(Triple(name, url, date))
                }

                showPDFsList(pdfList)
            }
            .addOnFailureListener {
                negativeMessage("Erro ao carregar PDF")
            }
    }

    fun showPDFsList(list: List<Triple<String, String, String>>) {
        val layout = binding.layoutDiets
        layout.removeAllViews() // Limpa visualizações antigas

        for ((name, url, date) in list) {
            // Cria container estilo card
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = getDrawable(R.drawable.borda)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 24, 0, 60)
                layoutParams = params
            }

            val title = TextView(this).apply {
                text = name
                textSize = 18f
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val textDate = TextView(this).apply {
                text = "Enviado em: $date"
                textSize = 16f
                setTextColor(Color.DKGRAY)
            }

            val button = TextView(this).apply {
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

            // Adiciona os elementos ao card
            card.addView(title)
            card.addView(textDate)
            card.addView(button)

            // Adiciona o card à lista
            layout.addView(card)
        }
    }

    // Obtém o nome do arquivo PDF a partir da URI
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
}
