package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingRegistrationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityTrainingRegistrationBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

private var gyms = listOf<String>()
private var trainingQuantity = 0

private var actualGym = 0
private var actualWorkout = 0

private val exercicesList = mutableListOf<Triple<EditText, EditText, EditText>>()

class TrainingRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        gyms = intent.getStringArrayListExtra("academias") ?: listOf()
        trainingQuantity = intent.getIntExtra("quantidadeTreinos", 0)

        setUpScreen()

        binding.btnAddExercise.setOnClickListener {
            addFieldExercice()
        }

        binding.btnSaveTraining.setOnClickListener {
            saveTraining()
        }
    }

    private fun setUpScreen() {
        binding.txtTitle.text = "Academia: ${gyms[actualGym]} - Treino ${'A' + actualWorkout}"
        binding.layoutExercises.removeAllViews()
        exercicesList.clear()
        addFieldExercice() // Começa com pelo menos um
    }

    private fun addFieldExercice() {
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

        // Campo Nome do Exercício
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

        // Campo Peso
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

        // Campo Repetições
        val editReps = EditText(this).apply {
            hint = "Reps"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, 50.dpToPx(), 1f)
            setBackgroundResource(R.drawable.borda)
            setPadding(12.dpToPx(), 0, 0, 0)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // Adiciona no layout horizontal
        horizontalLayout.addView(editName)
        horizontalLayout.addView(editWeight)
        horizontalLayout.addView(editReps)

        binding.layoutExercises.addView(horizontalLayout)

        exercicesList.add(Triple(editName, editWeight, editReps))
    }

    private fun saveTraining() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            negativeMessage("Usuário não autenticado")
            return
        }

        val listOfExercices = mutableListOf<Map<String, Any>>()

        for ((index, triple) in exercicesList.withIndex()) {
            val name = triple.first.text.toString().trim()
            val weightStr = triple.second.text.toString().trim()
            val repsStr = triple.third.text.toString().trim()

            // Verifica se todos os campos estão preenchidos
            if (name.isEmpty() || weightStr.isEmpty() || repsStr.isEmpty()) {
                negativeMessage("Preencha todos os campos do exercício ${index + 1}")
                return
            }

            // Converte peso e repetições em número
            val weigth = weightStr.toIntOrNull()
            val repetitions = repsStr.toIntOrNull()

            if (weigth == null || repetitions == null || weigth <= 0 || repetitions <= 0) {
                negativeMessage("Insira valores válidos (números maiores que zero) no exercício ${index + 1}")
                return
            }

            listOfExercices.add(
                mapOf(
                    "nome" to name,
                    "peso" to weigth,
                    "repeticoes" to repetitions
                )
            )
        }

        if (listOfExercices.isEmpty()) {
            negativeMessage("Adicione pelo menos um exercício")
            return
        }

        val trainingData = hashMapOf(
            "userId" to userId,
            "academia" to gyms[actualGym],
            "letraTreino" to ('A' + actualWorkout).toString(),
            "exercicios" to listOfExercices
        )

        db.collection("trainings")
            .add(trainingData)
            .addOnSuccessListener {
                positiveMessage("Treino salvo com sucesso")
                advanceTrainingOrGym()
            }
            .addOnFailureListener { e ->
                negativeMessage("Erro ao salvar treino: ${e.message}")
            }
    }

    private fun advanceTrainingOrGym() {
        if (actualWorkout < trainingQuantity - 1) {
            actualWorkout++
            setUpScreen()
        } else if (actualGym < gyms.size - 1) {
            actualWorkout = 0
            actualGym++
            setUpScreen()
        } else {
            positiveMessage("Todos os treinos cadastrados!")
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }

    // Converte dp para px
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
