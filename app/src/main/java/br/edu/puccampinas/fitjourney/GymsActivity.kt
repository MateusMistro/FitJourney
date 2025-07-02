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
import br.edu.puccampinas.fitjourney.databinding.ActivityGymsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityGymsBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

class GymsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGymsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        carregarAcademiasDoUsuario()

        binding.comeBack.setOnClickListener {
            startActivity(Intent(this,MenuActivity::class.java))
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun carregarAcademiasDoUsuario() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("gyms")
                .whereEqualTo("UserId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            // Percorre os campos academia1, academia2, etc.
                            document.data.forEach { (key, value) ->
                                if (key.startsWith("academia")) {
                                    val nomeAcademia = value.toString()
                                    criarBotaoAcademia(nomeAcademia)
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Nenhuma academia encontrada.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao buscar academias: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun criarBotaoAcademia(nomeAcademia: String) {
        val botao = Button(this).apply {
            text = nomeAcademia
            setBackgroundResource(R.drawable.borda)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.corPrincipal)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 25f

            val params = LinearLayout.LayoutParams(
                300.dpToPx(),
                55.dpToPx()
            ).apply {
                topMargin = 70.dpToPx() // ou 150.dpToPx() se for o primeiro botão
                gravity = Gravity.CENTER_HORIZONTAL
            }
            layoutParams = params

            setOnClickListener {
                Toast.makeText(this@GymsActivity, "Clicou em $nomeAcademia", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@GymsActivity, TrainingsActivity::class.java)
                intent.putExtra("academiaSelecionada", nomeAcademia)
                startActivity(intent)

            }
        }

        binding.layoutButtons.addView(botao)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun goToMenu(){
        startActivity(Intent(this,MenuActivity::class.java))
        finish()
    }
}
