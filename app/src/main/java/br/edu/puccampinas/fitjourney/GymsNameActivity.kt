package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityGymsNameBinding
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
        val quantidadeTreinos = intent.getStringExtra("quantidadeTreinos")
        val quantidadeAcademias = intent.getStringExtra("quantidadeAcademias")
        val numAcademias = quantidadeAcademias?.toIntOrNull() ?: 0
        val layout = binding.Academias

        for (i in 1..numAcademias) {
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
            salvarNoFirestore(quantidadeTreinos)
        }
    }

    private fun salvarNoFirestore(quantidadeTreinos: String?) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val nomesAcademias = editTextList.map { it.text.toString().trim() }

            // Verifica se algum campo está vazio
            val algumCampoVazio = nomesAcademias.any { it.isEmpty() }
            if (algumCampoVazio) {
                Toast.makeText(this, "Preencha todos os nomes das academias", Toast.LENGTH_SHORT).show()
                return
            }

            // Monta o mapa de dados
            val gymData = hashMapOf<String, Any>(
                "UserId" to userId
            )

            // Adiciona os campos academia1, academia2, etc.
            for ((index, nome) in nomesAcademias.withIndex()) {
                val key = "academia${index + 1}"
                gymData[key] = nome
            }

            // Salva no Firestore
            db.collection("gyms")
                .add(gymData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TrainingRegistrationActivity::class.java)
                    intent.putStringArrayListExtra("academias", ArrayList(nomesAcademias))
                    if (quantidadeTreinos != null) {
                        intent.putExtra("quantidadeTreinos", quantidadeTreinos.toInt())
                    }
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

}
