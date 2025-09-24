package com.example.ocrpoc

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ocrpoc.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedImageUri: Uri? = null
    private lateinit var textRecognizer: TextRecognizer

    // Activity result launchers
    private lateinit var photoUri: Uri
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
            displayImage(photoUri)
            binding.btnProcessImage.isEnabled = true
        }
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                selectedImageUri = imageUri
                displayImage(imageUri)
                binding.btnProcessImage.isEnabled = true
            }
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ML Kit Text Recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        setupUI()
        checkPermissions()
    }


    private fun setupUI() {
        binding.btnTakePhoto.setOnClickListener {
            if (checkCameraPermission()) {
                takePhoto()
            } else {
                requestPermissions()
            }
        }

        binding.btnSelectImage.setOnClickListener {
            if (checkStoragePermission()) {
                selectImage()
            } else {
                requestPermissions()
            }
        }

        binding.btnProcessImage.setOnClickListener {
            selectedImageUri?.let { uri ->
                processImage(uri)
            }
        }

        binding.btnCopyText.setOnClickListener {
            copyTextToClipboard()
        }
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val storagePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return cameraPermission && storagePermission
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private fun takePhoto() {
        val imageFile = File.createTempFile("IMG_", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
        takePhotoLauncher.launch(photoUri)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun displayImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(binding.imageView)
    }


    private fun processImage(uri: Uri) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnProcessImage.isEnabled = false

        try {
            val bitmap = loadBitmapFromUri(uri)
            val image = InputImage.fromBitmap(bitmap, 0)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnProcessImage.isEnabled = true
                    
                    if (visionText.text.isNotEmpty()) {
                        binding.textViewExtractedText.text = visionText.text
                        binding.btnCopyText.isEnabled = true
                        Toast.makeText(this@MainActivity, "Text extracted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        binding.textViewExtractedText.text = "No text found in the image"
                        binding.btnCopyText.isEnabled = false
                        Toast.makeText(this@MainActivity, "No text detected", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.btnProcessImage.isEnabled = true
                    binding.textViewExtractedText.text = "Error processing image: ${e.message}"
                    binding.btnCopyText.isEnabled = false
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnProcessImage.isEnabled = true
            binding.textViewExtractedText.text = "Error processing image: ${e.message}"
            binding.btnCopyText.isEnabled = false
            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream) ?: throw Exception("Could not load image")
    }



    private fun copyTextToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Extracted Text", binding.textViewExtractedText.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        textRecognizer.close()
    }
}