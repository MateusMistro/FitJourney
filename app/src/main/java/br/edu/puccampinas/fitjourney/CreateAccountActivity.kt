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

        enableEdgeToEdge() // Habilita layout edge-to-edge (bordas da tela)

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnCreate.setOnClickListener {
            validateAndCreateUser()
        }
    }

    private fun validateAndCreateUser() {
        // Recupera valores dos campos
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val name = binding.etName.text.toString()

        // Verifica se os campos estão preenchidos corretamente
        when {
            name.isEmpty() -> negativeMessage(binding.root, "Preencha seu nome")
            email.isEmpty() -> negativeMessage(binding.root, "Preencha seu email")
            password.isEmpty() -> negativeMessage(binding.root, "Preencha sua senha")
            password.length < 6 -> negativeMessage(binding.root, "A senha precisa ter pelo menos seis caracteres")
            password != confirmPassword -> negativeMessage(binding.root, "As senhas não coincidem")

            else -> {
                // Verifica se o email já está cadastrado na coleção "user" do Firestore
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
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Salva os dados no Firestore
                                        saveDataInFrestore(name, email, password)

                                        // Realiza login automático após cadastro
                                        auth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(this) { task ->
                                                if (task.isSuccessful) {
                                                    Log.d(ContentValues.TAG, "signInWithEmail:success")

                                                    val user = auth.currentUser
                                                    val userId = user?.uid

                                                    // Verifica se o documento do usuário foi salvo corretamente
                                                    val userDocRef = FirebaseFirestore.getInstance()
                                                        .collection("pessoa").document(userId!!)

                                                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                                                        if (documentSnapshot.exists()) {
                                                            startActivity(Intent(this, MenuActivity::class.java))
                                                        }
                                                    }
                                                } else {
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

    private fun saveDataInFrestore(name: String, email: String, password: String) {
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
                    positiveMessage(binding.root, "Erro ao enviar dados: $e")
                }
        }
    }

    private fun negativeMessage(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#F3787A")) // Cor vermelha
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }

    private fun positiveMessage(view: View, mensagem: String) {
        val snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#78F37A")) // Cor verde
        snackbar.setTextColor(Color.parseColor("#FFFFFF"))
        snackbar.show()
    }
}
