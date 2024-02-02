package com.example.plantapp.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plantapp.R
import com.example.plantapp.adapter.PlantRecyclerAdapter
import com.example.plantapp.databinding.FragmentDashboardBinding
import com.example.plantapp.model.Plant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var plantList: MutableList<Plant>
    private lateinit var recyclerView: RecyclerView
    private lateinit var plantRecyclerAdapter: PlantRecyclerAdapter
    private lateinit var binding: FragmentDashboardBinding
    // Galeriye gitmek için kullanılacak request code
    private val GALLERY_REQUEST_CODE = 123

    // Seçilen bitkinin pozisyonunu ve resim değişikliği için kullanılacak olan sabit
    private var selectedPlantPosition: Int = -1
    private lateinit var selectedPlantImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase Firestore referansını başlat
        db = FirebaseFirestore.getInstance()

        // Bitki listesini tutacak boş bir liste oluştur
        plantList = mutableListOf()

        // RecyclerView ve adapter'ı başlat
        recyclerView = view.findViewById(R.id.recyclerView)
        plantRecyclerAdapter = PlantRecyclerAdapter(requireContext(), plantList, this, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = plantRecyclerAdapter

        plantRecyclerAdapter.setOnDeleteClickListener { position ->
            val plant = plantList[position]
            deletePlant(plant)
        }

        // Firestore'dan bitki listesini al ve RecyclerView'a ayarla
        loadPlantListFromFirestore()


        // RecyclerView'daki her bir öğeye tıklama işlemi için bir dinleyici ekle
        plantRecyclerAdapter.setOnItemClickListener { position ->
            // Seçilen bitkinin pozisyonunu güncelle
            selectedPlantPosition = position
            // Galeriye gidip resim seçme işlemi için fonksiyonu çağır
            openGalleryForImage()
        }

    }

    // Galeriye gidip resim seçme işlemi için çağrılan fonksiyon
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // Galeriden seçilen resmi almak için onActivityResult metodunu kullan
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Seçilen resmin Uri bilgisini al
            val selectedImageUri = data?.data

            // Seçilen bitkinin pozisyonunu kontrol et
            if (selectedPlantPosition != -1 && selectedImageUri != null) {
                // Firestore'a seçilen resmi güncelle
                updatePlantImage(selectedPlantPosition, selectedImageUri)
            }
        }
    }



    private fun loadPlantListFromFirestore() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        if (userUid != null) {
            val usersCollection = db.collection("Users")
            val userPlantsCollection = usersCollection.document(userUid).collection("Plants")

            // Snapshot listener ekleyerek, veride herhangi bir değişiklik olduğunda tetiklenir.
            // Listeyi güncellemek için kullanılır. (Yeni bitki ekleme, bitki silme, bitki güncellemeden sonra)
            userPlantsCollection.addSnapshotListener { result, exception ->
                if (exception != null) {
                    Log.w("Firestore", "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (result != null) {
                    plantList.clear()

                    for (document in result) {
                        // Belge kimliğini al
                        val documentId = document.id

                        // Belge kimliğini içeren bitki nesnesini oluştur
                        val plant = document.toObject(Plant::class.java)
                        plant.documentId = documentId

                        plantList.add(plant)
                    }

                    plantRecyclerAdapter.notifyDataSetChanged()
                }
            }
        } else {
            Log.e("Firestore", "Kullanıcı UID'si null. Kullanıcı girişi yapılı değil.")
            Toast.makeText(requireContext(), "Kullanıcı girişi yapılı değil.", Toast.LENGTH_SHORT).show()
        }
    }


    // Firestore'da belgedeki resim URL'sini güncellemek için kullanılan fonksiyon / resim eklemek için kullanılan fonksiyon
    private fun updatePlantImage(position: Int, imageUri: Uri) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        if (userUid != null) {
            val usersCollection = db.collection("Users")
            val userPlantsCollection = usersCollection.document(userUid).collection("Plants")

            // Seçilen bitkinin pozisyonundaki belgeyi al
            val selectedPlant = plantList[position]

            // Yeni resim Uri'sini belgeye ekle
            selectedPlant.imageUrl = imageUri.toString()

            // Güncellenen değerleri belgeye eşitle
            selectedPlant.plantName = selectedPlant.plantName

            // Firestore'daki belgeyi güncelle
            userPlantsCollection.document(selectedPlant.documentId)
                .set(selectedPlant)
                .addOnSuccessListener {
                    // Başarılı bir şekilde güncellenirse buraya gelir
                    Log.d("Firestore", "Bitki resmi güncellendi.")
                    Toast.makeText(
                        requireContext(),
                        "Bitki resmi başarıyla güncellendi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    // Güncellenirken bir hata olursa buraya gelir
                    Log.w("Firestore", "Hata oluştu", e)
                    Toast.makeText(
                        requireContext(),
                        "Bitki resmi güncellenirken bir hata oluştu.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Log.e("Firestore", "Kullanıcı UID'si null. Kullanıcı girişi yapılı değil.")
            Toast.makeText(
                requireContext(),
                "Kullanıcı girişi yapılı değil.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //Firestore'dan bitkiyi silmek için kullanılan fonksiyon
    private fun deletePlant(plant: Plant) {
        Log.d("DeletePlant", "Delete Plant function started.")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val passengersCollection = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId!!)
            .collection("Plants")

        plant.documentId?.let { documentId ->
            Log.d("DeletePlant", "Plant ID: $documentId")
            passengersCollection.document(documentId).delete()
                .addOnSuccessListener {
                    Log.d("DeletePlant", "Successfully deleted plant.")
                    // Firestore'dan başarıyla silindi

                    // Silinen yolcuyu güncelleyerek RecyclerView'ı güncelleyin
                    //val updatedPlant = plantRecyclerAdapter.passengerList.toMutableList()
                    val updatedPlant = plantList.toMutableList()
                    updatedPlant.remove(plant)
                    //plantRecyclerAdapter.submitList(updatedPlant)
                    plantList = updatedPlant
                    Toast.makeText(requireContext(), "Plant successfully deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Firestore'dan silme işlemi başarısız oldu
                    Log.e("DeletePlant", "Error deleting plant", it)
                    Toast.makeText(requireContext(), "The plant could not be deleted!", Toast.LENGTH_SHORT).show()
                }
        }
    }


    fun getSelectedDaysFromCheckBoxes(radioGroup: RadioGroup): List<String> {
        val selectedDays = mutableListOf<String>()

        for (i in 0 until radioGroup.childCount) {
            val checkBox = radioGroup.getChildAt(i) as? CheckBox
            if (checkBox?.isChecked == true) {
                selectedDays.add(checkBox.text.toString())
            }
        }

        return selectedDays
    }


    // Firestore'da günleri güncellemek için fonksiyon
    // Firestore'da günleri güncellemek için fonksiyon
    fun updatePlantDays(position: Int, selectedDays: List<String>) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        if (userUid != null) {
            val usersCollection = db.collection("Users")
            val userPlantsCollection = usersCollection.document(userUid).collection("Plants")

            // Seçilen bitkinin pozisyonundaki belgeyi al
            val selectedPlant = plantList[position]

            // Firestore'dan belge çekildiğinde selectedDays'i güncelle
            userPlantsCollection.document(selectedPlant.documentId)
                .get()
                .addOnSuccessListener { document ->
                    // Firestore'dan belge çekildiğinde
                    val plantFromFirestore = document.toObject(Plant::class.java)
                    if (plantFromFirestore != null) {

                        // Yeni günleri belgeye ekle
                        plantFromFirestore.selectedDays = selectedDays

                        // Firestore'daki belgeyi güncelle
                        userPlantsCollection.document(selectedPlant.documentId)
                            .set(plantFromFirestore)
                            .addOnSuccessListener {
                                // Başarılı bir şekilde güncellenirse buraya gelir
                                Log.d("Firestore", "Bitki günleri güncellendi.")
                                Toast.makeText(
                                    context,
                                    "Bitki günleri başarıyla güncellendi.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                // Güncellenirken bir hata olursa buraya gelir
                                Log.w("Firestore", "Hata oluştu", e)
                                Toast.makeText(
                                    context,
                                    "Bitki günleri güncellenirken bir hata oluştu.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
        } else {
            Log.e("Firestore", "Kullanıcı UID'si null. Kullanıcı girişi yapılı değil.")
            Toast.makeText(
                context,
                "Kullanıcı girişi yapılı değil.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}
