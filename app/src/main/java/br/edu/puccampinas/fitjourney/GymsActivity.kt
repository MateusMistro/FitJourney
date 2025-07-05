package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityGymsBinding
import com.google.android.material.snackbar.Snackbar
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

        loadUserGyms()

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    // Função para buscar academias do usuário autenticado no Firestore
    private fun loadUserGyms() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("gyms")
                .whereEqualTo("UserId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            document.data.forEach { (key, value) ->
                                if (key.startsWith("academia")) {
                                    val gymName = value.toString()
                                    createGymButton(gymName)
                                }
                            }
                        }
                    } else {
                        negativeMessage("Nenhuma academia encontrada.")
                    }
                }
                .addOnFailureListener { e ->
                    negativeMessage("Erro ao buscar academias: ${e.message}")
                }
        } else {
            negativeMessage("Usuário não autenticado")
        }
    }

    // Cria dinamicamente um botão para cada academia cadastrada
    private fun createGymButton(gymName: String) {
        val button = Button(this).apply {
            text = gymName // Nome da academia no texto do botão
            setBackgroundResource(R.drawable.borda) // Aplica uma borda personalizada
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.corPrincipal)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            textSize = 25f

            // Define tamanho e margem do botão
            val params = LinearLayout.LayoutParams(
                300.dpToPx(),
                55.dpToPx()
            ).apply {
                topMargin = 70.dpToPx() // margem superior
                gravity = Gravity.CENTER_HORIZONTAL
            }
            layoutParams = params

            setOnClickListener {
                val intent = Intent(this@GymsActivity, TrainingsActivity::class.java)
                intent.putExtra("academiaSelecionada", gymName)
                startActivity(intent)
            }
        }

        // Adiciona o botão ao layout da tela
        binding.layoutButtons.addView(button)
    }

    // Extensão para converter dp em pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
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

    private fun comeBack() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }
}
