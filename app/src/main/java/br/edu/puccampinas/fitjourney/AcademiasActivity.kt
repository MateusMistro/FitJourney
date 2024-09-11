package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityAcademiasBinding

private lateinit var binding: ActivityAcademiasBinding

class AcademiasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAcademiasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val treino = intent.getStringExtra("treino")

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this,TreinosActivity::class.java))
            finish()
        }

        binding.btnSantaFe.setOnClickListener {
            when (treino) {
                "A" -> {
                    val intent = Intent(this,TreinoAActivity::class.java)
                    intent.putExtra("academia", "SF")
                    startActivity(intent)
                    finish()
                }
                "B" -> {

                }
                "C" -> {

                }
            }
        }

        binding.btnForcaBruta.setOnClickListener {
            when (treino) {
                "A" -> {
                    val intent = Intent(this,TreinoAActivity::class.java)
                    intent.putExtra("academia", "FB")
                    startActivity(intent)
                    finish()
                }
                "B" -> {

                }
                "C" -> {

                }
            }
        }
    }
}
