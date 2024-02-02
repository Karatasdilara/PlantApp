package com.example.plantapp.ui.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.plantapp.R
import com.example.plantapp.ui.login.LoginActivity
import com.example.plantapp.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class NotificationsFragment : Fragment() {
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        setHasOptionsMenu(true) // Bu, Fragment'ın kendi menüsünü kullanacağını belirtir

        // SharedPreferences objesini oluşturun
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signOutButton.setOnClickListener {
            // Kullanıcı oturumunu kapatırken SharedPreferences'ten bilgileri temizlenir
            with(sharedPreferences.edit()) {
                remove("username")
                remove("email")
                apply()
            }
            auth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
        // Bilgileri güncelle butonuna tıklandığında
        binding.updateInformationButton.setOnClickListener {
            // Update Information butonuna tıklandığında yapılacak işlemler
            val newUsername = binding.editTextNewUsername.text.toString()
            val newEmail = binding.editTextNewEmail.text.toString()
            // Yeni bilgileri SharedPreferences'e kaydet
            with(sharedPreferences.edit()) {
                putString("username", newUsername)
                putString("email", newEmail)
                apply()
            }

            // Firestore'daki bilgileri güncelle
            updateInformationInFirestore(newUsername, newEmail )

            // UI elemanlarına güncellenmiş bilgileri ata
            binding.editTextNewUsername.setText(newUsername)
            binding.editTextNewEmail.setText(newEmail)

            // Kullanıcıya güncelleme başarılı mesajını göstermek için Toast kullanabilirsiniz
            Toast.makeText(context, "Bilgiler güncellendi.", Toast.LENGTH_LONG).show()
        }
        // KVKK TextView'ına tıklandığında
        binding.kvkkTextView.setOnClickListener {
            showCustomDialog(getString(R.string.kvkk_text))
        }
        // About Us TextView'ına tıklandığında
        binding.aboutUsTextView.setOnClickListener {
            showCustomDialog(getString(R.string.about_us_text))
        }
        // About Us TextView'ına tıklandığında
        binding.contactUsTextView.setOnClickListener {
            showCustomDialog(getString(R.string.contact_us_text))
        }

        // Kullanıcı ID'sini kontrol etme
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            Log.d("NotificationsFragment", "Current User ID: $userId")

            // SharedPreferences'ten bilgileri al
            val username = sharedPreferences.getString("username", "")
            val email = sharedPreferences.getString("email", "")

            // SharedPreferences'te bilgi yoksa Firestore'dan çek
            if (username.isNullOrBlank() || email.isNullOrBlank() ) {
                loadUsernameFromFirestore(userId)
            } else {
                // SharedPreferences'ten gelen bilgileri UI elemanlarına ata

                binding.editTextNewUsername.setText(username)
                binding.editTextNewEmail.setText(email)

                // Firebase'den çekilen e-posta adresi editable=false yap (değiştirilemez)
                binding.editTextNewEmail.isEnabled = false
            }
        } else {
            Log.d("NotificationsFragment", "User is not signed in.")
        }
    }

    // onViewCreated içindeki loadUsernameFromFirestore'u çağırıyoruz
    private fun loadUsernameFromFirestore(userId: String) {
        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val email = document.getString("email")

                    // SharedPreferences'e bilgileri kaydet
                    with(sharedPreferences.edit()) {
                        putString("username", username)
                        putString("email", email)
                        apply()
                    }

                    // UI elemanlarına bilgileri ata

                    binding.editTextNewUsername.setText(username)
                    binding.editTextNewEmail.setText(email)
                    Log.d("EditTextDebug", "editTextNewUsername: ${binding.editTextNewUsername}")

                } else {
                    Log.d("NotificationsFragment", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("NotificationsFragment", "get failed with ", exception)
            }
    }

    // onViewCreated içindeki updateInformationInFirestore'u çağırıyoruz
    private fun updateInformationInFirestore(newUsername: String, newEmail: String ) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userData = hashMapOf(
                "username" to newUsername,
                "email" to newEmail,
            )

            db.collection("Users")
                .document(userId)
                .update(userData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("NotificationsFragment", "Bilgiler güncellendi.")
                }
                .addOnFailureListener { e ->
                    Log.w("NotificationsFragment", "Bilgi güncelleme başarısız.", e)
                }
        }
    }

    private fun showCustomDialog(content: String) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_dialog, null)
        builder.setView(dialogView)

        val contentTextView: TextView = dialogView.findViewById(R.id.contentTextView)
        contentTextView.text = content

        val okButton: Button = dialogView.findViewById(R.id.okButton)
        val dialog = builder.create()

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}