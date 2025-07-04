package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityGymsNameBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityGymsNameBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

class GymsNameActivity : AppCompatActivity() {
    private val editTextList = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGymsNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val trainingQuantity = intent.getStringExtra("quantidadeTreinos")
        val gymsQuantity = intent.getStringExtra("quantidadeAcademias")
        val gymsNumber = gymsQuantity?.toIntOrNull() ?: 0
        val layout = binding.Gyms

        for (i in 1..gymsNumber) {
            val editText = EditText(this)
            editText.hint = "Nome da academia $i"

            // Define o tamanho e centraliza
            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.edittext_width), // 350dp
                resources.getDimensionPixelSize(R.dimen.edittext_height) // 50dp
            )
            params.topMargin = resources.getDimensionPixelSize(R.dimen.edittext_margin_top) // margem superior
            params.gravity = Gravity.CENTER // centraliza no LinearLayout

            editText.layoutParams = params

            // Outras configurações visuais
            editText.setBackgroundResource(R.drawable.borda)
            editText.setPadding(15.dpToPx(), 0, 0, 0) // padding left de 15dp
            editText.setTextColor(ContextCompat.getColor(this, R.color.black))
            editText.setHintTextColor(ContextCompat.getColor(this, R.color.black))

            // Adiciona na tela e na lista
            layout.addView(editText)
            editTextList.add(editText)
        }

        // Clique no botão salvar
        binding.btnSave.setOnClickListener {
            val filledFields = editTextList.all { it.text.toString().trim().isNotEmpty() }

            if (!filledFields) {
                negativeMessage("Preencha todos os nomes das academias")
                return@setOnClickListener
            }

            saveDataInFirestore(trainingQuantity)
        }

    }

    private fun saveDataInFirestore(trainingQuantity: String?) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val gymsName = editTextList.map { it.text.toString().trim() }

            // Verifica se algum campo está vazio
            val someEmptyField = gymsName.any { it.isEmpty() }
            if (someEmptyField) {
                negativeMessage("Preencha todos os nomes das academias")
                return
            }

            // Monta o mapa de dados
            val gymData = hashMapOf<String, Any>(
                "UserId" to userId
            )

            // Adiciona os campos academia1, academia2, etc.
            for ((index, name) in gymsName.withIndex()) {
                val key = "academia${index + 1}"
                gymData[key] = name
            }

            // Salva no Firestore
            db.collection("gyms")
                .add(gymData)
                .addOnSuccessListener {
                    positiveMessage("Dados salvos com sucesso!")
                    val intent = Intent(this, TrainingRegistrationActivity::class.java)
                    intent.putStringArrayListExtra("academias", ArrayList(gymsName))
                    if (trainingQuantity != null) {
                        intent.putExtra("quantidadeTreinos", trainingQuantity.toInt())
                    }
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    negativeMessage("Erro ao salvar: ${e.message}")
                }

        } else {
            negativeMessage("Usuário não autenticado")
        }
    }

    fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
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

}
