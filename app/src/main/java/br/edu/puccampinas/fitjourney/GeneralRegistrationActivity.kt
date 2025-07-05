package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityGeneralRegistrationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityGeneralRegistrationBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

class GeneralRegistrationActivity : AppCompatActivity() {

    private var trainingQuantity: String = ""
    private var gymsQuantity: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Permite que o layout vá até a borda da tela

        binding = ActivityGeneralRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.comeBack.setOnClickListener {
            comeBack()
        }

        binding.btnSave.setOnClickListener {
            saveData()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun saveData() {
        trainingQuantity = binding.Training.text.toString()
        gymsQuantity = binding.Gyms.text.toString()

        // Tenta converter os valores para inteiro
        val trainingNumber = trainingQuantity.toIntOrNull()
        val gymsNumber = gymsQuantity.toIntOrNull()

        // Verifica se os campos foram preenchidos
        if (trainingQuantity.isNotEmpty() && gymsQuantity.isNotEmpty()) {
            if (trainingNumber != null && gymsNumber != null) {
                goToNextActivity()
            } else {
                negativeMessage("Insira apenas números válidos!")
            }
        } else {
            negativeMessage("Preencha todos os campos")
        }
    }

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
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

    private fun comeBack() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish()
    }

    private fun goToNextActivity() {
        val intent = Intent(this, GymsNameActivity::class.java)
        intent.putExtra("quantidadeTreinos", trainingQuantity)
        intent.putExtra("quantidadeAcademias", gymsQuantity)
        startActivity(intent)
    }
}
