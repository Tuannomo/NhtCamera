package com.nht.nhtcamera.camera

import android.graphics.Bitmap
import android.view.Surface.ROTATION_0
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nht.nhtcamera.utils.ImageHelper
import com.nht.nhtcamera.model.ImageSaved
import com.nht.nhtcamera.model.ImageSavedDelete
import com.nht.nhtcamera.utils.SchedulerConfiguration
import com.nht.nhtcamera.utils.SchedulerConfigurationImpl
import com.nht.nhtcamera.R
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_180
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_270
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_90
import com.nht.nhtcamera.location.LocationProvider
import com.nht.nhtcamera.model.ImageRotation
import com.nht.nhtcamera.utils.EventLiveData
import io.reactivex.disposables.CompositeDisposable

class CameraViewModel : ViewModel() {

    private var _imageCapturedLiveData = MutableLiveData<List<ImageSaved>>()
    val imageCapturedLiveData: LiveData<List<ImageSaved>> = _imageCapturedLiveData

    private var _imagePreviewEditLiveData = MutableLiveData<List<ImageSaved>>()
    val imagePreviewEditLiveData: LiveData<List<ImageSaved>> = _imagePreviewEditLiveData

    private var _imageEditLiveData = MutableLiveData<List<ImageSaved>>()
    val imageEditLiveData: LiveData<List<ImageSaved>> = _imageEditLiveData

    private var _imageDegreeRotateLiveData = MutableLiveData<ImageRotation>()
    val imageDegreeRotateLiveData: LiveData<ImageRotation> = _imageDegreeRotateLiveData

    private var _imageDetailsLiveData = MutableLiveData<Bitmap>()
    val imageDetailsLiveData: LiveData<Bitmap> = _imageDetailsLiveData

    private var _imageCountLiveData = MutableLiveData<Int>()
    val imageCountLiveData: LiveData<Int> = _imageCountLiveData

    private val _flashStatusLiveData = MutableLiveData<Boolean>()
    val flashStatusLiveData: LiveData<Boolean> = _flashStatusLiveData

    private val _messageLiveData = MutableLiveData<Int>()
    val messageLiveData: LiveData<Int> = _messageLiveData

    private val _finishScreenLiveData = MutableLiveData<Boolean>()
    val finishScreenLiveData: LiveData<Boolean> = _finishScreenLiveData

    private val _showEditImageScreenLiveData = MutableLiveData<Boolean>()
    val showEditImageScreenLiveData: LiveData<Boolean> = _showEditImageScreenLiveData

    private var _imageDeleteLiveData = MutableLiveData<EventLiveData<ImageSavedDelete>>()
    val imageDeleteLiveData: LiveData<EventLiveData<ImageSavedDelete>> = _imageDeleteLiveData

    private var _quitInformLiveData = MutableLiveData<Boolean>()
    val quitInformLiveData: LiveData<Boolean> = _quitInformLiveData

    private var _onBackPressLiveData = MutableLiveData<Boolean>()
    val onBackPressLiveData: LiveData<Boolean> = _onBackPressLiveData

    private var _isCapturedWithFlashLiveData = MutableLiveData<Boolean>()
    val isCapturedWithFlashLiveData: LiveData<Boolean> = _isCapturedWithFlashLiveData

    private var _errorMessageLiveData = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> = _errorMessageLiveData

    private var _showDialogPermissionLiveData = MutableLiveData<Boolean>()
    val showDialogPermissionLiveData: LiveData<Boolean> = _showDialogPermissionLiveData

    var images = mutableListOf<ImageSaved>()

    var currentDegree: Float = 0.0f
    var mImageRotateDegree: Int = 0
    var isFlashEnable: Boolean = false
    var currentImgPosition: Int = 0
    var isCapturing: Boolean = false

    var locationProvider = LocationProvider()
    var schedulerConfiguration: SchedulerConfiguration = SchedulerConfigurationImpl()
    private var composite = CompositeDisposable()

    fun onImageCapture(bitmap: Bitmap?, isRequireLocation: Boolean) {
        handleImageCaptured(bitmap, isRequireLocation)
    }

    fun onImageCaptureWithFlash(image: ImageProxy, isRequireLocation: Boolean) {
        val bitmap = ImageHelper.convertImageProxyToBitmap(image)
        handleImageCaptured(bitmap, isRequireLocation)
    }

    fun onOrientationChange(rotation: Int) {
        when (rotation) {
            ROTATION_0 -> {
                val targetDegree = if (currentDegree > 0.0f && currentDegree < 180f) {
                    currentDegree - 90
                } else if (currentDegree >= 180) {
                    360f
                } else {
                    0.0f
                }
                mImageRotateDegree = 0
                _imageDegreeRotateLiveData.value = ImageRotation(currentDegree, targetDegree)
                currentDegree = 0.0f
            }

            ROTATION_90 -> {
                mImageRotateDegree = 270
                _imageDegreeRotateLiveData.value = ImageRotation(currentDegree, 90f)
                currentDegree = 90f
            }

            ROTATION_180 -> {
                mImageRotateDegree = 180
                _imageDegreeRotateLiveData.value = ImageRotation(currentDegree, 180f)
                currentDegree = 180f
            }

            ROTATION_270 -> {
                mImageRotateDegree = 90
                val targetDegree = if (currentDegree == 180f) {
                    270f
                } else {
                    -90f
                }
                _imageDegreeRotateLiveData.value = ImageRotation(currentDegree, targetDegree)
                currentDegree = 270f
            }
        }


        // Disabled temporarily due to crash, need a better solution for this animation
        /*
        _imagePreviewEditLiveData.value = images.map { imageSaved ->
            imageSaved.rotation = currentDegree
            imageSaved
        }*/
    }

    fun onItemImageEditClick(imageBitmap: Bitmap, position: Int) {
        for (index in images.indices) {
            images[index].isSelected = index == position
        }
        _imageDetailsLiveData.value = images[position].imgBitmap
        images[currentImgPosition].imgBitmap = imageBitmap

        _imagePreviewEditLiveData.value = images
        _imageCapturedLiveData.value = images

        currentImgPosition = position
    }

    fun onBackPressClick(imageBitmap: Bitmap) {
        if (images.isNotEmpty()) {
            images[currentImgPosition].imgBitmap = imageBitmap

            _imageCapturedLiveData.value = images
        }
        _onBackPressLiveData.value = true
    }

    fun onItemImageCameraClick(position: Int) {
        for (index in images.indices) {
            images[index].isSelected = index == position
        }
        _imageCapturedLiveData.value = images
        _imageDetailsLiveData.value = images[position].imgBitmap
        currentImgPosition = position

        _showEditImageScreenLiveData.value = true
    }

    fun onItemImageDeleteClick(position: Int) {
        images.removeAt(position)
        if (images.size <= 0) {
            _imageCapturedLiveData.value = images
            _imagePreviewEditLiveData.value = images
            return
        }

        val imageTotal = images.size - 1
        var isLatestPosition = false
        if (imageTotal >= position) {
            currentImgPosition = position
        } else {
            isLatestPosition = true
            currentImgPosition = imageTotal
        }

        images[currentImgPosition].isSelected = true
        _imageDetailsLiveData.value = images[currentImgPosition].imgBitmap

        _imageCapturedLiveData.value = images
        _imageCountLiveData.value = images.size

        if (isLatestPosition) {
            _imagePreviewEditLiveData.value = images
        } else {
            _imageDeleteLiveData.value =
                EventLiveData(
                    ImageSavedDelete(
                        position,
                        images
                    )
                )
        }
    }

    fun onBtnFlashClick() {
        isFlashEnable = !isFlashEnable
        _flashStatusLiveData.value = isFlashEnable
    }

    fun onBtnSendClick(bitmap: Bitmap? = null) {
        if (bitmap != null) {
            images[currentImgPosition].imgBitmap = bitmap
        }
        //TODO: send images to outside library
//        RxBus.publish(RxBus.SEND_IMAGES, images)
        _finishScreenLiveData.value = true
    }

    fun fetchData() {
        _imageEditLiveData.value = images
        _imageCountLiveData.value = images.size
    }

    fun onCameraCancelClick() {
        _quitInformLiveData.value = images.isNotEmpty()
    }

    fun onBtnCaptureClicked() {
        if (isCapturing) {
            return
        }
        isCapturing = true
        _isCapturedWithFlashLiveData.value = isFlashEnable
    }

    fun onCaptureError(message: String?) {
        isCapturing = false

        message?.let { _errorMessageLiveData.value = it }
    }

    private fun handleImageCaptured(bitmap: Bitmap?, isRequireLocation: Boolean) {
        isCapturing = false
        if (images.size >= 10) {
            _messageLiveData.value = R.string.camera_reach_limit_picture_inform
        } else {
            if (bitmap != null) {
                val imageBitmap = ImageHelper.rotateImageIfRequired(bitmap, mImageRotateDegree)

                if (imageBitmap != null) {
//                    if (isRequireLocation) {
//                        val subscribe = locationProvider.fetchDeviceLocation()
//                            .observeOn(schedulerConfiguration.mainScheduler())
//                            .subscribe({ result ->
//                                when (result) {
//                                    is GetLocationResult.Success -> {
//                                        val imgName = Helper.getImgCapturedNameByDate()
//                                        val dateCaptured = Helper.getImgCapturedDate()
//                                        val lng = result.location.longitude
//                                        val lat = result.location.latitude
//                                        images.add(
//                                            ImageSaved(
//                                                imgBitmap = imageBitmap,
//                                                imgPath = "",
//                                                isSelected = false,
//                                                rotation = 0.0f,
//                                                mLongitude = lng,
//                                                mLatitude = lat,
//                                                dateCaptured = dateCaptured,
//                                                imgNameByDate = imgName
//                                            )
//                                        )
//                                        _imageCapturedLiveData.value = images
//                                        _imageCountLiveData.value = images.size
//                                    }
//
//                                    is GetLocationResult.TimeOut -> {
//                                        _errorMessageLiveData.value = "Cannot get latest location."
//                                    }
//
//                                    else -> {
//                                        _showDialogPermissionLiveData.value = true
//                                    }
//                                }
//                            }, {})
//                        composite.add(subscribe)
//                    } else {
                        images.add(ImageSaved(imageBitmap))
                        _imageCapturedLiveData.value = images
                        _imageCountLiveData.value = images.size
//                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        composite.clear()
    }
}