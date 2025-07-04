package br.edu.puccampinas.fitjourney

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityMenuBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityMenuBinding
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth
private var userId: String? = null

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        userId = currentUser?.uid

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.menu.setOnClickListener {
            goToMenu()
        }

        binding.btnTraining.setOnClickListener {
            goToTraining()
        }

        binding.btnDiets.setOnClickListener {
            goToDiets()
        }

        binding.btnPhotos.setOnClickListener {
            goToPhotos()
        }

        binding.btnAnthropometricAssessment.setOnClickListener {
            goToAnthropometricAssessment()
        }
    }

    private fun negativeMessage(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun positiveMessage(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A"))
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun goToMenu(){
        startActivity(Intent(this,MenuActivity::class.java))
        finish()
    }

    private fun logout(){
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun goToDiets(){
        startActivity(Intent(this,DietsActivity::class.java))
    }

    private fun goToPhotos(){
        startActivity(Intent(this,PhotosActivity::class.java))
    }

    private fun goToAnthropometricAssessment(){
        startActivity(Intent(this,AnthropometricAssessmentActivity::class.java))
    }

    private fun goToTraining(){
        if (userId != null) {
            db.collection("trainings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val intent = Intent(this, GymsActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, GeneralRegistrationActivity::class.java)
                        startActivity(intent)
                    }
                }
                .addOnFailureListener { exception ->
                    negativeMessage(binding.root,"Erro ao acessar dados: ${exception.message}")
                }
        } else {
            negativeMessage(binding.root,"Usuário não autenticado")
        }
    }
}
