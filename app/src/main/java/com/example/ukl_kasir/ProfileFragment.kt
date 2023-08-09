package com.example.ukl_kasir

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ukl_kasir.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var kasirActivity: KasirActivity
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var profileImageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kasirActivity = activity as KasirActivity
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set data user and profile image here
        // You can retrieve and display user data from Firestore in this section
        // and display the profile image if it is available in Firebase Cloud Storage

        // Upload profile image button click listener
        binding.btnUploadImage.setOnClickListener {
            selectProfileImage()
        }

        // Save user data button click listener
        binding.btnSaveData.setOnClickListener {
            val userName = binding.edtName.text.toString()
            val userAge = binding.edtAge.text.toString().toInt()
            val userBio = binding.edtBio.text.toString()

            updateUserProfileData(userName, userAge, userBio)
        }

        // Logout button click listener
        binding.btnLogout.setOnClickListener {
            logOutUser()
        }
    }

    private fun selectProfileImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                profileImageUri = imageUri
                binding.ivProfileImage.setImageURI(profileImageUri)
                uploadProfileImage(profileImageUri)
            }
        }
    }

    private fun updateUserProfileData(userName: String, userAge: Int, userBio: String) {
        val userId = kasirActivity.getUserId()

        if (userId != null) {
            val userMap = hashMapOf(
                "name" to userName,
                "age" to userAge,
                "bio" to userBio
            )

            db.collection("Profile").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    Toast.makeText(kasirActivity, "User data updated successfully.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(kasirActivity, "Failed to update user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val userId = kasirActivity.getUserId()

        if (userId != null) {
            val storageRef = storage.reference.child("profile_images").child(userId)
            val uploadTask = storageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                Toast.makeText(kasirActivity, "Profile image uploaded successfully.", Toast.LENGTH_SHORT).show()

                // Get the URL of the uploaded image and save it to Firestore
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()
                    db.collection("Profile").document(userId)
                        .update("profileImage", profileImageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(kasirActivity, "Profile image URL saved.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(kasirActivity, "Failed to save profile image URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(kasirActivity, "Failed to upload profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logOutUser() {
        auth.signOut()
        kasirActivity.showToastMessage("Logged out successfully.")
        kasirActivity.finish() // Close the activity or navigate to Login screen
    }
}