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
    private val camposExercicios = mutableListOf<Triple<String, EditText, EditText>>() // nome, peso, reps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        val academia = intent.getStringExtra("academia")
        val letraTreino = intent.getStringExtra("treino")

        binding.trainingTv.text = "Treino " + letraTreino

        if (userId == null || academia == null || letraTreino == null) {
            mensagemNegativa("Dados insuficientes.")
            finish()
            return
        }

        db.collection("trainings")
            .whereEqualTo("userId", userId)
            .whereEqualTo("academia", academia)
            .whereEqualTo("letraTreino", letraTreino)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.first()
                    docId = doc.id

                    val lista = doc.get("exercicios") as? List<Map<String, Any>>
                    lista?.forEach { exercicioMap ->
                        val nome = exercicioMap["nome"].toString()
                        val peso = (exercicioMap["peso"] as Long).toInt()
                        val reps = (exercicioMap["repeticoes"] as Long).toInt()
                        criarCamposExercicio(nome, peso, reps)
                    }

                } else {
                    mensagemNegativa("Treino não encontrado.")
                }
            }

        binding.btnSave.setOnClickListener {
            salvar()
        }

        binding.comeBack.setOnClickListener {
            finish()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun criarCamposExercicio(nome: String, peso: Int, repeticoes: Int) {
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

        val titulo = TextView(context).apply {
            text = nome.uppercase()
            textSize = 16f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val pesoInput = EditText(context).apply {
            hint = "Peso(kg):"
            setHint("Peso(kg): " + peso.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val repsInput = EditText(context).apply {
            hint = "Repetições:"
            setHint("Repetições: " + repeticoes.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        camposExercicios.add(Triple(nome, pesoInput, repsInput))

        // Adiciona os elementos ao container do exercício
        container.addView(titulo)
        container.addView(pesoInput)
        container.addView(repsInput)

        // Adiciona o container ao layout principal
        binding.layoutExercises.addView(container)
    }

    private fun salvar() {
        db.collection("trainings")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                val exerciciosOriginais = document.get("exercicios") as? List<Map<String, Any>> ?: return@addOnSuccessListener

                val exerciciosAtualizados = mutableListOf<Map<String, Any>>()

                for ((index, triple) in camposExercicios.withIndex()) {
                    val nome = triple.first
                    val pesoStr = triple.second.text.toString().trim()
                    val repsStr = triple.third.text.toString().trim()

                    // Verificação se os campos estão preenchidos
                    if (pesoStr.isEmpty() || repsStr.isEmpty()) {
                        mensagemNegativa("Preencha todos os campos de peso e repetições.")
                        return@addOnSuccessListener
                    }

                    // Verificação se são números válidos
                    val novoPeso = pesoStr.toIntOrNull()
                    val novasReps = repsStr.toIntOrNull()

                    if (novoPeso == null || novasReps == null) {
                        mensagemNegativa("Insira apenas números válidos.")
                        return@addOnSuccessListener
                    }

                    val original = exerciciosOriginais.getOrNull(index)
                    val pesoAntigo = (original?.get("peso") as? Long)?.toInt() ?: 0
                    val repsAntigas = (original?.get("repeticoes") as? Long)?.toInt() ?: 0

                    val deveAtualizar = novoPeso > pesoAntigo ||
                            (novoPeso == pesoAntigo && novasReps > repsAntigas)

                    val exercicioFinal = if (deveAtualizar) {
                        mapOf("nome" to nome, "peso" to novoPeso, "repeticoes" to novasReps)
                    } else {
                        mapOf("nome" to nome, "peso" to pesoAntigo, "repeticoes" to repsAntigas)
                    }

                    exerciciosAtualizados.add(exercicioFinal)
                }

                db.collection("trainings")
                    .document(docId)
                    .update("exercicios", exerciciosAtualizados)
                    .addOnSuccessListener {
                        mensagemPositiva("Exercícios atualizados com sucesso.")
                    }
                    .addOnFailureListener {
                        mensagemNegativa("Erro ao atualizar exercícios: ${it.message}")
                    }
            }
            .addOnFailureListener {
                mensagemNegativa("Erro ao buscar treino atual: ${it.message}")
            }
    }


    private fun mensagemNegativa(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor("#F3787A"))
            .setTextColor(Color.WHITE)
            .show()
    }

    private fun mensagemPositiva(msg: String) {
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
