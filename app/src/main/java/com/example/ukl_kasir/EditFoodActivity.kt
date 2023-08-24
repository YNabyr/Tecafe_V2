package com.example.ukl_kasir

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.ukl_kasir.databinding.ActivityDetailFoodBinding
import com.example.ukl_kasir.databinding.ActivityEditFoodBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class EditFoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditFoodBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageRef: StorageReference
    private var selectedImage: Bitmap? = null
    private var selectedMenuType: String = ""

    private var menuId: String? = null
    private var oldImageName: String? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebaseComponents()
        getMenuDetailsFromIntent()

        binding.ivFoodImage.setOnClickListener {
            showImagePicker()
        }

        binding.btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun initializeFirebaseComponents() {
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    private fun loadMenuImage() {
        Glide.with(this)
            .load(oldImageName)
            .into(binding.ivFoodImage)
    }
    private fun getMenuDetailsFromIntent() {
        menuId = intent.getStringExtra("id")
        oldImageName = intent.getStringExtra("gambar")
        val menuName = intent.getStringExtra("nama")
        val menuPrice = intent.getStringExtra("harga")
        val menuType = intent.getStringExtra("type")

        populateMenuDetails(menuName, menuPrice, menuType)
        loadMenuImage()
    }

    private fun populateMenuDetails(name: String?, price: String?, type: String?) {
        binding.edtMenuName.setText(name)
        binding.edtMenuPrice.setText(price)
        binding.rbMakanan.isChecked = type == "Makanan"
        binding.rbMinuman.isChecked = type == "Minuman"
    }

    private fun saveChanges() {
        val menuName = binding.edtMenuName.text.toString()
        val newPrice = binding.edtMenuPrice.text.toString()
        selectedMenuType = if (binding.rbMakanan.isChecked) "Makanan" else "Minuman"

        if (selectedImage != null) {
            uploadImage()
        } else {
            updateFirestoreData()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
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
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    private fun uploadImage() {
        selectedImage?.let { image ->
            val imageRef = storage.reference.child("food_images/${menuId}.jpg")
            val imageByteArray = convertBitmapToByteArray(image)

            val uploadTask = imageRef.putBytes(imageByteArray)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                    updateFirestoreData(imageUrl.toString())
                }.addOnFailureListener { exception ->
                    showError("Error uploading image: ${exception.message}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    selectedImage = data?.extras?.get("data") as Bitmap
                    binding.ivFoodImage.setImageBitmap(selectedImage)
                }

                GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    selectedImage =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    binding.ivFoodImage.setImageBitmap(selectedImage)
                }
            }
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun updateFirestoreData(imageUrl: String? = null) {
        menuId?.let {
            val newData = hashMapOf(
                "menuName" to binding.edtMenuName.text.toString(),
                "menuPrice" to binding.edtMenuPrice.text.toString(),
                "menuType" to selectedMenuType
            )
            imageUrl?.let {
                newData["menuImage"] = imageUrl
            }

            db.collection("menu").document(it)
                .update(newData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Menu updated successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back to DetailFoodActivity or any other appropriate screen
                }
                .addOnFailureListener { exception ->
                    showError("Error updating menu: ${exception.message}")
                }
        }
    }
}