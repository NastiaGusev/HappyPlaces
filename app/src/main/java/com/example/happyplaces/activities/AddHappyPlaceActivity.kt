package com.example.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddHappyPlaceBinding
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var savedImageToInternalStorage: Uri? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var happyPlacesDetails: HappyPlaceModel? = null

    companion object {
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    private var cameraResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //get the data from the camera activity
                val bitmap = result.data?.extras?.get("data") as Bitmap
                //set photo
                binding.ivPlaceImage.setImageBitmap(bitmap)
                savedImageToInternalStorage = saveImageToInternalStorage(bitmap)
                Log.d("LOG", savedImageToInternalStorage.toString())
            }
        }

    private var galleryResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //get the data from the camera activity
                val contentUri = result.data?.data
                //set photo
                try {
                    val selectedImageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                    binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    savedImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                    Log.d("LOG", savedImageToInternalStorage.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBarAddPlaces)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBarAddPlaces.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlacesDetails =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel?
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateView()
        }
        updateDateView()

        if (happyPlacesDetails != null) {
            supportActionBar?.title = "Edit Happy Place"
            binding.etTitle.setText(happyPlacesDetails!!.title)
            binding.etDescription.setText(happyPlacesDetails!!.description)
            binding.etDate.setText(happyPlacesDetails!!.date)
            binding.etLocation.setText(happyPlacesDetails!!.location)
            latitude = happyPlacesDetails!!.latitude
            longitude = happyPlacesDetails!!.longitude

            savedImageToInternalStorage = Uri.parse(happyPlacesDetails!!.image)
            binding.ivPlaceImage.setImageURI(savedImageToInternalStorage)

            binding.btnSave.text = "UPDATE"
        }
        binding.etDate.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

    }

    private fun updateDateView() {
        val myFormat = "dd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(simpleDateFormat.format(cal.time).toString())
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            binding.etDate.id -> {

                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_WEEK)
                ).show()
            }
            binding.tvAddImage.id -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action:")
                val pictureDialogItems =
                    arrayOf("Select photo from Gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePicture()
                    }
                }
                pictureDialog.show()
            }
            binding.btnSave.id -> {
                when {
                    binding.etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()
                    }
                    binding.etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter description", Toast.LENGTH_LONG).show()
                    }
                    binding.etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter location", Toast.LENGTH_LONG).show()
                    }
                    savedImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            if(happyPlacesDetails == null) 0 else happyPlacesDetails!!.id,
                            binding.etTitle.text.toString(),
                            savedImageToInternalStorage.toString(),
                            binding.etDescription.text.toString(),
                            binding.etDate.text.toString(),
                            binding.etLocation.text.toString(),
                            latitude,
                            longitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if(happyPlacesDetails == null) {
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                            if (addHappyPlace > 0) {
                                Toast.makeText(
                                    this,
                                    "The happy place details are inserted successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                            if (updateHappyPlace > 0) {
                                Toast.makeText(
                                    this,
                                    "The happy place details were updated successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }

            }
        }
    }

    private fun takePicture() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.CAMERA
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(report: PermissionGrantedResponse?) {
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "Camera permission is granted",
                    Toast.LENGTH_LONG
                ).show()
                //Intent starts the camera
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(intent)
            }

            override fun onPermissionDenied(report: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "Camera permission is denied",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onPermissionRationaleShouldBeShown(
                permission: PermissionRequest,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check();
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResultLauncher.launch(galleryIntent)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissons: MutableList<PermissionRequest>,
                token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check();
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage(
                "It looks like you have turned off permission that is required for this" +
                        " feature. It can be enabled under the Application Settings"
            )
            .setPositiveButton("Go to settings")
            { _, _ ->
                try {
                    //Settings page
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    //Send the user to the application settings where he can directly change the permissions
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel")
            { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

