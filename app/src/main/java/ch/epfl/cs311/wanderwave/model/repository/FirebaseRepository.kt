package ch.epfl.cs311.wanderwave.model.repository

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

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

  fun getItem(itemId: String): Flow<Result<T>>
}
