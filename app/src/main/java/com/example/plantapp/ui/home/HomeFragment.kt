package com.example.plantapp.ui.home


import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.plantapp.databinding.FragmentHomeBinding
import com.example.plantapp.ml.AutoModel3
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var outputTextView: TextView
    private var GALLERY_REQUEST_CODE = 123

    private lateinit var db: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        imageView = binding.imageView
        button = binding.bntCaptureImage
        outputTextView = binding.outputTextView
        val buttonLoad = binding.btnLoadImage

        //take image button
        binding.bntCaptureImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takePicturePreview.launch(null)
            } else {
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        //load image button
        buttonLoad.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onResult.launch(intent)
            } else {
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        //to redirect user to google search for the scientific name
        outputTextView.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${outputTextView.text}"))
            startActivity(intent)
        }
        //to download image when longPress on ImageView
        imageView.setOnLongClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@setOnLongClickListener true
        }

        // Button tıklandığında Firestore'a veri kaydet
        binding.btnSaveFirestore.setOnClickListener {
            // Bitki bilgilerini Firestore'a kaydet
            savePlantToFirestore()
        }
    }

    //Firestore'a veri kaydetme (taranan bitki adı ve resmi kaydediyor)
    private fun savePlantToFirestore() {
        // Kullanıcının UID'sini al
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        if (userUid != null) {
            val plantName = outputTextView.text.toString()

            if (plantName.isNotEmpty()) {
                val usersCollection = db.collection("Users")
                val userPlantsCollection = usersCollection.document(userUid).collection("Plants")

                // Resmin URI'sini al
                val imageUri = getImageUriFromImageView(imageView)

                if (imageUri != null) {
                    // "Plants" koleksiyonuna ekleme işlemi yap
                    val plantData = hashMapOf(
                        "plantName" to plantName,
                        "imageUrl" to imageUri.toString()
                    )

                    userPlantsCollection.add(plantData)
                        .addOnSuccessListener { documentReference ->
                            // Başarılı bir şekilde eklenirse buraya gelir
                            Log.d("Firestore", "Bitki başarıyla eklendi. Belge ID: ${documentReference.id}")
                            Toast.makeText(requireContext(), "Bitki başarıyla kaydedildi.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // Eklerken bir hata olursa buraya gelir
                            Log.w("Firestore", "Hata oluştu", e)
                            Toast.makeText(requireContext(), "Bitki kaydedilirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Resim seçilmedi.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Bitki adı boş olamaz.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("Firestore", "Kullanıcı UID'si null. Kullanıcı girişi yapılı değil.")
            Toast.makeText(requireContext(), "Kullanıcı girişi yapılı değil.", Toast.LENGTH_SHORT).show()
        }
    }
    //ImageView'dan Uri almak için (savePlantToFirestore içeriisnde kullanımak için bu fonksiyonu oluşturduk)
    private fun getImageUriFromImageView(imageView: ImageView): Uri? {
        val drawable = imageView.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            // Bitmap'i dosyaya yaz ve dosyanın URI'sini al
            return saveBitmapAndGetUri(requireContext(), bitmap)
        }
        return null
    }
    //Bitmap'i dosyaya yaz ve dosyanın URI'sini al(savePlantToFirestore içeriisnde kullanımak için bu fonksiyonu oluşturduk)
    private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "plant_image_${System.currentTimeMillis()}.png")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }


    //Kamera izni
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePicturePreview.launch(null)
            } else {
                Toast.makeText(requireContext(), "Permission Denied! Try Again.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    //Kameradan resim alma
    private val takePicturePreview =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                outputGenerator(bitmap)
            }
        }
    //get image
    private val onResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i("TAG", "This is the result: ${result.data} ${result.resultCode}")
            onResultRecived(GALLERY_REQUEST_CODE, result)
        }
    //get image result
    private fun onResultRecived(requestCode: Int, result: ActivityResult?) {
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (result?.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        Log.i("TAG", "onResultRecived: $uri")
                        val bitmap =
                            BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                } else {
                    Log.e("TAG", "onActivityResult: error in selecting image")
                }
            }
        }
    }
    //Output generator
    private fun outputGenerator(bitmap: Bitmap) {
        //declaring tensorflow lite model veriable
        val plantmodel = AutoModel3.newInstance(requireContext())

        // Converting bitmap into tensorflow image
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

        // Process the image using trained model and sort it in descending order
        val outputs = plantmodel.process(tfimage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score }
            }

        // Getting result having high probability
        val highProbabilityOutput = outputs[0]

        // Setting output text
        outputTextView.text = highProbabilityOutput.label
        Log.i("TAG", "outputGenerator: $highProbabilityOutput")

        // Releases model resources if no longer used.
        plantmodel.close()
    }
    //cihaza indirme
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                AlertDialog.Builder(requireContext()).setTitle("Download Image?")
                    .setMessage("Do you want to download this image to your device?")
                    .setPositiveButton("Yes") { _, _ ->
                        val drawable: BitmapDrawable = imageView.drawable as BitmapDrawable
                        val bitmap = drawable.bitmap
                        downloadImage(bitmap)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please allow permission to download image",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    //Bitmap'i alıp kullanıcının cihazına kaydeden fonksiyon
    private fun downloadImage(mBitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "Plants_Images" + System.currentTimeMillis() / 1000
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (uri != null) {
            requireContext().contentResolver.insert(uri, contentValues)?.also {
                requireContext().contentResolver.openOutputStream(it).use { outputStream ->
                    if (!outputStream?.let { it1 ->
                            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, it1)
                        }!!
                    ) {
                        throw IOException("Could'nt save the bitmap")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Image Saved",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                return it
            }
        }
        return null
    }
}


