package br.edu.puccampinas.fitjourney

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var auth: FirebaseAuth
private lateinit var binding: ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Para usar layout em tela cheia
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Se já estiver logado, vai direto para o menu
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToMenu()
            return
        }

        binding.etCreate.setOnClickListener {
            createAccount()
        }

        binding.btnEnter.setOnClickListener {
            validateAndLogin()
        }
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        // Validação dos campos obrigatórios
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            when {
                email.isEmpty() -> {
                    negativeMessage(binding.root, "Preencha seu email")
                }
                password.isEmpty() -> {
                    negativeMessage(binding.root, "Preencha sua senha")
                }
                password.length <= 5 -> {
                    negativeMessage(binding.root, "A senha precisa ter pelo menos seis caracteres")
                }
            }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        val userDocRef = FirebaseFirestore.getInstance()
                            .collection("user")
                            .document(userId!!)
                        userDocRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                goToMenu()
                            }
                        }
                    } else {
                        Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                        negativeMessage(binding.root, "Email ou senha inválidos, tente novamente")
                    }
                }
        }
    }

    private fun negativeMessage(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A")) // vermelho
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun positiveMessage(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A")) // verde
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun goToMenu() {
        startActivity(Intent(this, MenuActivity::class.java))
        finish() // Finaliza a LoginActivity para que não possa voltar
    }

    private fun createAccount() {
        startActivity(Intent(this, CreateAccountActivity::class.java))
    }
}
