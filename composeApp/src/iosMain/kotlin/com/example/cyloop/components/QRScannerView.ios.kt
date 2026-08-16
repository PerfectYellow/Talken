package com.example.cyloop.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import platform.AVFoundation.*
import platform.Foundation.*
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*
import platform.CoreGraphics.*
import platform.darwin.*
import platform.CoreFoundation.kCFBooleanTrue
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QRScannerView(
    modifier: Modifier,
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
    if (device == null) {
        onDismiss()
        return
    }

    val captureSession = remember { AVCaptureSession() }
    
    val cameraView = remember {
        UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)).apply {
            val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
            previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            layer.addSublayer(previewLayer)
        }
    }

    LaunchedEffect(Unit) {
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        if (input != null && captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        }

        val output = AVCaptureMetadataOutput()
        if (captureSession.canAddOutput(output)) {
            captureSession.addOutput(output)
            output.setMetadataObjectsDelegate(object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
                override fun captureOutput(
                    output: AVCaptureOutput,
                    didOutputMetadataObjects: List<*>,
                    fromConnection: AVCaptureConnection
                ) {
                    val metadataObject = didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject
                    if (metadataObject?.type == AVMetadataObjectTypeQRCode) {
                        metadataObject!!.stringValue?.let { code ->
                            onCodeScanned(code)
                            captureSession.stopRunning()
                        }
                    }
                }
            }, dispatch_get_main_queue())
            output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
        }

        captureSession.startRunning()
    }

    DisposableEffect(Unit) {
        onDispose {
            captureSession.stopRunning()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        UIKitView(
            factory = { cameraView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                CATransaction.begin()
                CATransaction.setValue(kCFBooleanTrue, kCATransactionDisableActions)
                view.layer.sublayers?.firstOrNull()?.let { layer ->
                    (layer as AVCaptureVideoPreviewLayer).setFrame(view.bounds)
                }
                CATransaction.commit()
            }
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}
