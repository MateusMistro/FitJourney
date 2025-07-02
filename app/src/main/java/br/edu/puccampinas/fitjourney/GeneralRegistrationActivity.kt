package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityGeneralRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityGeneralRegistrationBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth

class GeneralRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityGeneralRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.comeBack.setOnClickListener {
            startActivity(Intent(this,MenuActivity::class.java))
            finish()
        }

        binding.btnSave.setOnClickListener {
            val quantidadeTreinos = binding.Training.text.toString()
            val quantidadeAcademias = binding.Gyms.text.toString()

            val treinosNumero = quantidadeTreinos.toIntOrNull()
            val academiasNumero = quantidadeAcademias.toIntOrNull()

            if (quantidadeTreinos.isNotEmpty() && quantidadeAcademias.isNotEmpty()) {
                if (treinosNumero != null && academiasNumero != null) {
                    val intent = Intent(this, GymsNameActivity::class.java)
                    intent.putExtra("quantidadeTreinos", quantidadeTreinos)
                    intent.putExtra("quantidadeAcademias", quantidadeAcademias)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Insira apenas números válidos!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }
    }

    private fun goToMenu(){
        startActivity(Intent(this,MenuActivity::class.java))
        finish()
    }
}
