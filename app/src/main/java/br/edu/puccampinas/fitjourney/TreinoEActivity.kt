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
import br.edu.puccampinas.fitjourney.databinding.ActivityTreinoEactivityBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

private lateinit var binding: ActivityTreinoEactivityBinding
private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class TreinoEActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTreinoEactivityBinding.inflate(layoutInflater)
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
            // Consulta a coleção "workoutE" onde o campo "UserId" é igual ao ID do usuário conectado
            db.collection("WorkoutE")
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

            val field1 = document.getString("ElevacaoLateralHalter") ?: ""
            val field2 = document.getString("ElevacaoLateralPolia") ?: ""
            val field3 = document.getString("Desenvolvimento") ?: ""
            val field4 = document.getString("CrucifixoInverso") ?: ""
            val field5 = document.getString("SupinoInclinadoHalter") ?: ""
            val field6 = document.getString("CrossOver") ?: ""

            binding.Exercice1.hint = "Elevação Lateral Halter: $field1"
            binding.Exercice2.hint = "Elevação Lateral Polia: $field2"
            binding.Exercice3.hint = "Desenvolvimento: $field3"
            binding.Exercice4.hint = "Crucifixo Inverso: $field4"
            binding.Exercice5.hint = "Supino Inclinado Halter: $field5"
            binding.Exercice6.hint = "Cross Over: $field6"
        }
    }

    private fun saveData() {
        // Obtém o valor da variável 'academia' da Intent
        val academia = intent.getStringExtra("academia") ?: ""

        // Obtém os valores dos campos e faz o trim para remover espaços extras
        val ElevacaoLateralHalter = binding.Exercice1.text.toString().trim()
        val ElevacaoLateralPolia = binding.Exercice2.text.toString().trim()
        val Desenvolvimento = binding.Exercice3.text.toString().trim()
        val CrucifixoInverso = binding.Exercice4.text.toString().trim()
        val SupinoInclinadoHalter = binding.Exercice5.text.toString().trim()
        val CrossOver = binding.Exercice6.text.toString().trim()

        // Verifica se todos os campos estão preenchidos
        if (ElevacaoLateralHalter.isEmpty() ||
            ElevacaoLateralPolia.isEmpty() ||
            Desenvolvimento.isEmpty() ||
            CrucifixoInverso.isEmpty() ||
            SupinoInclinadoHalter.isEmpty() ||
            CrossOver.isEmpty()) {

            // Exibe uma mensagem de erro se algum campo estiver vazio
            mensagemNegativa(binding.root, "Por favor, preencha todos os campos.")
            return
        }

        // Converte os valores dos campos para números (partindo do pressuposto que estão no formato "número/número")
        val (ElevacaoLateralHalterFirst, ElevacaoLateralHalterSecond) = parseNumbers(ElevacaoLateralHalter)
        val (ElevacaoLateralPoliaFirst, ElevacaoLateralPoliaSecond) = parseNumbers(ElevacaoLateralPolia)
        val (DesenvolvimentoFirst, DesenvolvimentoSecond) = parseNumbers(Desenvolvimento)
        val (CrucifixoInversoFirst, CrucifixoInversoSecond) = parseNumbers(CrucifixoInverso)
        val (SupinoInclinadoHalterFirst, SupinoInclinadoHalterSecond) = parseNumbers(SupinoInclinadoHalter)
        val (CrossOverFirst, CrossOverSecond) = parseNumbers(CrossOver)

        // Obtém o ID do usuário autenticado
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            // Cria o mapa de dados a ser salvo no Firestore
            val data = mapOf(
                "Treino" to "Treino E",
                "Academia" to academia,
                "ElevacaoLateralHalter" to ElevacaoLateralHalter,
                "ElevacaoLateralPolia" to ElevacaoLateralPolia,
                "Desenvolvimento" to Desenvolvimento,
                "CrucifixoInverso" to CrucifixoInverso,
                "SupinoInclinadoHalter" to SupinoInclinadoHalter,
                "CrossOver" to CrossOver,
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
            db.collection("WorkoutE")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {

                            // Obtém os dados existentes do documento
                            val existingElevacaoLateralHalter = document.getString("ElevacaoLateralHalter") ?: ""
                            val existingElevacaoLateralPolia = document.getString("ElevacaoLateralPolia") ?: ""
                            val existingDesenvolvimento = document.getString("Desenvolvimento") ?: ""
                            val existingCrucifixoInverso = document.getString("CrucifixoInverso") ?: ""
                            val existingSupinoInclinadoHalter = document.getString("SupinoInclinadoHalter") ?: ""
                            val existingCrossOver = document.getString("CrossOver") ?: ""

                            // Converte os dados existentes para números
                            val (existingElevacaoLateralHalterFirst, existingElevacaoLateralHalterSecond) = parseNumbers(existingElevacaoLateralHalter)
                            val (existingElevacaoLateralPoliaFirst, existingElevacaoLateralPoliaSecond) = parseNumbers(existingElevacaoLateralPolia)
                            val (existingDesenvolvimentoFirst, existingDesenvolvimentoSecond) = parseNumbers(existingDesenvolvimento)
                            val (existingCrucifixoInversoFirst, existingCrucifixoInversoSecond) = parseNumbers(existingCrucifixoInverso)
                            val (existingSupinoInclinadoHalterFirst, existingSupinoInclinadoHalterSecond) = parseNumbers(existingSupinoInclinadoHalter)
                            val (existingCrossOverFirst, existingCrossOverSecond) = parseNumbers(existingCrossOver)

                            var novoElevacaoLateralHalter = existingElevacaoLateralHalter
                            var novoElevacaoLateralPolia = existingElevacaoLateralPolia
                            var novoDesenvolvimento = existingDesenvolvimento
                            var novoCrucifixoInverso = existingCrucifixoInverso
                            var novoSupinoInclinadoHalter = existingSupinoInclinadoHalter
                            var novoCrossOver = existingCrossOver

                            // Verifica se os novos valores são maiores ou iguais

                            if(shouldUpdate(existingElevacaoLateralHalterFirst, existingElevacaoLateralHalterSecond, ElevacaoLateralHalterFirst, ElevacaoLateralHalterSecond)){
                                novoElevacaoLateralHalter = ElevacaoLateralHalter
                            }
                            if(shouldUpdate(existingElevacaoLateralPoliaFirst, existingElevacaoLateralPoliaSecond, ElevacaoLateralPoliaFirst, ElevacaoLateralPoliaSecond)){
                                novoElevacaoLateralPolia = ElevacaoLateralPolia
                            }
                            if(shouldUpdate(existingDesenvolvimentoFirst, existingDesenvolvimentoSecond, DesenvolvimentoFirst, DesenvolvimentoSecond)){
                                novoDesenvolvimento = Desenvolvimento
                            }
                            if(shouldUpdate(existingCrucifixoInversoFirst, existingCrucifixoInversoSecond, CrucifixoInversoFirst, CrucifixoInversoSecond)){
                                novoCrucifixoInverso = CrucifixoInverso
                            }
                            if(shouldUpdate(existingSupinoInclinadoHalterFirst, existingSupinoInclinadoHalterSecond, SupinoInclinadoHalterFirst, SupinoInclinadoHalterSecond)){
                                novoSupinoInclinadoHalter = SupinoInclinadoHalter
                            }
                            if(shouldUpdate(existingCrossOverFirst, existingCrossOverSecond, CrossOverFirst, CrossOverSecond) ){
                                novoCrossOver = CrossOver
                            }

                            // Cria o mapa de dados a ser salvo no Firestore
                            val data = mapOf(
                                "ElevacaoLateralHalter" to novoElevacaoLateralHalter,
                                "ElevacaoLateralPolia" to novoElevacaoLateralPolia,
                                "Desenvolvimento" to novoDesenvolvimento,
                                "CrucifixoInverso" to novoCrucifixoInverso,
                                "SupinoInclinadoHalter" to novoSupinoInclinadoHalter,
                                "CrossOver" to novoCrossOver
                            )

                            // Atualiza o documento na coleção "WorkoutE"
                            db.collection("WorkoutE")
                                .document(document.id)
                                .update(data)
                                .addOnSuccessListener {
                                    mensagemPositiva(binding.root, "Dados atualizados com sucesso.")
                                }
                                .addOnFailureListener { exception ->
                                    mensagemNegativa(binding.root, "Erro ao atualizar dados: ${exception.message}")
                                }

                        }
                    }

                    if(academia == "SF") {
                        // Atualiza também os documentos com "Academia" = "FB"
                        db.collection("WorkoutE")
                            .whereEqualTo("Academia", "FB")
                            .get()
                            .addOnSuccessListener { fbDocuments ->
                                for (fbDocument in fbDocuments) {
                                    val (fbElevacaoLateralHalterFirst, fbElevacaoLateralHalterSecond) = parseNumbers(
                                        fbDocument.getString("ElevacaoLateralHalter") ?: ""
                                    )
                                    val (fbDesenvolvimentoFirst, fbDesenvolvimentoSecond) = parseNumbers(
                                        fbDocument.getString("Desenvolvimento") ?: ""
                                    )
                                    val (fbSupinoInclinadoHalterFirst, fbSupinoInclinadoHalterSecond) = parseNumbers(
                                        fbDocument.getString("SupinoInclinadoHalter") ?: ""
                                    )

                                    // Inicializando variáveis para os novos valores
                                    var updatedElevacaoLateralHalter =
                                        fbDocument.getString("ElevacaoLateralHalter") ?: ""
                                    var updatedDesenvolvimento =
                                        fbDocument.getString("Desenvolvimento") ?: ""
                                    var updatedSupinoInclinadoHalter =
                                        fbDocument.getString("SupinoInclinadoHalter") ?: ""

                                    // Verifica se deve atualizar também para academia FB
                                    if (shouldUpdate(
                                            fbElevacaoLateralHalterFirst,
                                            fbElevacaoLateralHalterSecond,
                                            ElevacaoLateralHalterFirst,
                                            ElevacaoLateralHalterSecond
                                        )
                                    ) {
                                        updatedElevacaoLateralHalter = ElevacaoLateralHalter
                                    }
                                    if (shouldUpdate(
                                            fbDesenvolvimentoFirst,
                                            fbDesenvolvimentoSecond,
                                            DesenvolvimentoFirst,
                                            DesenvolvimentoSecond
                                        )
                                    ) {
                                        updatedDesenvolvimento = Desenvolvimento
                                    }

                                    if (shouldUpdate(
                                            fbSupinoInclinadoHalterFirst,
                                            fbSupinoInclinadoHalterSecond,
                                            SupinoInclinadoHalterFirst,
                                            SupinoInclinadoHalterSecond
                                        )
                                    ) {
                                        updatedSupinoInclinadoHalter = SupinoInclinadoHalter
                                    }

                                    val updatedData = mapOf(
                                        "ElevacaoLateralHalter" to updatedElevacaoLateralHalter,
                                        "Desenvolvimento" to updatedDesenvolvimento,
                                        "SupinoInclinadoHalter" to updatedSupinoInclinadoHalter
                                    )

                                    db.collection("WorkoutE")
                                        .document(fbDocument.id)
                                        .update(updatedData)
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener { exception ->
                                            mensagemNegativa(
                                                binding.root,
                                                "Erro ao atualizar dados da academia FB: ${exception.message}"
                                            )
                                        }
                                }

                            }
                            .addOnFailureListener { exception ->
                                mensagemNegativa(
                                    binding.root,
                                    "Erro ao buscar documentos da academia FB: ${exception.message}"
                                )
                            }
                    }else if(academia == "FB") {
                        db.collection("WorkoutE")
                            .whereEqualTo("Academia", "SF")
                            .get()
                            .addOnSuccessListener { fbDocuments ->
                                for (fbDocument in fbDocuments) {
                                    val (fbElevacaoLateralHalterFirst, fbElevacaoLateralHalterSecond) = parseNumbers(
                                        fbDocument.getString("ElevacaoLateralHalter") ?: ""
                                    )
                                    val (fbDesenvolvimentoFirst, fbDesenvolvimentoSecond) = parseNumbers(
                                        fbDocument.getString("Desenvolvimento") ?: ""
                                    )
                                    val (fbSupinoInclinadoHalterFirst, fbSupinoInclinadoHalterSecond) = parseNumbers(
                                        fbDocument.getString("SupinoInclinadoHalter") ?: ""
                                    )

                                    // Inicializando variáveis para os novos valores
                                    var updatedElevacaoLateralHalter =
                                        fbDocument.getString("ElevacaoLateralHalter") ?: ""
                                    var updatedDesenvolvimento =
                                        fbDocument.getString("Desenvolvimento") ?: ""
                                    var updatedSupinoInclinadoHalter =
                                        fbDocument.getString("SupinoInclinadoHalter") ?: ""

                                    // Verifica se deve atualizar também para academia FB
                                    if (shouldUpdate(
                                            fbElevacaoLateralHalterFirst,
                                            fbElevacaoLateralHalterSecond,
                                            ElevacaoLateralHalterFirst,
                                            ElevacaoLateralHalterSecond
                                        )
                                    ) {
                                        updatedElevacaoLateralHalter = ElevacaoLateralHalter
                                    }
                                    if (shouldUpdate(
                                            fbDesenvolvimentoFirst,
                                            fbDesenvolvimentoSecond,
                                            DesenvolvimentoFirst,
                                            DesenvolvimentoSecond
                                        )
                                    ) {
                                        updatedDesenvolvimento = Desenvolvimento
                                    }

                                    if (shouldUpdate(
                                            fbSupinoInclinadoHalterFirst,
                                            fbSupinoInclinadoHalterSecond,
                                            SupinoInclinadoHalterFirst,
                                            SupinoInclinadoHalterSecond
                                        )
                                    ) {
                                        updatedSupinoInclinadoHalter = SupinoInclinadoHalter
                                    }

                                    val updatedData = mapOf(
                                        "ElevacaoLateralHalter" to updatedElevacaoLateralHalter,
                                        "Desenvolvimento" to updatedDesenvolvimento,
                                        "SupinoInclinadoHalter" to updatedSupinoInclinadoHalter
                                    )

                                    db.collection("WorkoutE")
                                        .document(fbDocument.id)
                                        .update(updatedData)
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener { exception ->
                                            mensagemNegativa(
                                                binding.root,
                                                "Erro ao atualizar dados da academia FB: ${exception.message}"
                                            )
                                        }
                                }

                            }
                            .addOnFailureListener { exception ->
                                mensagemNegativa(
                                    binding.root,
                                    "Erro ao buscar documentos da academia FB: ${exception.message}"
                                )
                            }
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
