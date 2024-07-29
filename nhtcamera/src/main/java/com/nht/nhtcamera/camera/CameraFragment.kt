package com.nht.nhtcamera.camera

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nht.nhtcamera.utils.ImageHelper
import com.nht.nhtcamera.utils.LuminosityAnalyzer
import com.nht.nhtcamera.R
import com.nht.nhtcamera.adapter.RcvImageCameraItemViewHolder
import com.nht.nhtcamera.adapter.RcvImageCameraPreviewAdapter
import com.nht.nhtcamera.databinding.FragmentCameraBinding
import com.nht.nhtcamera.model.ImageRotation
import com.nht.nhtcamera.utils.AlertDialogFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var mAdapter: RcvImageCameraPreviewAdapter
    private lateinit var viewModel: CameraViewModel
    private lateinit var dialogInform: AlertDialog.Builder
    private lateinit var dialogQuit: AlertDialog.Builder
    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null
    private lateinit var camera: Camera
    private var isEnoughDeviceMemorize: Boolean = false
    private var isRequireLocation: Boolean = false

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private val alertDialogFactory = AlertDialogFactory()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("TUANNH","CameraFragment onCreateView")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TUANNH","CameraFragment onViewCreated")
        viewModel = ViewModelProvider(this)[CameraViewModel::class.java]

        initControl()
        initEventHandle()
        initViewModel()
    }

    override fun onResume() {
        super.onResume()
        isEnoughDeviceMemorize = checkEnoughDeviceMemorize()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun initViewModel() {
        viewModel.imageDegreeRotateLiveData.observe(viewLifecycleOwner) { degreeRotation ->
            handleBtnAnimation(degreeRotation)
        }
        viewModel.imageCapturedLiveData.observe(viewLifecycleOwner) { listImageCaptured ->
            if (listImageCaptured.isEmpty()) {
                binding.btnSend.visibility = GONE
                binding.tvEmptyImageCaptured.visibility = VISIBLE
                binding.rcvImage.visibility = GONE
                binding.imgCountContainer.visibility = GONE
            } else {
                binding.btnSend.visibility = VISIBLE
                binding.tvEmptyImageCaptured.visibility = GONE
                binding.rcvImage.visibility = VISIBLE
                binding.imgCountContainer.visibility = VISIBLE
            }
            mAdapter.setViewModels(listImageCaptured)
            if (listImageCaptured.isNotEmpty()) {
                binding.rcvImage.scrollToPosition(listImageCaptured.size - 1)
            }
        }
        viewModel.imageCountLiveData.observe(viewLifecycleOwner) { imgCount ->
            binding.imgCountContainer.visibility = VISIBLE
            binding.tvImageNumber.text = imgCount.toString()
        }
        viewModel.flashStatusLiveData.observe(viewLifecycleOwner) { isFlashEnable ->
            if (isFlashEnable) {
                binding.btnFlash.setImageResource(R.drawable.ic_flash)
                imageCapture?.flashMode = FLASH_MODE_ON
            } else {
                binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
                imageCapture?.flashMode = FLASH_MODE_OFF
            }
        }
        viewModel.messageLiveData.observe(viewLifecycleOwner) { messageId ->
            dialogInform.setMessage(requireContext().getString(messageId))
            dialogInform.show()
        }
        viewModel.finishScreenLiveData.observe(viewLifecycleOwner) {
            activity?.finish()
        }
        viewModel.showEditImageScreenLiveData.observe(viewLifecycleOwner) {
            (activity as CameraMainScreen).showFragment()
        }
        viewModel.quitInformLiveData.observe(viewLifecycleOwner) { showQuitDialog ->
            if (showQuitDialog) {
                dialogQuit.show()
            } else {
                activity?.finish()
            }
        }
        viewModel.isCapturedWithFlashLiveData.observe(viewLifecycleOwner) { isCapturedWithFlash ->
            if (isCapturedWithFlash) {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(requireContext()),
                    object : OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            captureAnimation()
                            viewModel.onImageCaptureWithFlash(
                                image = image,
                                isRequireLocation = isRequireLocation
                            )
                            image.close()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            viewModel.onCaptureError(exception.message)
                        }
                    })
            } else {
                captureAnimation()
                viewModel.onImageCapture(
                    bitmap = binding.viewFinder.bitmap,
                    isRequireLocation = isRequireLocation
                )
            }
        }
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
        viewModel.showDialogPermissionLiveData.observe(viewLifecycleOwner) { isShowDialog ->
            if (isShowDialog) {
                showPermissionInformDialog()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventHandle() {
        binding.btnCapture.setOnClickListener {
            if (isEnoughDeviceMemorize) {
                dialogInform.setMessage(getString(R.string.camera_not_enough_storage_inform))
                dialogInform.show()
                return@setOnClickListener
            }
            viewModel.onBtnCaptureClicked()
        }
        binding.btnFlash.setOnClickListener {
            viewModel.onBtnFlashClick()
        }
        binding.btnSend.setOnClickListener {
            viewModel.onBtnSendClick()
        }
        binding.btnCancelCamera.setOnClickListener {
            viewModel.onCameraCancelClick()
        }

        binding.viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun checkEnoughDeviceMemorize(): Boolean {
        val memorySize = ImageHelper.getAvailableInternalMemorySize()
        return memorySize <= LIMIT_IMAGE_SIZE
    }

    private fun initControl() {
        arguments?.let { bundle ->
            isRequireLocation = bundle.getBoolean(REQUIRE_LOCATION)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.viewFinder.post {
            startCamera()
        }

        binding.cameraPreviewContainer.setContentPadding(0, 0, 0, 0)

        mAdapter = RcvImageCameraPreviewAdapter(requireContext())
        mAdapter.setItemImageClickCallBack(onItemImageClickListener)

        binding.rcvImage.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rcvImage.adapter = mAdapter

        setUpInformDialog()
        setUpQuitDialog()
    }
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        binding.viewFinder.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleGestureDetector = ScaleGestureDetector(requireContext(), cameraZoomListener)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().apply {
                setTargetRotation(binding.viewFinder.display.rotation)
            }.build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = Builder()
                .setTargetResolution(getTargetResolution())
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setFlashType(FLASH_TYPE_ONE_SHOT_FLASH)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setImageQueueDepth(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer {})
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getTargetResolution(): Size {
        return when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> Size(768, 1024)
            Configuration.ORIENTATION_LANDSCAPE -> Size(1024, 768)
            else -> Size(1024, 768)
        }
    }

    private fun handleBtnAnimation(imageRotation: ImageRotation) {
        val rotateCapture = createRotateAnimation(imageRotation)
        val rotateFlash = createRotateAnimation(imageRotation)
        val imgCountArrow = createRotateAnimation(imageRotation)
        val countNumber = createRotateAnimation(imageRotation)

        binding.btnCapture.startAnimation(rotateCapture)
        binding.btnFlash.startAnimation(rotateFlash)
        binding.imgSend.startAnimation(imgCountArrow)
        binding.tvImageNumber.startAnimation(countNumber)
    }

    private fun createRotateAnimation(imageRotation: ImageRotation): RotateAnimation {
        val rotationAnimation = RotateAnimation(
            imageRotation.currentDegree,
            imageRotation.targetDegree,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotationAnimation.duration = 200
        rotationAnimation.interpolator = LinearInterpolator()
        rotationAnimation.fillAfter = true

        return rotationAnimation
    }

    private fun captureAnimation() {
        val animation: Animation = AlphaAnimation(0.6f, 0.1f)
        animation.duration = 200
        animation.interpolator = LinearInterpolator()
        animation.repeatMode = Animation.REVERSE

        binding.viewFinder.startAnimation(animation)
    }

    private fun setUpInformDialog() {
        dialogInform = AlertDialog.Builder(requireContext())
        dialogInform.setPositiveButton(getString(R.string.camera_txt_ok)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        dialogInform.create()
    }

    private fun setUpQuitDialog() {
        dialogQuit = AlertDialog.Builder(requireContext())
        dialogQuit.setCancelable(false)
        dialogQuit.setMessage(getString(R.string.camera_inform_all_photos_will_be_discarded))
        dialogQuit.setPositiveButton(getString(R.string.camera_txt_ok)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            activity?.finish()
        }
        dialogQuit.setNegativeButton(getString(R.string.txt_cancel)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            viewModel.onItemImageCameraClick(position = 0)
        }
        dialogQuit.create()
    }

    private val onItemImageClickListener = object :
        RcvImageCameraItemViewHolder.OnItemImageClickListener {
        override fun onItemImageClick(position: Int) {
            viewModel.onItemImageCameraClick(position)
        }
    }

    private val cameraZoomListener = object : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 0F
            val delta = detector.scaleFactor

            camera.cameraControl.setZoomRatio(currentZoomRatio * delta)

            return true
        }
    }

    private fun showPermissionInformDialog() {
        if (isLocationEnabled(requireContext())){
            showPermissionDialog()
        }else {
            showRequestLocationEnableSetting()
        }
    }

    private fun showRequestLocationEnableSetting() {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            R.string.txt_camera_location_dialog_above_pie
        }else {
            R.string.txt_camera_location_dialog_below_pie
        }
        alertDialogFactory.getAlertDialog(
            context = requireContext(),
            messageStringRes = message,
            positiveButtonStringRes = R.string.txt_open_settings,
            negativeButtonStringRes = R.string.dialog_cancel_text
        ) { _: DialogInterface?, _: Int ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }.show()
    }

    private fun showPermissionDialog() {
        alertDialogFactory.getAlertDialog(
            context = requireContext(),
            messageStringRes = R.string.txt_camera_permission_message_dialog_inform,
            positiveButtonStringRes = R.string.txt_open_settings,
            negativeButtonStringRes = R.string.dialog_cancel_text
        ) { _: DialogInterface?, _: Int ->
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null)
                )
            )
        }.show()
    }

    @Suppress("deprecation")
    private fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val LIMIT_IMAGE_SIZE = 10240
        private const val REQUIRE_LOCATION = "REQUIRE_LOCATION"

        fun newInstance(isRequireLocation: Boolean): CameraFragment {
            val fragment = CameraFragment()
            val bundle = Bundle()
            bundle.putBoolean(REQUIRE_LOCATION, isRequireLocation)
            fragment.arguments = bundle
            return fragment
        }
    }
}