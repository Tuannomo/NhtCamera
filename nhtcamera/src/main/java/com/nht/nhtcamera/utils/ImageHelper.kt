package com.nht.nhtcamera.utils

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ExifInterface.ORIENTATION_NORMAL
import android.media.ExifInterface.ORIENTATION_ROTATE_180
import android.media.ExifInterface.ORIENTATION_ROTATE_270
import android.media.ExifInterface.ORIENTATION_ROTATE_90
import android.media.ExifInterface.TAG_ORIENTATION
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.camera.core.ImageProxy
import com.nht.nhtcamera.CameraApplication
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import kotlin.math.floor

class ImageHelper {

    companion object {
        private const val TAG = "ImageHelper"

        fun getAvailableInternalMemorySize(): Long {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long = stat.blockSizeLong
            val availableBlocks: Long = stat.availableBlocksLong
            return availableBlocks * blockSize
        }

        fun rotateImageIfRequired(img: Bitmap?, orientation: Int): Bitmap? {
            return when (orientation) {
                90 -> img?.let { getRotatedBitmap(it, 90) }
                180 -> img?.let { getRotatedBitmap(it, 180) }
                270 -> img?.let { getRotatedBitmap(it, 270) }
                else -> img
            }
        }

        fun getImageFileFromPath(uri: Uri, displayName: String): String {
            val destinationFilename =
                File(CameraApplication.cameraApplication?.filesDir?.path + File.separatorChar + displayName)
            try {
                CameraApplication.cameraApplication?.contentResolver?.openInputStream(uri).use { ins ->
                    ins?.let {
                        createFileFromStream(
                            it,
                            destinationFilename
                        )
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return destinationFilename.absolutePath
        }

        fun getImagePathFromURI(contentURI: Uri): String? {
            val filePath: String?
            val projection = arrayOf(
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME
            )

            val cursor =
                CameraApplication.cameraApplication?.contentResolver?.query(
                    contentURI,
                    projection,
                    null,
                    null,
                    null
                )
            if (cursor == null) {
                filePath = contentURI.path
            } else {
                cursor.moveToFirst()
                val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val idName: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                filePath = cursor.getString(idx)
                val name = cursor.getString(idName)

                getImageFileFromPath(contentURI, name)
                cursor.close()
            }
            return filePath
        }

//        fun getBitmapWithRotation(image: GalleryModel): Bitmap {
//            val rotateDegree = rotateGalleryImageIfRequired(image.imagePath)
//
//            return if (image.imageEditedTemporary != null) {
//                image.imageEditedTemporary!!
//            } else {
//                if (rotateDegree > 0.0f) {
//                    getRotatedBitmap(
//                        imgBitmap = convertImgPathToBitmap(
//                            getImageFileFromPath(
//                                image.imagePath,
//                                image.displayName
//                            )
//                        ),
//                        degree = rotateDegree.toInt()
//                    )
//                } else {
//                    convertImgPathToBitmap(
//                        getImageFileFromPath(
//                            image.imagePath,
//                            image.displayName
//                        )
//                    )
//                }
//            }
//        }

        fun convertImageProxyToBitmap(image: ImageProxy): Bitmap {
            val buffer = image.planes[0].buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            return getRotatedBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size), 90)
        }

        @Throws(IOException::class)
        private fun rotateGalleryImageIfRequired(selectedImage: Uri): Float {
            val input = CameraApplication.cameraApplication?.contentResolver?.openInputStream(selectedImage)
            val exifInterface: ExifInterface =
                if (Build.VERSION.SDK_INT > 23) ExifInterface(input!!) else ExifInterface(
                    selectedImage.path!!
                )
            return when (exifInterface.getAttributeInt(
                TAG_ORIENTATION,
                ORIENTATION_NORMAL
            )) {
                ORIENTATION_ROTATE_90 -> 90f
                ORIENTATION_ROTATE_180 -> 180f
                ORIENTATION_ROTATE_270 -> 270f
                else -> 0.0f
            }
        }

        private fun createFileFromStream(ins: InputStream, destination: File) {
            try {
                FileOutputStream(destination).use { os ->
                    val buffer = ByteArray(4096)
                    var length: Int
                    while (ins.read(buffer).also { length = it } > 0) {
                        os.write(buffer, 0, length)
                    }
                    os.flush()
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }

        private fun getRotatedBitmap(imgBitmap: Bitmap, degree: Int): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedImg = Bitmap.createBitmap(
                imgBitmap,
                0,
                0,
                imgBitmap.width,
                imgBitmap.height,
                matrix,
                true
            )
            imgBitmap.recycle()
            return rotatedImg
        }

        private fun convertImgPathToBitmap(imagePath: String?): Bitmap {
            return BitmapFactory.decodeFile(imagePath)
        }


        private fun getImgStoredUri(
            name: String?,
            isSavedToGallery: Boolean,
            imgBitMap: Bitmap
        ): Uri? {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    if (isSavedToGallery) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    } else {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Field")
                    }
                }
            }
            val uri: Uri? =
                CameraApplication.cameraApplication?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

            uri?.let {
                CameraApplication.cameraApplication?.contentResolver?.openOutputStream(it).use { output ->
                    output?.let { it1 -> imgBitMap.compress(Bitmap.CompressFormat.JPEG, 100, it1) }
                }
            }
            return uri
        }


        private fun getGpsLocation(geoDegree: Double): String {
            val degree = floor(geoDegree).toInt()
            val minutes = floor((geoDegree - degree) * 60).toInt()
            val seconds: String =
                DecimalFormat("###.####").format((geoDegree - (degree.toDouble() + minutes.toDouble() / 60)) * 3600)

            return "$degree/1,$minutes/1,$seconds/1000"
        }
    }
}