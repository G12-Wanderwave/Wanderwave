package ch.epfl.cs311.wanderwave.ui.components.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ch.epfl.cs311.wanderwave.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

private fun bitmapFromResource(resourceId: Int, context: Context): Bitmap {
  val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
  return bitmap
}

private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
  return Bitmap.createScaledBitmap(bitmap, width, height, false)
}

fun getIcon(context: Context): BitmapDescriptor {
  return BitmapDescriptorFactory.fromBitmap(
      resizeBitmap(bitmapFromResource(R.drawable.beaconlogo, context = context), 100, 100))
}
