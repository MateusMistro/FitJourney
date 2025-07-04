package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityTrainingsBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

class TrainingsActivity : AppCompatActivity() {
    private lateinit var selectedGym: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        selectedGym = intent.getStringExtra("academiaSelecionada") ?: ""

        if (selectedGym.isEmpty()) {
            negativeMessage("Academia não selecionada")
            finish()
            return
        }

        fetchUsersWorkouts()

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun fetchUsersWorkouts() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("trainings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("academia", selectedGym)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val trainingsSet = mutableSetOf<String>()

                        for (document in documents) {
                            val training = document.getString("letraTreino")
                            training?.let {
                                trainingsSet.add(it)
                            }
                        }

                        createTrainingButtons(trainingsSet)
                    } else {
                        negativeMessage("Nenhum treino cadastrado para essa academia.")
                    }
                }
                .addOnFailureListener { exception ->
                    negativeMessage("Erro: ${exception.message}")
                }
        } else {
            negativeMessage("Usuário não autenticado")
        }
    }

    private fun createTrainingButtons(trainings: Set<String>) {
        val layout = binding.layoutTrainings

        for (training in trainings.sorted()) {
            val button = Button(this)
            button.text = "Treino $training"

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
                val intent = Intent(this, TrainingDetailActivity::class.java)
                intent.putExtra("academia", selectedGym)
                intent.putExtra("treino", training)
                startActivity(intent)
            }

            layout.addView(button)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun goToMenu(){
        startActivity(Intent(this,MenuActivity::class.java))
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

    private fun comeBack(){
        startActivity(Intent(this,GymsActivity::class.java))
        finish()
    }
}
