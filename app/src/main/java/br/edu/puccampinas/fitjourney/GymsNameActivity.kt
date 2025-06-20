package br.edu.puccampinas.fitjourney

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.puccampinas.fitjourney.databinding.ActivityGymsNameBinding

private lateinit var binding: ActivityGymsNameBinding

class GymsNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGymsNameBinding.inflate(layoutInflater)
        val quantidadeTreinos = intent.getStringExtra("quantidadeTreinos")
        val quantidadeAcademias = intent.getStringExtra("quantidadeAcademias")
        val numAcademias = quantidadeAcademias?.toIntOrNull() ?: 0
        val layout = binding.Academias

        for (i in 1..numAcademias) {
            val editText = EditText(this)
            editText.hint = "Nome da academia $i"
            editText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            editText.setPadding(20, 20, 20, 20)
            editText.setBackgroundResource(R.drawable.borda)
            layout.addView(editText)
        }

    }
}
