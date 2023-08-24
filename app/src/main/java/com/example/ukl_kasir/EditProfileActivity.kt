package com.example.ukl_kasir

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.ukl_kasir.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageRef: StorageReference
    private var selectedImage: Bitmap? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.ivProfileImage.setOnClickListener {
            showImagePicker()
        }

        binding.btnSave.setOnClickListener {
            binding.pbLoading.visibility = View.VISIBLE
            // Upload image to Firebase Storage and then save data in Firestore
            uploadImage()
        }
    }

    private fun showImagePicker() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image from:")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                selectedImage = data.extras?.get("data") as Bitmap
                binding.ivProfileImage.setImageBitmap(selectedImage)
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                val imageUri = data.data
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                binding.ivProfileImage.setImageBitmap(selectedImage)
            }
        }
    }

    private fun uploadImage() {
        if (selectedImage != null) {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val imageByteArray = convertBitmapToByteArray(selectedImage!!)
                imageRef = storage.reference.child("profile_images/$userId.jpg")
                val uploadTask = imageRef.putBytes(imageByteArray)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                        saveDataToFirestore(imageUrl.toString())
                    }
                }.addOnFailureListener { exception ->
                    showError("Image upload failed: ${exception.message}")
                }
            } else {
                showError("User not authenticated")
            }
        } else {
            showError("Select an image first")
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun saveDataToFirestore(imageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val name = binding.edtName.text.toString()
            val age = binding.edtAge.text.toString()
            val bio = binding.edtBio.text.toString()

            val profileData = ProfileModel(userId, name, age, bio, imageUrl)

            db.collection("profile").document(userId).set(profileData)
                .addOnSuccessListener {
                    binding.pbLoading.visibility = View.GONE
                    val intent = Intent(this, KasirActivity::class.java)
                    startActivity(intent)
                    showToastMessage("Profile data saved successfully.")
                    finish()
                }
                .addOnFailureListener { exception ->
                    showError("Error saving profile data: ${exception.message}")
                }
        } else {
            showToastMessage("User not authenticated")
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}