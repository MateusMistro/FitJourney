package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityTreinosBinding

private lateinit var binding: ActivityTreinosBinding

class TreinosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_treinos)
        binding = ActivityTreinosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVoltar.setOnClickListener{
            startActivity(Intent(this,MenuActivity::class.java))
            finish()
        }

        binding.btnTreinoA.setOnClickListener {
            val intent = Intent(this, AcademiasActivity::class.java)
            intent.putExtra("treino", "A")
            startActivity(intent)
            finish()
        }

        binding.btnTreinoB.setOnClickListener {
            val intent = Intent(this, AcademiasActivity::class.java)
            intent.putExtra("treino", "B")
            startActivity(intent)
            finish()
        }

        binding.btnTreinoC.setOnClickListener {
            val intent = Intent(this, AcademiasActivity::class.java)
            intent.putExtra("treino", "C")
            startActivity(intent)
            finish()
        }

        binding.btnTreinoD.setOnClickListener {
            val intent = Intent(this, AcademiasActivity::class.java)
            intent.putExtra("treino", "D")
            startActivity(intent)
            finish()
        }

        binding.btnTreinoE.setOnClickListener {
            val intent = Intent(this, AcademiasActivity::class.java)
            intent.putExtra("treino", "E")
            startActivity(intent)
            finish()
        }
    }
}
