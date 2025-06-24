package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTrainingRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityTrainingRegistrationBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

private var academias = listOf<String>()
private var quantidadeTreinos = 0

private var academiaAtual = 0
private var treinoAtual = 0

private val exerciciosList = mutableListOf<Triple<EditText, EditText, EditText>>()

class TrainingRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        academias = intent.getStringArrayListExtra("academias") ?: listOf()
        quantidadeTreinos = intent.getIntExtra("quantidadeTreinos", 0)

        configurarTela()

        binding.btnAdicionarExercicio.setOnClickListener {
            adicionarCampoExercicio()
        }

        binding.btnSalvarTreino.setOnClickListener {
            salvarTreino()
        }
    }

    private fun configurarTela() {
        binding.txtTitulo.text = "Academia: ${academias[academiaAtual]} - Treino ${'A' + treinoAtual}"
        binding.layoutExercicios.removeAllViews()
        exerciciosList.clear()
        adicionarCampoExercicio() // Começa com pelo menos um
    }

    private fun adicionarCampoExercicio() {
        val layoutHorizontal = LinearLayout(this).apply {
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
        val editNome = EditText(this).apply {
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
        val editPeso = EditText(this).apply {
            hint = "Peso"
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
        layoutHorizontal.addView(editNome)
        layoutHorizontal.addView(editPeso)
        layoutHorizontal.addView(editReps)

        binding.layoutExercicios.addView(layoutHorizontal)

        exerciciosList.add(Triple(editNome, editPeso, editReps))
    }


    private fun salvarTreino() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val listaExercicios = exerciciosList.mapNotNull { (nomeField, pesoField, repField) ->
            val nome = nomeField.text.toString().trim()
            val peso = pesoField.text.toString().trim().toIntOrNull()
            val repeticoes = repField.text.toString().trim().toIntOrNull()

            if (nome.isNotEmpty() && peso != null && repeticoes != null) {
                mapOf(
                    "nome" to nome,
                    "peso" to peso,
                    "repeticoes" to repeticoes
                )
            } else {
                null
            }
        }

        if (listaExercicios.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um exercício completo", Toast.LENGTH_SHORT).show()
            return
        }

        val treinoData = hashMapOf(
            "userId" to userId,
            "academia" to academias[academiaAtual],
            "letraTreino" to ('A' + treinoAtual).toString(),
            "exercicios" to listaExercicios
        )

        db.collection("trainings")
            .add(treinoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Treino salvo com sucesso", Toast.LENGTH_SHORT).show()
                avancarTreinoOuAcademia()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun avancarTreinoOuAcademia() {
        if (treinoAtual < quantidadeTreinos - 1) {
            treinoAtual++
            configurarTela()
        } else if (academiaAtual < academias.size - 1) {
            treinoAtual = 0
            academiaAtual++
            configurarTela()
        } else {
            Toast.makeText(this, "Todos os treinos cadastrados!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }

    // Converte dp para px
    fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
