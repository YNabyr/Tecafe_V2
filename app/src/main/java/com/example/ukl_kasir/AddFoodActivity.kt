package com.example.ukl_kasir

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ukl_kasir.databinding.ActivityAddFoodBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class AddFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddFoodBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageRef: StorageReference
    private var selectedImage: Bitmap? = null
    private var selectedmenuType: String = ""

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.ivFoodImage.setOnClickListener {
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
        startActivityForResult(intent, AddFoodActivity.CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, AddFoodActivity.GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AddFoodActivity.CAMERA_REQUEST_CODE && data != null) {
                selectedImage = data.extras?.get("data") as Bitmap
                binding.ivFoodImage.setImageBitmap(selectedImage)
            } else if (requestCode == AddFoodActivity.GALLERY_REQUEST_CODE && data != null) {
                val imageUri = data.data
                selectedImage = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                binding.ivFoodImage.setImageBitmap(selectedImage)
            }
        }
    }

    private fun uploadImage() {
        if (selectedImage != null) {
            val menuId = db.collection("menu").document().id
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val imageByteArray = convertBitmapToByteArray(selectedImage!!)
                imageRef = storage.reference.child("food_images/${menuId}.jpg")
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

        val menuName = binding.edtMenuName.text.toString()
        val menuPrice = binding.edtMenuPrice.text.toString()
        val selectedMenuTypeId = binding.rgMenuType.checkedRadioButtonId
        val selectedRoleRadio = findViewById<RadioButton>(selectedMenuTypeId)
        val menuType = selectedRoleRadio.text.toString()

        if (menuName.isNotBlank() && menuPrice.isNotBlank() && menuType.isNotBlank()) {
            // Generate a unique ID for the new menu document
            val menuId = db.collection("menu").document().id

            val menuData = FoodModel(menuId, menuName, menuType, menuPrice.toInt(), imageUrl)
            db.collection("menu")
                .document(menuId) // Use the generated menuId as the document ID
                .set(menuData)
                .addOnSuccessListener {
                    binding.pbLoading.visibility = View.GONE
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    showToastMessage("Menu data saved successfully.")
                    finish()
                }
                .addOnFailureListener { exception ->
                    showError("Error saving menu data: ${exception.message}")
                }
        } else {
            showError("Please fill in all the fields.")
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        binding.pbLoading.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}