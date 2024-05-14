package ch.epfl.cs311.wanderwave.model.remote

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await

abstract class FirebaseConnection<T, U>(open val db: FirebaseFirestore) {

  abstract val collectionName: String

  abstract val getItemId: (T) -> String

  abstract fun documentToItem(document: DocumentSnapshot): T?

  abstract fun itemToMap(item: T): Map<String, Any>

  // Have success and failure log messages as companion object constants
  companion object {
    const val ADD_SUCCESS_LOG_MESSAGE = "DocumentSnapshot successfully added !!"
    const val ADD_FAILURE_LOG_MESSAGE = "Error adding document: "
  }

  open fun addItem(item: T) {
    val itemMap = itemToMap(item)
    Log.d("Conneciton", "beacon map : ${itemMap}")
    db.collection(collectionName)
        .add(itemMap)
        .addOnFailureListener { e -> Log.e("Firestore", ADD_FAILURE_LOG_MESSAGE, e) }
        .addOnSuccessListener { Log.d("Firestore", ADD_SUCCESS_LOG_MESSAGE) }
  }

  suspend fun addItemAndGetId(item: T): String? {
    val itemMap = itemToMap(item)
    var documentId: String? = null

    try {
      val documentReference = db.collection(collectionName).add(itemMap).await()

      Log.d("Firestore", ADD_SUCCESS_LOG_MESSAGE)
      documentId = documentReference.id
    } catch (e: Exception) {
      Log.e("Firestore", ADD_FAILURE_LOG_MESSAGE, e)
    }

    return documentId
  }

  open fun addItemWithId(item: T) {
    // implementation is the same but we want to leave it for clarity
    updateItem(item)
  }

  open fun updateItem(item: T) {
    val itemId = getItemId(item)
    val itemMap = itemToMap(item)

    db.collection(collectionName)
        .document(itemId)
        .set(itemMap) // Use set to update the document
        .addOnFailureListener { e -> Log.e("Firestore", ADD_FAILURE_LOG_MESSAGE, e) }
        .addOnSuccessListener { Log.d("Firestore", ADD_SUCCESS_LOG_MESSAGE) }
  }

  open fun deleteItem(item: T) {
    val itemId = getItemId(item)
    deleteItem(itemId)
  }

  open fun deleteItem(itemId: String) {
    db.collection(collectionName)
        .document(itemId)
        .delete()
        .addOnFailureListener { e -> Log.e("Firestore", ADD_FAILURE_LOG_MESSAGE, e) }
        .addOnSuccessListener { Log.d("Firestore", ADD_SUCCESS_LOG_MESSAGE) }
  }

  open fun getItem(itemId: String): Flow<T> {
    val dataFlow = MutableStateFlow<T?>(null)
    db.collection(collectionName)
        .document(itemId)
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.data != null) {
            documentToItem(document)?.let {
              dataFlow.value = it
              documentTransform(document, dataFlow)
            }
          }
        }
        .addOnFailureListener { e -> Log.e("Firestore", "Error getting document: ", e) }

    return dataFlow.mapNotNull { it }
  }

  /**
   * Transforms the document snapshot or performs additional operations on the stateFlow. This
   * function is intended to be overridden in subclasses if specific behavior is needed. If not
   * overridden, it defaults to a no-op (no operation).
   */
  open internal fun documentTransform(
      documentSnapshot: DocumentSnapshot,
      stateFlow: MutableStateFlow<T?>
  ) {}
}
