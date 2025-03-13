package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTreinoDactivityBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

private lateinit var binding: ActivityTreinoDactivityBinding
private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class TreinoDActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTreinoDactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, AcademiasActivity::class.java))
            finish()
        }

        val academia = intent.getStringExtra("academia")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtém o ID do usuário conectado
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Consulta a coleção "workoutD" onde o campo "UserId" é igual ao ID do usuário conectado
            db.collection("WorkoutD")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        setEditTextHints(documents)
                    } else {
                        mensagemNegativa(binding.root, userId)
                    }
                }
                .addOnFailureListener { exception ->
                    mensagemNegativa(
                        binding.root,
                        "Erro ao consultar documentos: ${exception.message}"
                    )
                }
        } else {
            mensagemNegativa(binding.root, "Usuário não está autenticado.")
        }

        binding.btnSave.setOnClickListener {
            saveData()
        }
    }

    private fun setEditTextHints(documents: QuerySnapshot) {

        for (document in documents) {

            val field1 = document.getString("BicepsInclinado") ?: ""
            val field2 = document.getString("RoscaMartelo") ?: ""
            val field3 = document.getString("RoscaScott") ?: ""
            val field4 = document.getString("TricepsFrances") ?: ""
            val field5 = document.getString("TricepsCorda") ?: ""
            val field6 = document.getString("TricepsTesta") ?: ""

            binding.Exercice1.hint = "Biceps Inclinado: $field1"
            binding.Exercice2.hint = "Rosca Martelo: $field2"
            binding.Exercice3.hint = "Rosca Scott: $field3"
            binding.Exercice4.hint = "Triceps Francês: $field4"
            binding.Exercice5.hint = "Triceps Corda: $field5"
            binding.Exercice6.hint = "Triceps Testa: $field6"
        }
    }

    private fun saveData() {
        // Obtém o valor da variável 'academia' da Intent
        val academia = intent.getStringExtra("academia") ?: ""

        // Obtém os valores dos campos e faz o trim para remover espaços extras
        val BicepsInclinado = binding.Exercice1.text.toString().trim()
        val RoscaMartelo = binding.Exercice2.text.toString().trim()
        val RoscaScott = binding.Exercice3.text.toString().trim()
        val TricepsFrances = binding.Exercice4.text.toString().trim()
        val TricepsCorda = binding.Exercice5.text.toString().trim()
        val TricepsTesta = binding.Exercice6.text.toString().trim()

        // Verifica se todos os campos estão preenchidos
        if (BicepsInclinado.isEmpty() ||
            RoscaMartelo.isEmpty() ||
            RoscaScott.isEmpty() ||
            TricepsFrances.isEmpty() ||
            TricepsCorda.isEmpty() ||
            TricepsTesta.isEmpty()) {

            // Exibe uma mensagem de erro se algum campo estiver vazio
            mensagemNegativa(binding.root, "Por favor, preencha todos os campos.")
            return
        }

        // Converte os valores dos campos para números (partindo do pressuposto que estão no formato "número/número")
        val (BicepsInclinadoFirst, BicepsInclinadoSecond) = parseNumbers(BicepsInclinado)
        val (RoscaMarteloFirst, RoscaMarteloSecond) = parseNumbers(RoscaMartelo)
        val (RoscaScottFirst, RoscaScottSecond) = parseNumbers(RoscaScott)
        val (TricepsFrancesFirst, TricepsFrancesSecond) = parseNumbers(TricepsFrances)
        val (TricepsCordaFirst, TricepsCordaSecond) = parseNumbers(TricepsCorda)
        val (TricepsTestaFirst, TricepsTestaSecond) = parseNumbers(TricepsTesta)

        // Obtém o ID do usuário autenticado
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Cria o mapa de dados a ser salvo no Firestore
            val data = mapOf(
                "Treino" to "Treino D",
                "Academia" to academia,
                "BicepsInclinado" to BicepsInclinado,
                "RoscaMartelo" to RoscaMartelo,
                "RoscaScott" to RoscaScott,
                "TricepsFrances" to TricepsFrances,
                "TricepsCorda" to TricepsCorda,
                "TricepsTesta" to TricepsTesta,
                "UserId" to userId,
                "Data" to Date()
            )

            // Adiciona o documento à coleção "Treinos"
            db.collection("Workouts")
                .add(data)
                .addOnSuccessListener {
                    mensagemPositiva(binding.root, "Dados salvos com sucesso.")
                }
                .addOnFailureListener { exception ->
                    mensagemNegativa(binding.root, "Erro ao salvar dados: ${exception.message}")
                }
        } else {
            mensagemNegativa(binding.root, "Usuário não está autenticado.")
        }


        if (userId != null) {
            // Consulta a coleção "WorkoutA" com base no "UserId" e "Academia"
            db.collection("WorkoutD")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {

                            // Obtém os dados existentes do documento
                            val existingBicepsInclinado = document.getString("BicepsInclinado") ?: ""
                            val existingRoscaMartelo = document.getString("RoscaMartelo") ?: ""
                            val existingRoscaScott = document.getString("RoscaScott") ?: ""
                            val existingTricepsFrances = document.getString("TricepsFrances") ?: ""
                            val existingTricepsCorda = document.getString("TricepsCorda") ?: ""
                            val existingTricepsTesta = document.getString("TricepsTesta") ?: ""

                            // Converte os dados existentes para números
                            val (existingBicepsInclinadoFirst, existingBicepsInclinadoSecond) = parseNumbers(existingBicepsInclinado)
                            val (existingRoscaMarteloFirst, existingRoscaMarteloSecond) = parseNumbers(existingRoscaMartelo)
                            val (existingRoscaScottFirst, existingRoscaScottSecond) = parseNumbers(existingRoscaScott)
                            val (existingTricepsFrancesFirst, existingTricepsFrancesSecond) = parseNumbers(existingTricepsFrances)
                            val (existingTricepsCordaFirst, existingTricepsCordaSecond) = parseNumbers(existingTricepsCorda)
                            val (existingTricepsTestaFirst, existingTricepsTestaSecond) = parseNumbers(existingTricepsTesta)

                            var novoBicepsInclinado = existingBicepsInclinado
                            var novoRoscaMartelo = existingRoscaMartelo
                            var novoRoscaScott = existingRoscaScott
                            var novoTricepsFrances = existingTricepsFrances
                            var novoTricepsCorda = existingTricepsCorda
                            var novoTricepsTesta = existingTricepsTesta

                            // Verifica se os novos valores são maiores ou iguais

                            if(shouldUpdate(existingBicepsInclinadoFirst, existingBicepsInclinadoSecond, BicepsInclinadoFirst, BicepsInclinadoSecond)){
                                novoBicepsInclinado = BicepsInclinado
                            }
                            if(shouldUpdate(existingRoscaMarteloFirst, existingRoscaMarteloSecond, RoscaMarteloFirst, RoscaMarteloSecond)){
                                novoRoscaMartelo = RoscaMartelo
                            }
                            if(shouldUpdate(existingRoscaScottFirst, existingRoscaScottSecond, RoscaScottFirst, RoscaScottSecond)){
                                novoRoscaScott = RoscaScott
                            }
                            if(shouldUpdate(existingTricepsFrancesFirst, existingTricepsFrancesSecond, TricepsFrancesFirst, TricepsFrancesSecond)){
                                novoTricepsFrances = TricepsFrances
                            }
                            if(shouldUpdate(existingTricepsCordaFirst, existingTricepsCordaSecond, TricepsCordaFirst, TricepsCordaSecond)){
                                novoTricepsCorda = TricepsCorda
                            }
                            if(shouldUpdate(existingTricepsTestaFirst, existingTricepsTestaSecond, TricepsTestaFirst, TricepsTestaSecond) ){
                                novoTricepsTesta = TricepsTesta
                            }

                            // Cria o mapa de dados a ser salvo no Firestore
                            val data = mapOf(
                                "BicepsInclinado" to novoBicepsInclinado,
                                "RoscaMartelo" to novoRoscaMartelo,
                                "RoscaScott" to novoRoscaScott,
                                "TricepsFrances" to novoTricepsFrances,
                                "TricepsCorda" to novoTricepsCorda,
                                "TricepsTesta" to novoTricepsTesta
                            )

                            // Atualiza o documento na coleção "WorkoutD"
                            db.collection("WorkoutD")
                                .document(document.id)
                                .update(data)
                                .addOnSuccessListener {
                                    mensagemPositiva(binding.root, "Dados atualizados com sucesso.")
                                }
                                .addOnFailureListener { exception ->
                                    mensagemNegativa(binding.root, "Erro ao atualizar dados: ${exception.message}")
                                }

                        }
                    } else {
                        mensagemNegativa(binding.root, "Nenhum documento encontrado para atualizar.")
                    }
                }
                .addOnFailureListener { exception ->
                    mensagemNegativa(binding.root, "Erro ao consultar documentos: ${exception.message}")
                }
        } else {
            mensagemNegativa(binding.root, "Usuário não está autenticado.")
        }
    }

    // Função para converter a string do formato "número/número" para dois inteiros
    private fun parseNumbers(value: String): Pair<Int, Int> {
        val parts = value.split("/")
        return if (parts.size == 2) {
            try {
                Pair(parts[0].toInt(), parts[1].toInt())
            } catch (e: NumberFormatException) {
                Pair(0, 0) // Retorna 0, 0 em caso de erro de conversão
            }
        } else {
            Pair(0, 0) // Retorna 0, 0 se o formato estiver incorreto
        }
    }

    // Função para comparar dois pares de números
    private fun shouldUpdate(existingFirst: Int, existingSecond: Int, newFirst: Int, newSecond: Int): Boolean {
        // Se o primeiro número for maior, atualiza
        if (newFirst > existingFirst) {
            return true
        }

        // Se o primeiro número for igual e o segundo maior, atualiza
        if (newFirst == existingFirst && newSecond > existingSecond) {
            return true
        }

        // Se o primeiro número for menor, não atualiza nada
        return false
    }


    private fun mensagemNegativa(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun mensagemPositiva(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }
}
