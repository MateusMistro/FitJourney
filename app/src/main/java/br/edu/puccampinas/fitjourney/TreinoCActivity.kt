package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTreinoBactivityBinding
import br.edu.puccampinas.fitjourney.databinding.ActivityTreinoCactivityBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

private lateinit var binding: ActivityTreinoCactivityBinding
private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class TreinoCActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTreinoCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, AcademiasActivity::class.java))
            finish()
        }

        val academia = intent.getStringExtra("academia")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("WorkoutC")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        setEditTextHints(documents)
                    } else {
                        mensagemNegativa(binding.root, "Nenhum dado encontrado para esse usuário.")
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
        for (document in documents) {
            val field1 = document.getString("PuxadaAberta") ?: ""
            val field2 = document.getString("RemadaTriangulo") ?: ""
            val field3 = document.getString("Pulldown") ?: ""
            val field4 = document.getString("CrucifixoInverso") ?: ""
            val field5 = document.getString("BicepsInclinado") ?: ""
            val field6 = document.getString("RoscaMartelo") ?: ""
            val field7 = document.getString("RoscaScottMaquina") ?: ""

            binding.Exercice1.hint = "Puxada Aberta: $field1"
            binding.Exercice2.hint = "Remada Triângulo: $field2"
            binding.Exercice3.hint = "Pulldown: $field3"
            binding.Exercice4.hint = "Crucifixo Inverso: $field4"
            binding.Exercice5.hint = "Biceps Inclinado: $field5"
            binding.Exercice6.hint = "Rosca Martelo: $field6"
            binding.Exercice7.hint = "Rosca Scott Maquina: $field7"
        }
    }

    private fun saveData() {
        val academia = intent.getStringExtra("academia") ?: ""

        val puxadaAberta = binding.Exercice1.text.toString().trim()
        val remadaTriangulo = binding.Exercice2.text.toString().trim()
        val pulldown = binding.Exercice3.text.toString().trim()
        val crucifixoInverso = binding.Exercice4.text.toString().trim()
        val bicepsInclinado = binding.Exercice5.text.toString().trim()
        val roscaMartelo = binding.Exercice6.text.toString().trim()
        val roscaScottMaquina = binding.Exercice7.text.toString().trim()

        if (puxadaAberta.isEmpty() ||
            remadaTriangulo.isEmpty() ||
            pulldown.isEmpty() ||
            crucifixoInverso.isEmpty() ||
            bicepsInclinado.isEmpty() ||
            roscaMartelo.isEmpty() ||
            roscaScottMaquina.isEmpty()
        ) {
            mensagemNegativa(binding.root, "Por favor, preencha todos os campos.")
            return
        }

        val (puxadaAbertaFirst, puxadaAbertaSecond) = parseNumbers(puxadaAberta)
        val (remadaTrianguloFirst, remadaTrianguloSecond) = parseNumbers(remadaTriangulo)
        val (pulldownFirst, pulldownSecond) = parseNumbers(pulldown)
        val (crucifixoInversoFirst, crucifixoInversoSecond) = parseNumbers(crucifixoInverso)
        val (bicepsInclinadoFirst, bicepsInclinadoSecond) = parseNumbers(bicepsInclinado)
        val (roscaMarteloFirst, roscaMarteloSecond) = parseNumbers(roscaMartelo)
        val (roscaScottMaquinaFirst, roscaScottMaquinaSecond) = parseNumbers(roscaScottMaquina)

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val data = mapOf(
                "Treino" to "Treino C",
                "Academia" to academia,
                "PuxadaAberta" to puxadaAberta,
                "RemadaTriangulo" to remadaTriangulo,
                "Pulldown" to pulldown,
                "CrucifixoInverso" to crucifixoInverso,
                "BicepsInclinado" to bicepsInclinado,
                "RoscaMartelo" to roscaMartelo,
                "RoscaScottMaquina" to roscaScottMaquina,
                "UserId" to userId,
                "Data" to Date()
            )

            db.collection("Workouts")
                .add(data)
                .addOnSuccessListener {
                    mensagemPositiva(binding.root, "Dados salvos com sucesso.")
                }
                .addOnFailureListener { exception ->
                    mensagemNegativa(binding.root, "Erro ao salvar dados: ${exception.message}")
                }

            db.collection("WorkoutC")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val existingPuxadaAberta = document.getString("PuxadaAberta") ?: ""
                            val existingRemadaTriangulo = document.getString("RemadaTriangulo") ?: ""
                            val existingPulldown = document.getString("Pulldown") ?: ""
                            val existingCrucifixoInvertido = document.getString("CrucifixoInverso") ?: ""
                            val existingBicepsInclinado = document.getString("BicepsInclinado") ?: ""
                            val existingRoscaMartelo = document.getString("RoscaMartelo") ?: ""
                            val existingRoscaScottMaquina = document.getString("RoscaScottMaquina") ?: ""

                            val (existingPuxadaAbertaFirst, existingPuxadaAbertaSecond) = parseNumbers(existingPuxadaAberta)
                            val (existingRemadaTrianguloFirst, existingRemadaTrianguloSecond) = parseNumbers(existingRemadaTriangulo)
                            val (existingPulldownFirst, existingPulldownSecond) = parseNumbers(existingPulldown)
                            val (existingCrucifixoInvertidoFirst, existingCrucifixoInvertidoSecond) = parseNumbers(existingCrucifixoInvertido)
                            val (existingBicepsInclinadoFirst, existingBicepsInclinadoSecond) = parseNumbers(existingBicepsInclinado)
                            val (existingRoscaMarteloFirst, existingRoscaMarteloSecond) = parseNumbers(existingRoscaMartelo)
                            val (existingRoscaScottMaquinaFirst, existingRoscaScottMaquinaSecond) = parseNumbers(existingRoscaScottMaquina)

                            var novoPuxadaAberta= existingPuxadaAberta
                            var novoRemadaTriangulo = existingRemadaTriangulo
                            var novoPulldown= existingPulldown
                            var novoCrucifixoInvertido = existingCrucifixoInvertido
                            var novoBicepsInclinado = existingBicepsInclinado
                            var novoRoscaMartelo = existingRoscaMartelo
                            var novoRoscaScottMaquina = existingRoscaScottMaquina


                            // Agora verifica corretamente se deve atualizar com base no novo valor
                            if (shouldUpdate(existingPuxadaAbertaFirst, existingPuxadaAbertaSecond, puxadaAbertaFirst, puxadaAbertaSecond)) {
                                novoPuxadaAberta = puxadaAberta
                            }
                            if(shouldUpdate(
                                    existingRemadaTrianguloFirst, existingRemadaTrianguloSecond, remadaTrianguloFirst, remadaTrianguloSecond
                                )){
                                novoRemadaTriangulo = remadaTriangulo
                            }
                            if(shouldUpdate(
                                    existingPulldownFirst, existingPulldownSecond, pulldownFirst, pulldownSecond
                                )){
                                novoPulldown = pulldown
                            }
                            if(shouldUpdate(
                                    existingCrucifixoInvertidoFirst, existingCrucifixoInvertidoSecond, crucifixoInversoFirst, crucifixoInversoSecond
                                )){
                                novoCrucifixoInvertido = crucifixoInverso
                            }
                            if(shouldUpdate(
                                    existingBicepsInclinadoFirst, existingBicepsInclinadoSecond, bicepsInclinadoFirst, bicepsInclinadoSecond
                                )){
                                novoBicepsInclinado = bicepsInclinado
                            }
                            if(shouldUpdate(
                                    existingRoscaMarteloFirst, existingRoscaMarteloSecond, roscaMarteloFirst, roscaMarteloSecond
                                )){
                                novoRoscaMartelo = roscaMartelo
                            }

                            if( shouldUpdate(
                                    existingRoscaScottMaquinaFirst, existingRoscaScottMaquinaSecond, roscaScottMaquinaFirst, roscaScottMaquinaSecond
                                )){
                                novoRoscaScottMaquina = roscaScottMaquina

                            }

                            val updatedData = mapOf(
                                "PuxadaAberta" to novoPuxadaAberta,
                                "RemadaTriangulo" to novoRemadaTriangulo,
                                "Pulldown" to novoPulldown,
                                "CrucifixoInverso" to novoCrucifixoInvertido,
                                "BicepsInclinado" to novoBicepsInclinado,
                                "RoscaMartelo" to novoRoscaMartelo,
                                "RoscaScottMaquina" to novoRoscaScottMaquina
                            )

                            db.collection("WorkoutC")
                                .document(document.id)
                                .update(updatedData)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { exception ->
                                    mensagemNegativa(binding.root, "Erro ao atualizar dados: ${exception.message}")
                                }

                        }
                    }

                    if(academia == "SF") {
                        // Atualiza também os documentos com "Academia" = "FB"
                        db.collection("WorkoutC")
                            .whereEqualTo("Academia", "FB")
                            .get()
                            .addOnSuccessListener { fbDocuments ->
                                for (fbDocument in fbDocuments) {
                                    val (fbBicepsInclinadoFirst, fbBicepsInclinadoSecond) = parseNumbers(
                                        fbDocument.getString("BicepsInclinado") ?: ""
                                    )
                                    val (fbRoscaMarteloFirst, fbRoscaMarteloSecond) = parseNumbers(
                                        fbDocument.getString("RoscaMartelo") ?: ""
                                    )

                                    // Inicializando variáveis para os novos valores
                                    var updatedBicepsInclinado =
                                        fbDocument.getString("BicepsInclinado") ?: ""
                                    var updatedRoscaMartelo =
                                        fbDocument.getString("RoscaMartelo") ?: ""


                                    // Verifica se deve atualizar também para academia FB
                                    if (shouldUpdate(
                                            fbBicepsInclinadoFirst,
                                            fbBicepsInclinadoSecond,
                                            bicepsInclinadoFirst,
                                            bicepsInclinadoSecond
                                        )
                                    ) {
                                        updatedBicepsInclinado = bicepsInclinado
                                    }
                                    if (shouldUpdate(
                                            fbRoscaMarteloFirst,
                                            fbRoscaMarteloSecond,
                                            roscaMarteloFirst,
                                            roscaMarteloSecond
                                        )
                                    ) {
                                        updatedRoscaMartelo = roscaMartelo
                                    }

                                    val updatedData = mapOf(
                                        "BicepsInclinado" to updatedBicepsInclinado,
                                        "RoscaMartelo" to updatedRoscaMartelo
                                    )

                                    db.collection("WorkoutC")
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
                        db.collection("WorkoutC")
                            .whereEqualTo("Academia", "SF")
                            .get()
                            .addOnSuccessListener { fbDocuments ->
                                for (fbDocument in fbDocuments) {
                                    val (fbBicepsInclinadoFirst, fbBicepsInclinadoSecond) = parseNumbers(
                                        fbDocument.getString("BicepsInclinado") ?: ""
                                    )
                                    val (fbRoscaMarteloFirst, fbRoscaMarteloSecond) = parseNumbers(
                                        fbDocument.getString("RoscaMartelo") ?: ""
                                    )

                                    // Inicializando variáveis para os novos valores
                                    var updatedBicepsInclinado =
                                        fbDocument.getString("BicepsInclinado") ?: ""
                                    var updatedRoscaMartelo =
                                        fbDocument.getString("RoscaMartelo") ?: ""


                                    // Verifica se deve atualizar também para academia FB
                                    if (shouldUpdate(
                                            fbBicepsInclinadoFirst,
                                            fbBicepsInclinadoSecond,
                                            bicepsInclinadoFirst,
                                            bicepsInclinadoSecond
                                        )
                                    ) {
                                        updatedBicepsInclinado = bicepsInclinado
                                    }
                                    if (shouldUpdate(
                                            fbRoscaMarteloFirst,
                                            fbRoscaMarteloSecond,
                                            roscaMarteloFirst,
                                            roscaMarteloSecond
                                        )
                                    ) {
                                        updatedRoscaMartelo = roscaMartelo
                                    }

                                    val updatedData = mapOf(
                                        "BicepsInclinado" to updatedBicepsInclinado,
                                        "RoscaMartelo" to updatedRoscaMartelo
                                    )

                                    db.collection("WorkoutC")
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

    private fun parseNumbers(input: String): Pair<Int, Int> {
        val parts = input.split("/")
        return if (parts.size == 2) {
            val first = parts[0].toIntOrNull() ?: 0
            val second = parts[1].toIntOrNull() ?: 0
            Pair(first, second)
        } else {
            Pair(0, 0)
        }
    }

    private fun shouldUpdate(existingFirst: Int, existingSecond: Int, newFirst: Int, newSecond: Int): Boolean {
        // Atualizar apenas se o primeiro número for maior
        // Ou, se o primeiro número for igual, verificar o segundo número
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
