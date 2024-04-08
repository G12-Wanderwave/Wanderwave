package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull

interface FirebaseConnectionInt<T, U> {

  val collectionName: String

  val getItemId: (T) -> String

  private val db: FirebaseFirestore
    get() = FirebaseFirestore.getInstance()

  fun documentToItem(document: DocumentSnapshot): T

  fun itemToHash(item: T): HashMap<String, Any>

  fun addItem(item: T) {
    val itemMap = itemToHash(item)

    db.collection(collectionName)
        .add(itemMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }
  }

  fun updateItem(item: T) {
    val itemId = getItemId(item)
    val itemMap = itemToHash(item)

    db.collection(collectionName)
        .document(itemId)
        .set(itemMap) // Use set to update the document
        .addOnFailureListener { e -> Log.e("Firestore", "Error updating document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully updated!") }
  }

  fun deleteItem(item: T) {
    val itemId = getItemId(item)

    db.collection(collectionName).document(itemId).delete().addOnFailureListener { e ->
      Log.e("Firestore", "Error deleting document: ", e)
    }
  }

  fun getItem(item: T): Flow<T> = getItem(getItemId(item))

  fun getItem(itemId: String): Flow<T> {
    val dataFlow = MutableStateFlow<T?>(null)
    db.collection(collectionName)
        .document(itemId)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.data != null) {
            val item = documentToItem(document)
            dataFlow.value = item
          }
        }
        .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }

    return dataFlow.mapNotNull { it }
  }
}
