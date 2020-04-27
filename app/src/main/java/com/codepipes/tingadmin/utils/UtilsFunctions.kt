package com.codepipes.tingadmin.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Base64
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.caverock.androidsvg.SVG
import com.codepipes.tingadmin.models.*
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.round


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class UtilsFunctions(private val context: Context ) {

    public fun checkLocationPermissions(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) { true } else { requestLocationPermissions(); false }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FINE_LOCATION
        )
    }

    public fun base64ToBitmap(data: String) : Bitmap {
        val imageBytes = Base64.decode(data, Base64.DEFAULT)
        return  BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    public fun isConnectedToInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    public fun isConnected(): Boolean {
        return this.isConnectedToInternet()
    }

    private fun getDrawableFromUrl(url: URL): Drawable? {
        try {
            val input = url.openStream()
            return Drawable.createFromStream(input, "src")
        } catch (e: MalformedURLException) {
        } catch (e: IOException) { }
        return null
    }

    public fun vectorToBitmap(vectorDrawable: SVG): BitmapDescriptor {
        MapsInitializer.initialize(context)
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.renderToCanvas(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {

        private const val REQUEST_FINE_LOCATION = 2

        @SuppressLint("SimpleDateFormat")
        public fun timeAgo(date: String) : String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val time = sdf.parse(date).time
            val now = System.currentTimeMillis() - Date().timezoneOffset
            return DateUtils.getRelativeTimeSpanString(time , now, DateUtils.MINUTE_IN_MILLIS).toString()
        }

        @SuppressLint("SimpleDateFormat")
        public fun formatDate(date: String) : String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val time = sdf.parse(date)
            return SimpleDateFormat("MMMM dd, yyyy").format(time)
        }

        public fun getToken(length: Int): String{
            val chars: String = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            var result: String = ""
            for (i in 0..length) result += chars[floor(Math.random() * chars.length).toInt()]
            return result
        }

        public fun compressFile(file: File): File? {
            try {
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true
                o.inSampleSize = 6

                var inputStream = FileInputStream(file)

                BitmapFactory.decodeStream(inputStream, null, o)
                inputStream.close()

                val REQUIRED_SIZE = 75

                var scale = 1
                while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                    scale *= 2
                }

                val o2 = BitmapFactory.Options()
                o2.inSampleSize = scale
                inputStream = FileInputStream(file)

                val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
                inputStream.close()

                file.createNewFile()
                val outputStream = FileOutputStream(file)

                selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                return file

            } catch (e: Exception) { return null }
        }
    }
}