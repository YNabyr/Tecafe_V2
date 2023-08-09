package com.example.ukl_kasir

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ukl_kasir.databinding.ActivityKasirBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class KasirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKasirBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKasirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set default fragment when the activity starts
        val defaultFragment = HomeFragment()
        replaceFragment(defaultFragment)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    val profileFragment = ProfileFragment()
                    replaceFragment(profileFragment)
                    true
                }
                R.id.home -> {
                    val homeFragment = HomeFragment()
                    replaceFragment(homeFragment)
                    true
                }
                R.id.settings -> {
                    val settingsFragment = SettingsFragment()
                    replaceFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    // Function to update user profile data in Firestore
    fun updateUserProfileData(userName: String, userAge: Int, userBio: String) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userMap = hashMapOf(
                "name" to userName,
                "age" to userAge,
                "bio" to userBio
            )

            db.collection("User").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "User data updated successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to upload profile image to Firebase Cloud Storage
    fun uploadProfileImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val storageRef = storage.reference.child("profile_images").child(userId)
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                Toast.makeText(this, "Profile image uploaded successfully.", Toast.LENGTH_SHORT).show()

                // Get the URL of the uploaded image and save it to Firestore
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()
                    db.collection("User").document(userId)
                        .update("profileImage", profileImageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile image URL saved.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Failed to save profile image URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to delete profile image from Firebase Cloud Storage
    fun deleteProfileImage() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val storageRef = storage.reference.child("profile_images").child(userId)

            storageRef.delete().addOnSuccessListener {
                db.collection("User").document(userId)
                    .update("profileImage", null)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile image deleted successfully.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to delete profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to delete profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to show Toast message
    fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Function to get the user ID
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }
}