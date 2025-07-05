package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrainingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrainingDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var docId: String

    // Lista de triples: nome do exercício, campo peso e campo repetições
    private val exercisesFields = mutableListOf<Triple<String, EditText, EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTrainingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val gym = intent.getStringExtra("academia")
        val trainingLetter = intent.getStringExtra("treino")

        binding.trainingTv.text = "Treino $trainingLetter"

        if (userId == null || gym == null || trainingLetter == null) {
            showNegativeMessage("Dados insuficientes.")
            finish()
            return
        }

        db.collection("trainings")
            .whereEqualTo("userId", userId)
            .whereEqualTo("academia", gym)
            .whereEqualTo("letraTreino", trainingLetter)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.first()
                    docId = doc.id

                    val exercisesList = doc.get("exercicios") as? List<Map<String, Any>>
                    exercisesList?.forEach { exerciseMap ->
                        val name = exerciseMap["nome"].toString()
                        val weight = (exerciseMap["peso"] as? Long)?.toInt() ?: 0
                        val reps = (exerciseMap["repeticoes"] as? Long)?.toInt() ?: 0
                        createExerciseField(name, weight, reps)
                    }
                } else {
                    showNegativeMessage("Treino não encontrado.")
                }
            }
            .addOnFailureListener { e ->
                showNegativeMessage("Erro ao buscar treino: ${e.message}")
            }

        binding.btnSave.setOnClickListener {
            saveExercises()
        }

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun createExerciseField(name: String, weight: Int, reps: Int) {
        val context = this

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.borda)
            setPadding(24, 24, 24, 24)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 24, 0, 60)
            layoutParams = params
        }

        val title = TextView(context).apply {
            text = name.uppercase()
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val weightInput = EditText(context).apply {
            hint = "Peso (kg): $weight"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(weight.toString())
        }

        val repsInput = EditText(context).apply {
            hint = "Repetições: $reps"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(reps.toString())
        }

        // Guarda referência dos campos para salvar depois
        exercisesFields.add(Triple(name, weightInput, repsInput))

        container.addView(title)
        container.addView(weightInput)
        container.addView(repsInput)

        binding.layoutExercises.addView(container)
    }

    private fun saveExercises() {
        db.collection("trainings").document(docId)
            .get()
            .addOnSuccessListener { document ->
                val originalExercises = document.get("exercicios") as? List<Map<String, Any>>
                if (originalExercises == null) {
                    showNegativeMessage("Dados originais do treino não encontrados.")
                    return@addOnSuccessListener
                }

                val updatedExercises = mutableListOf<Map<String, Any>>()

                for ((index, triple) in exercisesFields.withIndex()) {
                    val name = triple.first
                    val weightStr = triple.second.text.toString().trim()
                    val repsStr = triple.third.text.toString().trim()

                    if (weightStr.isEmpty() || repsStr.isEmpty()) {
                        showNegativeMessage("Preencha todos os campos de peso e repetições.")
                        return@addOnSuccessListener
                    }

                    val newWeight = weightStr.toIntOrNull()
                    val newReps = repsStr.toIntOrNull()

                    if (newWeight == null || newReps == null) {
                        showNegativeMessage("Insira apenas números válidos.")
                        return@addOnSuccessListener
                    }

                    val original = originalExercises.getOrNull(index)
                    val oldWeight = (original?.get("peso") as? Long)?.toInt() ?: 0
                    val oldReps = (original?.get("repeticoes") as? Long)?.toInt() ?: 0

                    // Atualiza apenas se o novo peso ou repetições forem melhores
                    val shouldUpdate = newWeight > oldWeight || (newWeight == oldWeight && newReps > oldReps)

                    val finalExercise = if (shouldUpdate) {
                        mapOf("nome" to name, "peso" to newWeight, "repeticoes" to newReps)
                    } else {
                        mapOf("nome" to name, "peso" to oldWeight, "repeticoes" to oldReps)
                    }

                    updatedExercises.add(finalExercise)
                }

                db.collection("trainings").document(docId)
                    .update("exercicios", updatedExercises)
                    .addOnSuccessListener {
                        showPositiveMessage("Exercícios atualizados com sucesso.")
                    }
                    .addOnFailureListener { e ->
                        showNegativeMessage("Erro ao atualizar exercícios: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showNegativeMessage("Erro ao buscar treino atual: ${e.message}")
            }
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

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }
}
