package fr.steph.showmemories.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import fr.steph.showmemories.models.ShowModel
import fr.steph.showmemories.repository.ShowRepository.Singleton.databaseRef
import fr.steph.showmemories.repository.ShowRepository.Singleton.showList
import fr.steph.showmemories.repository.ShowRepository.Singleton.showsListener
import fr.steph.showmemories.repository.ShowRepository.Singleton.storageRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ShowRepository {
    private val _shows = MutableLiveData<List<ShowModel>>(null)
    val shows: LiveData<List<ShowModel>> = _shows

    object Singleton {
        // Storage/Database connexions
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://tv-memories.appspot.com")
        val databaseRef = FirebaseDatabase.getInstance().getReference("shows")

        // List containing all shows
        val showList = arrayListOf<ShowModel>()

        var showsListener: ValueEventListener? = null
    }

    suspend fun addDatabaseListener() {
        withContext(Dispatchers.Default) {
            if (showsListener == null)
                showsListener = databaseRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        showList.clear()

                        for (ds in snapshot.children) {
                            val show = ds.getValue(ShowModel::class.java)
                            if (show != null) showList.add(show)
                        }
                        _shows.postValue(showList)
                    }

                    override fun onCancelled(p0: DatabaseError) {}
                })
        }
    }

    suspend fun insertShow(show: ShowModel, successCallback: () -> Unit, failureCallback: () -> Unit) {
        withContext(Dispatchers.IO) {
            databaseRef.child(show.id).setValue(show)
                .addOnSuccessListener { successCallback() }
                .addOnFailureListener { failureCallback() }
        }
    }

    suspend fun deleteShow(show: ShowModel, successCallback: () -> Unit, failureCallback: () -> Unit) {
        withContext(Dispatchers.IO) {
            deleteImage(show.imageUrl)
            databaseRef.child(show.id).removeValue()
                .addOnSuccessListener { successCallback() }
                .addOnFailureListener { failureCallback() }
        }
    }

    // Upload files on storage
    suspend fun uploadImage(file: Uri, callback: (Uri) -> Unit, failureCallback: () -> Unit) {
        withContext(Dispatchers.IO) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val ref = storageRef.child(fileName)
            val uploadTask = ref.putFile(file)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) callback(task.result)
            }.addOnFailureListener { failureCallback() }
        }
    }

    suspend fun deleteImage(imageUrl: String) {
        withContext(Dispatchers.IO) {
            val photoRef: StorageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            photoRef.delete()
        }
    }
}