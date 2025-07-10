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
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrainingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrainingsBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var selectedGym: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTrainingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        selectedGym = intent.getStringExtra("academiaSelecionada") ?: ""

        if (selectedGym.isEmpty()) {
            showNegativeMessage("Academia não selecionada")
            finish()
            return
        }

        fetchUsersWorkouts()

        binding.comeBack.setOnClickListener { goBack() }
        binding.menu.setOnClickListener { goToMenu() }
    }

    // Busca treinos do usuário na academia selecionada
    private fun fetchUsersWorkouts() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            showNegativeMessage("Usuário não autenticado")
            return
        }

        db.collection("trainings")
            .whereEqualTo("userId", userId)
            .whereEqualTo("academia", selectedGym)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    showNegativeMessage("Nenhum treino cadastrado para essa academia.")
                    return@addOnSuccessListener
                }

                val trainingsSet = documents.mapNotNull { it.getString("letraTreino") }.toSet()

                createTrainingButtons(trainingsSet)
            }
            .addOnFailureListener { exception ->
                showNegativeMessage("Erro: ${exception.message}")
            }
    }

    // Cria botões para cada treino disponível
    private fun createTrainingButtons(trainings: Set<String>) {
        val layout = binding.layoutTrainings
        layout.removeAllViews() // Limpa antes de adicionar novos botões

        trainings.sorted().forEach { training ->
            val button = Button(this).apply {
                text = "Treino $training"
                layoutParams = LinearLayout.LayoutParams(
                    300.dpToPx(),
                    55.dpToPx()
                ).apply {
                    gravity = Gravity.CENTER
                    topMargin = 70.dpToPx()
                }
                setBackgroundResource(R.drawable.borda)
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.corPrincipal)
                setTextColor(ContextCompat.getColor(context, R.color.black))
                textSize = 25f

                setOnClickListener {
                    val intent = Intent(this@TrainingsActivity, TrainingDetailActivity::class.java).apply {
                        putExtra("academia", selectedGym)
                        putExtra("treino", training)
                    }
                    startActivity(intent)
                }
            }
            layout.addView(button)
        }
    }

    // Extensão para converter dp em px
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    private fun goBack() {
        startActivity(Intent(this, GymsActivity::class.java))
        finish()
    }

    private fun showNegativeMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#F3787A"))
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun showPositiveMessage(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#78F37A"))
            .setTextColor(Color.WHITE)
            .show()
    }
}
