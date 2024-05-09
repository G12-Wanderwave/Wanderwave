package ch.epfl.cs311.wanderwave.model.remote

import com.google.firebase.firestore.DocumentReference

// Represents an object that can be stored in Firebase Firestore
abstract class FirebaseTemplateObject() {
  // Convert the object to a map for Firebase Firestore storage

  abstract val collectionName: String

  abstract fun toMap(): Map<String, Any?>
}

// Represents a string node in Firebase
class FirebaseTemplateNode(val str: String, override val collectionName: String) :
    FirebaseTemplateObject() {

  override fun toMap(): Map<String, Any?> {
    return hashMapOf("node" to str)
  }
}

// Represents a firebase document, the base document
class FirebaseTemplateDocument<T>(
    val children: List<FirebaseTemplateObject>,
    override val collectionName: String
) : FirebaseTemplateObject() {

  override fun toMap(): Map<String, Any?> {
    return hashMapOf()
  }
}

// Represents a list in Firebase
class FirebaseTemplateList(val child: FirebaseTemplateObject, override val collectionName: String) :
    FirebaseTemplateObject() {

  override fun toMap(): Map<String, Any?> {
    return hashMapOf()
  }
}

// Represents a map in Firebase
class FirebaseTemplateMap(
    val map: Map<String, FirebaseTemplateObject>,
    override val collectionName: String
) : FirebaseTemplateObject() {

  override fun toMap(): Map<String, Any?> {
    return hashMapOf(
        "map" to (map as Map<*, *>).mapValues { (it.value as FirebaseTemplateObject).toMap() })
  }
}

// Represents a reference to another document in Firebase
class FirebaseTemplateReference(val ref: DocumentReference, override val collectionName: String) :
    FirebaseTemplateObject() {

  override fun toMap(): Map<String, Any?> {
    return hashMapOf("ref" to ref)
  }
}
