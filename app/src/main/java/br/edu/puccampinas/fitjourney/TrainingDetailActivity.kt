package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
    private val exercicesFild = mutableListOf<Triple<String, EditText, EditText>>() // nome, peso, reps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val gym = intent.getStringExtra("academia")
        val trainingLetter = intent.getStringExtra("treino")

        binding.trainingTv.text = "Treino " + trainingLetter

        if (userId == null || gym == null || trainingLetter == null) {
            negativeMessage("Dados insuficientes.")
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

                    val list = doc.get("exercicios") as? List<Map<String, Any>>
                    list?.forEach { exerciceMap ->
                        val name = exerciceMap["nome"].toString()
                        val weight = (exerciceMap["peso"] as Long).toInt()
                        val reps = (exerciceMap["repeticoes"] as Long).toInt()
                        createFildExercicies(name, weight, reps)
                    }

                } else {
                    negativeMessage("Treino não encontrado.")
                }
            }

        binding.btnSave.setOnClickListener {
            save()
        }

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun createFildExercicies(name: String, weight: Int, reps: Int) {
        val context = this

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.borda)
            setPadding(24, 24, 24, 24)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 24, 0, 60) // Espaço entre os cartões
            layoutParams = params
        }

        val title = TextView(context).apply {
            text = name.uppercase()
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val weightInput = EditText(context).apply {
            hint = "Peso(kg):"
            setHint("Peso(kg): " + weight.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val repsInput = EditText(context).apply {
            hint = "Repetições:"
            setHint("Repetições: " + reps.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        exercicesFild.add(Triple(name, weightInput, repsInput))

        // Adiciona os elementos ao container do exercício
        container.addView(title)
        container.addView(weightInput)
        container.addView(repsInput)

        // Adiciona o container ao layout principal
        binding.layoutExercises.addView(container)
    }

    private fun save() {
        db.collection("trainings")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                val originalExercices = document.get("exercicios") as? List<Map<String, Any>> ?: return@addOnSuccessListener

                val newExercices = mutableListOf<Map<String, Any>>()

                for ((index, triple) in exercicesFild.withIndex()) {
                    val name = triple.first
                    val weightStr = triple.second.text.toString().trim()
                    val repsStr = triple.third.text.toString().trim()

                    // Verificação se os campos estão preenchidos
                    if (weightStr.isEmpty() || repsStr.isEmpty()) {
                        negativeMessage("Preencha todos os campos de peso e repetições.")
                        return@addOnSuccessListener
                    }

                    // Verificação se são números válidos
                    val newWeight = weightStr.toIntOrNull()
                    val newReps = repsStr.toIntOrNull()

                    if (newWeight == null || newReps == null) {
                        negativeMessage("Insira apenas números válidos.")
                        return@addOnSuccessListener
                    }

                    val original = originalExercices.getOrNull(index)
                    val oldWeight = (original?.get("peso") as? Long)?.toInt() ?: 0
                    val oldReps = (original?.get("repeticoes") as? Long)?.toInt() ?: 0

                    val shouldActualize = newWeight > oldWeight ||
                            (newWeight == oldWeight && newReps > oldReps)

                    val finalExercice = if (shouldActualize) {
                        mapOf("nome" to name, "peso" to newWeight, "repeticoes" to newReps)
                    } else {
                        mapOf("nome" to name, "peso" to oldWeight, "repeticoes" to oldReps)
                    }

                    newExercices.add(finalExercice)
                }

                db.collection("trainings")
                    .document(docId)
                    .update("exercicios", newExercices)
                    .addOnSuccessListener {
                        positiveMessage("Exercícios atualizados com sucesso.")
                    }
                    .addOnFailureListener {
                        negativeMessage("Erro ao atualizar exercícios: ${it.message}")
                    }
            }
            .addOnFailureListener {
                negativeMessage("Erro ao buscar treino atual: ${it.message}")
            }
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

    private fun goToMenu(){
        startActivity(Intent(this,MenuActivity::class.java))
        finish()
    }
}
