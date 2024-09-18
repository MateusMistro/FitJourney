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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date

private lateinit var binding: ActivityTreinoBactivityBinding
private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class TreinoBActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTreinoBactivityBinding.inflate(layoutInflater)
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
            db.collection("WorkoutB")
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
            val field1 = document.getString("SupinoInclinadoHalter") ?: ""
            val field2 = document.getString("SupinoRetoBarra") ?: ""
            val field3 = document.getString("CrucifixoMaquina") ?: ""
            val field4 = document.getString("ElevacaoLateralHalter") ?: ""
            val field5 = document.getString("ElevacaoLateralPolia") ?: ""
            val field6 = document.getString("DesenvolvimentoMaquina") ?: ""
            val field7 = document.getString("TricepsFrances") ?: ""
            val field8 = document.getString("TricepsCorda") ?: ""

            binding.Exercice1.hint = "Supino Inclinado Halter: $field1"
            binding.Exercice2.hint = "Supino Reto Barra: $field2"
            binding.Exercice3.hint = "Crucifixo Maquina: $field3"
            binding.Exercice4.hint = "Elevação Lateral Halter: $field4"
            binding.Exercice5.hint = "Elevação Lateral Polia: $field5"
            binding.Exercice6.hint = "Desenvolvimento Maquina: $field6"
            binding.Exercice7.hint = "Triceps Frances: $field7"
            binding.Exercice8.hint = "Triceps Corda: $field8"
        }
    }

    private fun saveData() {
        val academia = intent.getStringExtra("academia") ?: ""

        val supinoInclinadoHalter = binding.Exercice1.text.toString().trim()
        val supinoRetoBarra = binding.Exercice2.text.toString().trim()
        val crucifixoMaquina = binding.Exercice3.text.toString().trim()
        val elevacaoLateralHalter = binding.Exercice4.text.toString().trim()
        val elevacaoLateralPolia = binding.Exercice5.text.toString().trim()
        val desenvolvimentoMaquina = binding.Exercice6.text.toString().trim()
        val tricepsFrances = binding.Exercice7.text.toString().trim()
        val tricepsCorda = binding.Exercice8.text.toString().trim()

        if (supinoInclinadoHalter.isEmpty() ||
            supinoRetoBarra.isEmpty() ||
            crucifixoMaquina.isEmpty() ||
            elevacaoLateralHalter.isEmpty() ||
            elevacaoLateralPolia.isEmpty() ||
            desenvolvimentoMaquina.isEmpty() ||
            tricepsFrances.isEmpty() ||
            tricepsCorda.isEmpty()
        ) {
            mensagemNegativa(binding.root, "Por favor, preencha todos os campos.")
            return
        }

        val (supinoInclinadoHalterFirst, supinoInclinadoHalterSecond) = parseNumbers(supinoInclinadoHalter)
        val (supinoRetoBarraFirst, supinoRetoBarraSecond) = parseNumbers(supinoRetoBarra)
        val (crucifixoMaquinaFirst, crucifixoMaquinaSecond) = parseNumbers(crucifixoMaquina)
        val (elevacaoLateralHalterFirst, elevacaoLateralHalterSecond) = parseNumbers(elevacaoLateralHalter)
        val (elevacaoLateralPoliaFirst, elevacaoLateralPoliaSecond) = parseNumbers(elevacaoLateralPolia)
        val (desenvolvimentoMaquinaFirst, desenvolvimentoMaquinaSecond) = parseNumbers(desenvolvimentoMaquina)
        val (tricepsFrancesFirst, tricepsFrancesSecond) = parseNumbers(tricepsFrances)
        val (tricepsCordaFirst, tricepsCordaSecond) = parseNumbers(tricepsCorda)

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val data = mapOf(
                "Treino" to "Treino B",
                "Academia" to academia,
                "SupinoInclinadoHalter" to supinoInclinadoHalter,
                "SupinoRetoBarra" to supinoRetoBarra,
                "CrucifixoMaquina" to crucifixoMaquina,
                "ElevacaoLateralHalter" to elevacaoLateralHalter,
                "ElevacaoLateralPolia" to elevacaoLateralPolia,
                "DesenvolvimentoMaquina" to desenvolvimentoMaquina,
                "TricepsFrances" to tricepsFrances,
                "TricepsCorda" to tricepsCorda,
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

            db.collection("WorkoutB")
                .whereEqualTo("UserId", userId)
                .whereEqualTo("Academia", academia)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for (document in documents) {
                            val existingSupinoInclinado = document.getString("SupinoInclinadoHalter") ?: ""
                            val existingSupinoReto = document.getString("SupinoRetoBarra") ?: ""
                            val existingCrucifixo = document.getString("CrucifixoMaquina") ?: ""
                            val existingElevacaoLateralHalter = document.getString("ElevacaoLateralHalter") ?: ""
                            val existingElevacaoLateralPolia = document.getString("ElevacaoLateralPolia") ?: ""
                            val existingDesenvolvimento = document.getString("DesenvolvimentoMaquina") ?: ""
                            val existingTricepsFrances = document.getString("TricepsFrances") ?: ""
                            val existingTricepsCorda = document.getString("TricepsCorda") ?: ""

                            val (existingSupinoInclinadoFirst, existingSupinoInclinadoSecond) = parseNumbers(existingSupinoInclinado)
                            val (existingSupinoRetoFirst, existingSupinoRetoSecond) = parseNumbers(existingSupinoReto)
                            val (existingCrucifixoFirst, existingCrucifixoSecond) = parseNumbers(existingCrucifixo)
                            val (existingElevacaoLateralHalterFirst, existingElevacaoLateralHalterSecond) = parseNumbers(existingElevacaoLateralHalter)
                            val (existingElevacaoLateralPoliaFirst, existingElevacaoLateralPoliaSecond) = parseNumbers(existingElevacaoLateralPolia)
                            val (existingDesenvolvimentoFirst, existingDesenvolvimentoSecond) = parseNumbers(existingDesenvolvimento)
                            val (existingTricepsFrancesFirst, existingTricepsFrancesSecond) = parseNumbers(existingTricepsFrances)
                            val (existingTricepsCordaFirst, existingTricepsCordaSecond) = parseNumbers(existingTricepsCorda)

                            var novoSupinoInclinado= existingSupinoInclinado
                            var novoSupinoReto = existingSupinoReto
                            var novoCrucifixo= existingCrucifixo
                            var novoElevacaoHalter = existingElevacaoLateralHalter
                            var novoElevacaoPolia = existingElevacaoLateralPolia
                            var novoDesenvolvimento = existingDesenvolvimento
                            var novoFrances = existingTricepsFrances
                            var novoCorda = existingTricepsCorda


                            // Agora verifica corretamente se deve atualizar com base no novo valor
                            if (shouldUpdate(existingSupinoInclinadoFirst, existingSupinoInclinadoSecond, supinoInclinadoHalterFirst, supinoInclinadoHalterSecond)) {
                                    novoSupinoInclinado = supinoInclinadoHalter
                                }
                            if(shouldUpdate(
                                    existingSupinoRetoFirst, existingSupinoRetoSecond, supinoRetoBarraFirst, supinoRetoBarraSecond
                                )){
                                novoSupinoReto = supinoRetoBarra
                            }
                            if(shouldUpdate(
                                    existingCrucifixoFirst, existingCrucifixoSecond, crucifixoMaquinaFirst, crucifixoMaquinaSecond
                                )){
                                novoCrucifixo = crucifixoMaquina
                            }
                            if(shouldUpdate(
                                    existingElevacaoLateralHalterFirst, existingElevacaoLateralHalterSecond, elevacaoLateralHalterFirst, elevacaoLateralHalterSecond
                                )){
                                novoElevacaoHalter = elevacaoLateralHalter
                            }
                            if(shouldUpdate(
                                    existingElevacaoLateralPoliaFirst, existingElevacaoLateralPoliaSecond, elevacaoLateralPoliaFirst, elevacaoLateralPoliaSecond
                                )){
                                novoElevacaoPolia = elevacaoLateralPolia
                            }
                            if(shouldUpdate(
                                    existingDesenvolvimentoFirst, existingDesenvolvimentoSecond, desenvolvimentoMaquinaFirst, desenvolvimentoMaquinaSecond
                                )){
                                novoDesenvolvimento = desenvolvimentoMaquina
                            }

                            if( shouldUpdate(
                                    existingTricepsFrancesFirst, existingTricepsFrancesSecond, tricepsFrancesFirst, tricepsFrancesSecond
                                )){
                                     novoFrances = tricepsFrances

                                 }
                            if(shouldUpdate(
                                    existingTricepsCordaFirst, existingTricepsCordaSecond, tricepsCordaFirst, tricepsCordaSecond
                                )){
                                novoCorda = tricepsCorda
                            }


                                val updatedData = mapOf(
                                    "SupinoInclinadoHalter" to novoSupinoInclinado,
                                    "SupinoRetoBarra" to novoSupinoReto,
                                    "CrucifixoMaquina" to novoCrucifixo,
                                    "ElevacaoLateralHalter" to novoElevacaoHalter,
                                    "ElevacaoLateralPolia" to novoElevacaoPolia,
                                    "DesenvolvimentoMaquina" to novoDesenvolvimento,
                                    "TricepsFrances" to novoFrances,
                                    "TricepsCorda" to novoCorda
                                )

                                db.collection("WorkoutB")
                                    .document(document.id)
                                    .update(updatedData)
                                    .addOnSuccessListener {
                                        mensagemPositiva(binding.root, "Dados atualizados com sucesso.")
                                    }
                                    .addOnFailureListener { exception ->
                                        mensagemNegativa(binding.root, "Erro ao atualizar dados: ${exception.message}")
                                    }

                        }
                    }

                    // Atualiza também os documentos com "Academia" = "FB"
                    db.collection("WorkoutB")
                        .whereEqualTo("Academia", "FB")
                        .get()
                        .addOnSuccessListener { fbDocuments ->
                            for (fbDocument in fbDocuments) {
                                val (fbSupinoInclinadoFirst, fbSupinoInclinadoSecond) = parseNumbers(fbDocument.getString("SupinoInclinadoHalter") ?: "")
                                val (fbSupinoRetoFirst, fbSupinoRetoSecond) = parseNumbers(fbDocument.getString("SupinoRetoBarra") ?: "")
                                val (fbElevacaoLateralHalterFirst, fbElevacaoLateralHalterSecond) = parseNumbers(fbDocument.getString("ElevacaoLateralHalter") ?: "")

                                // Inicializando variáveis para os novos valores
                                var updatedSupinoInclinado = fbDocument.getString("SupinoInclinadoHalter") ?: ""
                                var updatedSupinoReto = fbDocument.getString("SupinoRetoBarra") ?: ""
                                var updatedElevacaoLateralHalter = fbDocument.getString("ElevacaoLateralHalter") ?: ""


                                // Verifica se deve atualizar também para academia FB
                                if (shouldUpdate(fbSupinoInclinadoFirst, fbSupinoInclinadoSecond, supinoInclinadoHalterFirst, supinoInclinadoHalterSecond)) {
                                    updatedSupinoInclinado = supinoInclinadoHalter
                                }
                                if (shouldUpdate(fbSupinoRetoFirst, fbSupinoRetoSecond, supinoRetoBarraFirst, supinoRetoBarraSecond)) {
                                    updatedSupinoReto = supinoRetoBarra
                                }
                                if (shouldUpdate(fbElevacaoLateralHalterFirst, fbElevacaoLateralHalterSecond, elevacaoLateralHalterFirst, elevacaoLateralHalterSecond)) {
                                    updatedElevacaoLateralHalter = elevacaoLateralHalter
                                }
                                val updatedData = mapOf(
                                    "SupinoInclinadoHalter" to updatedSupinoInclinado,
                                    "SupinoRetoBarra" to updatedSupinoReto,
                                    "ElevacaoLateralHalter" to updatedElevacaoLateralHalter
                                )

                                db.collection("WorkoutB")
                                        .document(fbDocument.id)
                                        .update(updatedData)
                                        .addOnSuccessListener {
                                        }
                                        .addOnFailureListener { exception ->
                                            mensagemNegativa(binding.root, "Erro ao atualizar dados da academia FB: ${exception.message}")
                                        }
                                }

                        }
                        .addOnFailureListener { exception ->
                            mensagemNegativa(binding.root, "Erro ao buscar documentos da academia FB: ${exception.message}")
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
