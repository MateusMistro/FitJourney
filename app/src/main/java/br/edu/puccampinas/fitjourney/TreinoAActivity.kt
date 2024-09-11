package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityTreinoAactivityBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

private lateinit var binding: ActivityTreinoAactivityBinding
private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class TreinoAActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTreinoAactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this,AcademiasActivity::class.java))
            finish()
        }

        val academia = intent.getStringExtra("academia")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtém o ID do usuário conectado
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Consulta a coleção "workoutA" onde o campo "UserId" é igual ao ID do usuário conectado
            db.collection("WorkoutA")
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
                    mensagemNegativa(binding.root, "Erro ao consultar documentos: ${exception.message}")
                }
        } else {
            mensagemNegativa(binding.root, "Usuário não está autenticado.")
        }

        binding.btnSave.setOnClickListener {
            saveData()
        }
    }

    private fun setEditTextHints(documents: QuerySnapshot) {

        val academia = intent.getStringExtra("academia")

        for (document in documents) {

            val field1 = document.getString("ExtensoraUnilateral") ?: ""
            val field2 = document.getString("AgachamentoHack") ?: ""
            val field3 = document.getString("LegPress") ?: ""
            val field4 = document.getString("MesaFlexora") ?: ""
            val field5 = document.getString("CadeiraFlexora") ?: ""
            val field6 = document.getString("PanturrilhaSentado") ?: ""

            val field7 = when (academia) {
                "SF" -> document.getString("PanturrilhaLegPress") ?: ""
                "FB" -> document.getString("PanturrilhaEmPe") ?: ""
                else -> ""
            }

            binding.Exercice1.hint = "Extensora Unilateral: $field1"
            binding.Exercice2.hint = "Agachamento Hack: $field2"
            binding.Exercice3.hint = "Leg Press: $field3"
            binding.Exercice4.hint = "Mesa Flexora: $field4"
            binding.Exercice5.hint = "Cadeira Flexora: $field5"
            binding.Exercice6.hint = "Panturrilha Sentado: $field6"

            // Define o hint do EditText7 com base no valor de "academia"
            when (academia) {
                "SF" -> binding.Exercice7.hint = "Panturrilha Leg Press: $field7"
                "FB" -> binding.Exercice7.hint = "Panturrilha Em Pé: $field7"
            }
        }
    }

    private fun saveData() {
        // Obtém o valor da variável 'academia' da Intent
        val academia = intent.getStringExtra("academia") ?: ""

        // Obtém os valores dos campos e faz o trim para remover espaços extras
        val extensoraUnilateral = binding.Exercice1.text.toString().trim()
        val agachamentoHack = binding.Exercice2.text.toString().trim()
        val legPress = binding.Exercice3.text.toString().trim()
        val mesaFlexora = binding.Exercice4.text.toString().trim()
        val cadeiraFlexora = binding.Exercice5.text.toString().trim()
        val panturrilhaSentado = binding.Exercice6.text.toString().trim()
        val panturrilha = binding.Exercice7.text.toString().trim()

        // Verifica se todos os campos estão preenchidos
        if (extensoraUnilateral.isEmpty() ||
            agachamentoHack.isEmpty() ||
            legPress.isEmpty() ||
            mesaFlexora.isEmpty() ||
            cadeiraFlexora.isEmpty() ||
            panturrilhaSentado.isEmpty() ||
            panturrilha.isEmpty()) {

            // Exibe uma mensagem de erro se algum campo estiver vazio
            mensagemNegativa(binding.root, "Por favor, preencha todos os campos.")
            return
        }

        val panturrilhaFieldSaveData = when (academia) {
            "SF" -> "PanturrilhaLegPress"
            "FB" -> "PanturrilhaEmPe"
            else -> "Panturrilha"
        }


        // Converte os valores dos campos para números (partindo do pressuposto que estão no formato "número/número")
        val (extensoraUnilateralFirst, extensoraUnilateralSecond) = parseNumbers(extensoraUnilateral)
        val (agachamentoHackFirst, agachamentoHackSecond) = parseNumbers(agachamentoHack)
        val (legPressFirst, legPressSecond) = parseNumbers(legPress)
        val (mesaFlexoraFirst, mesaFlexoraSecond) = parseNumbers(mesaFlexora)
        val (cadeiraFlexoraFirst, cadeiraFlexoraSecond) = parseNumbers(cadeiraFlexora)
        val (panturrilhaSentadoFirst, panturrilhaSentadoSecond) = parseNumbers(panturrilhaSentado)
        val (panturrilhaFirst, panturrilhaSecond) = parseNumbers(panturrilha)

        // Obtém o ID do usuário autenticado
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Cria o mapa de dados a ser salvo no Firestore
            val data = mapOf(
                "Treino" to "Treino A",
                "Academia" to academia,
                "ExtensoraUnilateral" to extensoraUnilateral,
                "AgachamentoHack" to agachamentoHack,
                "LegPress" to legPress,
                "MesaFlexora" to mesaFlexora,
                "CadeiraFlexora" to cadeiraFlexora,
                "PanturrilhaSentado" to panturrilhaSentado,
                panturrilhaFieldSaveData to panturrilha,
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
            db.collection("WorkoutA")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            // Determina o nome do campo "Panturrilha" com base no valor de "academia"
                            val panturrilhaField = when (academia) {
                                "SF" -> "PanturrilhaLegPress"
                                "FB" -> "PanturrilhaEmPe"
                                else -> "Panturrilha"
                            }

                            // Obtém os dados existentes do documento
                            val existingExtensora = document.getString("ExtensoraUnilateral") ?: ""
                            val existingAgachamento = document.getString("AgachamentoHack") ?: ""
                            val existingLegPress = document.getString("LegPress") ?: ""
                            val existingMesaFlexora = document.getString("MesaFlexora") ?: ""
                            val existingCadeiraFlexora = document.getString("CadeiraFlexora") ?: ""
                            val existingPanturrilhaSentado = document.getString("PanturrilhaSentado") ?: ""
                            val existingPanturrilha = document.getString(panturrilhaField) ?: ""

                            // Converte os dados existentes para números
                            val (existingExtensoraFirst, existingExtensoraSecond) = parseNumbers(existingExtensora)
                            val (existingAgachamentoFirst, existingAgachamentoSecond) = parseNumbers(existingAgachamento)
                            val (existingLegPressFirst, existingLegPressSecond) = parseNumbers(existingLegPress)
                            val (existingMesaFlexoraFirst, existingMesaFlexoraSecond) = parseNumbers(existingMesaFlexora)
                            val (existingCadeiraFlexoraFirst, existingCadeiraFlexoraSecond) = parseNumbers(existingCadeiraFlexora)
                            val (existingPanturrilhaSentadoFirst, existingPanturrilhaSentadoSecond) = parseNumbers(existingPanturrilhaSentado)
                            val (existingPanturrilhaFirst, existingPanturrilhaSecond) = parseNumbers(existingPanturrilha)

                            // Verifica se os novos valores são maiores ou iguais
                            if (shouldUpdate(existingExtensoraFirst, existingExtensoraSecond, extensoraUnilateralFirst, extensoraUnilateralSecond) ||
                                shouldUpdate(existingAgachamentoFirst, existingAgachamentoSecond, agachamentoHackFirst, agachamentoHackSecond) ||
                                shouldUpdate(existingLegPressFirst, existingLegPressSecond, legPressFirst, legPressSecond) ||
                                shouldUpdate(existingMesaFlexoraFirst, existingMesaFlexoraSecond, mesaFlexoraFirst, mesaFlexoraSecond) ||
                                shouldUpdate(existingCadeiraFlexoraFirst, existingCadeiraFlexoraSecond, cadeiraFlexoraFirst, cadeiraFlexoraSecond) ||
                                shouldUpdate(existingPanturrilhaSentadoFirst, existingPanturrilhaSentadoSecond, panturrilhaSentadoFirst, panturrilhaSentadoSecond) ||
                                shouldUpdate(existingPanturrilhaFirst, existingPanturrilhaSecond, panturrilhaFirst, panturrilhaSecond)) {

                                // Cria o mapa de dados a ser salvo no Firestore
                                val data = mapOf(
                                    "ExtensoraUnilateral" to extensoraUnilateral,
                                    "AgachamentoHack" to agachamentoHack,
                                    "LegPress" to legPress,
                                    "MesaFlexora" to mesaFlexora,
                                    "CadeiraFlexora" to cadeiraFlexora,
                                    "PanturrilhaSentado" to panturrilhaSentado,
                                    panturrilhaField to panturrilha
                                )

                                // Atualiza o documento na coleção "WorkoutA"
                                db.collection("WorkoutA")
                                    .document(document.id)
                                    .update(data)
                                    .addOnSuccessListener {
                                        mensagemPositiva(binding.root, "Dados atualizados com sucesso.")
                                    }
                                    .addOnFailureListener { exception ->
                                        mensagemNegativa(binding.root, "Erro ao atualizar dados: ${exception.message}")
                                    }
                            } else {
                                mensagemNegativa(binding.root, "Nenhum dado novo para atualizar.")
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
        return newFirst > existingFirst || (newFirst == existingFirst && newSecond > existingSecond)
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
