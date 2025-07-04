package br.edu.puccampinas.fitjourney

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.puccampinas.fitjourney.databinding.ActivityCreateAccountBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var binding: ActivityCreateAccountBinding
private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configuração do clique do botão de efetuar cadastro
        binding.btnCreate.setOnClickListener {
            validateAndCreateUser()
        }
    }

    private fun validateAndCreateUser(){
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val name = binding.etName.text.toString()

        // Verificação dos campos obrigatórios e das regras de validação
        when {
            name.isEmpty() -> {
                negativeMessage(binding.root, "Preencha seu nome")
            }
            email.isEmpty() -> {
                negativeMessage(binding.root, "Preencha seu email")
            }
            password.isEmpty() -> {
                negativeMessage(binding.root, "Preencha sua senha")
            }
            password.length < 6 -> {
                negativeMessage(binding.root, "A senha precisa ter pelo menos seis caracteres")
            }
            password != confirmPassword -> {
                negativeMessage(binding.root, "As senhas não coincidem")
            }
            else -> {
                // Verifica se o email já está em uso
                db.collection("user")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { people ->
                        var accountAlreadyExists = false

                        people.forEach { person ->
                            if (person.getString("email") == email) {
                                accountAlreadyExists = true
                                return@forEach
                            }
                        }

                        if (accountAlreadyExists) {
                            negativeMessage(binding.root, "O email fornecido já está em uso")
                        } else {
                            // Criação da conta no Firebase Auth
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Salva os dados no Firestore
                                        saveDataInFrestore(name, email, password)
                                        auth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(this) { task ->
                                                if (task.isSuccessful) {
                                                    // Se o login for bem-sucedido, vai para a tela principal do cliente
                                                    Log.d(ContentValues.TAG, "signInWithEmail:success")

                                                    val user = auth.currentUser
                                                    val userId = user?.uid

                                                    val userDocRef = FirebaseFirestore.getInstance().collection("pessoa").document(userId!!)
                                                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                                                        if (documentSnapshot.exists()) {
                                                            startActivity(Intent(this, MenuActivity::class.java))
                                                        }
                                                    }
                                                } else {
                                                    // Se o login falhar, exibe uma mensagem de erro
                                                    Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                                                    negativeMessage(binding.root, "Falha na autenticação, tente novamente")
                                                }
                                                this.finish()
                                            }
                                    } else {
                                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                                        negativeMessage(binding.root, "Falha na criação da conta: ${task.exception?.message}")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", exception)
                                    negativeMessage(binding.root, "Falha na criação da conta: ${exception.message}")
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Firestore query failed", exception)
                        negativeMessage(binding.root, "Erro ao verificar o email: ${exception.message}")
                    }
            }
        }
    }

    private fun saveDataInFrestore(name: String,email: String, password: String) {
        val pessoaMap = hashMapOf(
            "nome" to name,
            "email" to email,
            "senha" to password,
        )

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("user").document(it.uid)
                .set(pessoaMap)
                .addOnSuccessListener {
                    positiveMessage(binding.root, "Bem-vindo")
                }
                .addOnFailureListener { e ->
                    positiveMessage(binding.root,"Erro ao enviar dados: $e")
                }
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
}
