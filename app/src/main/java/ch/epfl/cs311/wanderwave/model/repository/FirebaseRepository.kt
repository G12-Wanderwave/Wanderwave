package ch.epfl.cs311.wanderwave.model.repository

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface FirebaseRepository<T> {
  val collectionName: String
  val getItemId: (T) -> String

  fun documentToItem(documentSnapshot: DocumentSnapshot): T?

  fun itemToMap(item: T): Map<String, Any>

  fun addItem(item: T)

  fun addItemWithId(item: T)

  fun updateItem(item: T)

  fun deleteItem(item: T)

  fun deleteItem(itemId: String)

  fun getItem(item: T, onSuccess: (DocumentSnapshot, MutableStateFlow<T?>) -> Unit): Flow<T>

  fun getItem(itemId: String, onSuccess: (DocumentSnapshot, MutableStateFlow<T?>) -> Unit): Flow<T>
}
