package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityTrainingsBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

class TrainingsActivity : AppCompatActivity() {
    private lateinit var academiaSelecionada: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        academiaSelecionada = intent.getStringExtra("academiaSelecionada") ?: ""

        if (academiaSelecionada.isEmpty()) {
            Toast.makeText(this, "Academia não selecionada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        buscarTreinosDoUsuario()

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this,GymsActivity::class.java))
            finish()
        }
    }

    private fun buscarTreinosDoUsuario() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("trainings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("academia", academiaSelecionada)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val treinosSet = mutableSetOf<String>()

                        for (document in documents) {
                            val treino = document.getString("letraTreino")
                            treino?.let {
                                treinosSet.add(it)
                            }
                        }

                        criarBotoesParaTreinos(treinosSet)
                    } else {
                        Toast.makeText(this, "Nenhum treino cadastrado para essa academia.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun criarBotoesParaTreinos(treinos: Set<String>) {
        val layout = binding.layoutTreinos

        for (treino in treinos.sorted()) {
            val button = Button(this)
            button.text = "Treino $treino"

            val params = LinearLayout.LayoutParams(
                300.dpToPx(),
                55.dpToPx()
            )
            params.gravity = Gravity.CENTER
            params.topMargin = 70.dpToPx()
            button.layoutParams = params

            button.setBackgroundResource(R.drawable.borda)
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.corPrincipal))
            button.setTextColor(ContextCompat.getColor(this, R.color.black))
            button.textSize = 25f

            button.setOnClickListener {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("academia", academiaSelecionada)
                intent.putExtra("treino", treino)
                startActivity(intent)
            }

            layout.addView(button)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
