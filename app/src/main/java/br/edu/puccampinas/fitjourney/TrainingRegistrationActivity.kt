package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingRegistrationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrainingRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrainingRegistrationBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var gyms = listOf<String>()
    private var trainingQuantity = 0

    private var actualGym = 0
    private var actualWorkout = 0

    // Lista de triples que guardam os campos: nome, peso e repetições
    private val exercisesList = mutableListOf<Triple<EditText, EditText, EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTrainingRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        gyms = intent.getStringArrayListExtra("academias") ?: emptyList()
        trainingQuantity = intent.getIntExtra("quantidadeTreinos", 0)

        if (gyms.isEmpty() || trainingQuantity <= 0) {
            showNegativeMessage("Dados de academias ou quantidade de treinos inválidos.")
            finish()
            return
        }

        setupScreen()

        binding.btnAddExercise.setOnClickListener {
            addExerciseField()
        }

        binding.btnSaveTraining.setOnClickListener {
            saveTraining()
        }
    }

    private fun setupScreen() {
        binding.txtTitle.text = "Academia: ${gyms[actualGym]} - Treino ${'A' + actualWorkout}"
        binding.layoutExercises.removeAllViews()
        exercisesList.clear()
        addExerciseField() // Começa com pelo menos um campo de exercício
    }

    private fun addExerciseField() {
        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20.dpToPx()
                marginStart = 10.dpToPx()
                marginEnd = 10.dpToPx()
            }
        }

        val editName = EditText(this).apply {
            hint = "Ex: Supino"
            layoutParams = LinearLayout.LayoutParams(0, 50.dpToPx(), 2f).apply {
                marginEnd = 8.dpToPx()
            }
            setBackgroundResource(R.drawable.borda)
            setPadding(12.dpToPx(), 0, 0, 0)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.black))
        }

        val editWeight = EditText(this).apply {
            hint = "Peso(Kg)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, 50.dpToPx(), 1f).apply {
                marginEnd = 8.dpToPx()
            }
            setBackgroundResource(R.drawable.borda)
            setPadding(12.dpToPx(), 0, 0, 0)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.black))
        }

        val editReps = EditText(this).apply {
            hint = "Reps"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, 50.dpToPx(), 1f)
            setBackgroundResource(R.drawable.borda)
            setPadding(12.dpToPx(), 0, 0, 0)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.black))
        }

        horizontalLayout.addView(editName)
        horizontalLayout.addView(editWeight)
        horizontalLayout.addView(editReps)

        binding.layoutExercises.addView(horizontalLayout)

        exercisesList.add(Triple(editName, editWeight, editReps))
    }

    private fun saveTraining() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            showNegativeMessage("Usuário não autenticado")
            return
        }

        val exercisesData = mutableListOf<Map<String, Any>>()

        for ((index, triple) in exercisesList.withIndex()) {
            val name = triple.first.text.toString().trim()
            val weightStr = triple.second.text.toString().trim()
            val repsStr = triple.third.text.toString().trim()

            if (name.isEmpty() || weightStr.isEmpty() || repsStr.isEmpty()) {
                showNegativeMessage("Preencha todos os campos do exercício ${index + 1}")
                return
            }

            val weight = weightStr.toIntOrNull()
            val reps = repsStr.toIntOrNull()

            if (weight == null || reps == null || weight <= 0 || reps <= 0) {
                showNegativeMessage("Valores inválidos no exercício ${index + 1}. Use números maiores que zero.")
                return
            }

            exercisesData.add(
                mapOf(
                    "nome" to name,
                    "peso" to weight,
                    "repeticoes" to reps
                )
            )
        }

        if (exercisesData.isEmpty()) {
            showNegativeMessage("Adicione pelo menos um exercício.")
            return
        }

        val trainingData = hashMapOf(
            "userId" to userId,
            "academia" to gyms[actualGym],
            "letraTreino" to ('A' + actualWorkout).toString(),
            "exercicios" to exercisesData
        )

        db.collection("trainings")
            .add(trainingData)
            .addOnSuccessListener {
                showPositiveMessage("Treino salvo com sucesso!")
                advanceTrainingOrGym()
            }
            .addOnFailureListener { e ->
                showNegativeMessage("Erro ao salvar treino: ${e.message}")
            }
    }

    private fun advanceTrainingOrGym() {
        if (actualWorkout < trainingQuantity - 1) {
            actualWorkout++
        } else if (actualGym < gyms.size - 1) {
            actualWorkout = 0
            actualGym++
        } else {
            showPositiveMessage("Todos os treinos cadastrados!")
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        setupScreen()
    }

    // Extensão para conversão dp -> px
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

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
