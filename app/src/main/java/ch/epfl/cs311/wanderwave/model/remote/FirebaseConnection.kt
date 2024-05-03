package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await

abstract class FirebaseConnection<T, U> {

  abstract val collectionName: String

  abstract val getItemId: (T) -> String

  open val db =
      FirebaseFirestore.getInstance().apply {
        firestoreSettings =
            FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(memoryCacheSettings {}) // Memory cache settings
                .setLocalCacheSettings(
                    persistentCacheSettings {
                      FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
                    } // Persistence cache settings (default)
                    )
                .build()
      }

  // If you want to use data exclusively from the local cache, you can use the following code:
  // db.disableNetwork().addOnCompleteListener {
  //   // Do offline things
  //   // ...
  // }

  abstract fun documentToItem(document: DocumentSnapshot): T?

  abstract fun itemToMap(item: T): Map<String, Any>

  open fun addItem(item: T) {
    val itemMap = itemToMap(item)

    db.collection(collectionName)
        .add(itemMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }
  }

  suspend fun addItemAndGetId(item: T): String? {
    val itemMap = itemToMap(item)
    var documentId: String? = null

    try {
      val documentReference = db.collection(collectionName).add(itemMap).await()

      Log.d("Firestore", "DocumentSnapshot successfully added!")
      documentId = documentReference.id
    } catch (e: Exception) {
      Log.e("Firestore", "Error adding document: ", e)
    }

    return documentId
  }

  open fun addItemWithId(item: T) {
    val itemId = getItemId(item)
    val itemMap = itemToMap(item)

    db.collection(collectionName)
        .document(itemId)
        .set(itemMap)
        .addOnFailureListener { e -> Log.e("Firestore", "Error adding document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully added!") }
  }

  open fun updateItem(item: T) {
    val itemId = getItemId(item)
    val itemMap = itemToMap(item)

    db.collection(collectionName)
        .document(itemId)
        .set(itemMap) // Use set to update the document
        .addOnFailureListener { e -> Log.e("Firestore", "Error updating document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully updated!") }
  }

  open fun deleteItem(item: T) {
    val itemId = getItemId(item)
    deleteItem(itemId)
  }

  open fun deleteItem(itemId: String) {
    db.collection(collectionName)
        .document(itemId)
        .delete()
        .addOnFailureListener { e -> Log.e("Firestore", "Error deleting document: ", e) }
        .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully deleted!") }
  }


  open fun getItem(itemId: String): Flow<T> = getItem(itemId) { _: DocumentSnapshot, _: MutableStateFlow<T?> -> }

  open fun getItem(
      itemId: String,
      onSuccess: (DocumentSnapshot, MutableStateFlow<T?>) -> Unit =
          { _: DocumentSnapshot, _: MutableStateFlow<T?> ->
          }
  ): Flow<T> {
    val dataFlow = MutableStateFlow<T?>(null)
    db.collection(collectionName)
        .document(itemId)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.data != null) {
            documentToItem(document)?.let {
              dataFlow.value = it
              onSuccess(document, dataFlow)
            }
          }
        }
        .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }

    return dataFlow.mapNotNull { it }
  }
}
