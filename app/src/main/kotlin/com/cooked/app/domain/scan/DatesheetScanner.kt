package com.cooked.app.domain.scan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class DatesheetScanner @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanImageUri(
        context: Context,
        imageUri: Uri,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<ScannedDeadlineCandidate> {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        val extractedText = processImage(inputImage)
        return DatesheetParser.parse(extractedText, now, zone)
    }

    private suspend fun processImage(image: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
}
