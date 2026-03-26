package com.example.covertpdfapplication.convert.converttopdf.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.aspose.slides.Presentation
import com.example.covertpdfapplication.convert.converttopdf.interface1.IConverterToPdf
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.AreaBreakType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ConvertPresentationService(
    private val context: Context
) : IConverterToPdf {

    companion object {
        private const val TAG = "CONVERT_PDF"
    }

    override suspend fun convertToPdf(context: Context, uris: List<Uri>): File? {
        return withContext(Dispatchers.IO) {

            if (uris.isEmpty()) return@withContext null

            val outputFile = File(
                context.cacheDir,
                "presentation_${System.currentTimeMillis()}.pdf"
            )

            try {
                val writer = PdfWriter(outputFile)
                val pdfDoc = PdfDocument(writer)
                val document = Document(pdfDoc)
                document.setMargins(0f, 0f, 0f, 0f)

                uris.forEach { uri ->
                    val file = copyUriToFile(context, uri)

                    try {
                        convertWithAsposeSafe(file, document)
                    } catch (e: Exception) {
                        Log.e(TAG, "Convert lỗi: ${e.message}", e)
                    }

                    file.delete()
                }

                document.close()

                return@withContext if (outputFile.exists() && outputFile.length() > 0) {
                    outputFile
                } else null

            } catch (e: Exception) {
                Log.e(TAG, "Crash tổng: ${e.message}", e)
                null
            }
        }
    }

    private fun convertWithAsposeSafe(file: File, document: Document) {

        val totalSlides = try {
            val pres = Presentation(file.absolutePath)
            val count = pres.slides.size()
            pres.dispose()
            count
        } catch (e: Exception) {
            Log.e(TAG, "Không đọc được file", e)
            return
        }

        for (i in 0 until totalSlides) {

            var presentation: Presentation? = null

            try {
                presentation = Presentation(file.absolutePath)

                val slide = presentation.slides.get_Item(i)

                val bitmap: Bitmap = slide.getThumbnail(0.5f, 0.5f)

                addBitmapToPdf(
                    bitmap = bitmap,
                    document = document,
                    isLastSlide = i == totalSlides - 1
                )

            } catch (oom: OutOfMemoryError) {
                Log.e(TAG, "OOM tại slide $i", oom)

                // 🔥 fallback scale thấp hơn nữa
                try {
                    presentation = Presentation(file.absolutePath)
                    val slide = presentation.slides.get_Item(i)

                    val bitmap: Bitmap = slide.getThumbnail(0.3f, 0.3f)

                    addBitmapToPdf(
                        bitmap = bitmap,
                        document = document,
                        isLastSlide = i == totalSlides - 1
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "Fallback cũng fail slide $i", e)
                    break
                }

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi slide $i: ${e.message}", e)
            } finally {
                presentation?.dispose()

                // 🔥 ÉP dọn RAM ngay
                System.gc()
                Thread.sleep(100)
            }
        }
    }

    // ================= PDF =================
    private fun addBitmapToPdf(
        bitmap: Bitmap,
        document: Document,
        isLastSlide: Boolean
    ) {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            val image = Image(
                ImageDataFactory.create(stream.toByteArray())
            )

            image.setAutoScale(true)
            document.add(image)

            if (!isLastSlide) {
                document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Lỗi add PDF", e)
        } finally {
            bitmap.recycle()
        }
    }

    // ================= FILE =================
    private fun copyUriToFile(context: Context, uri: Uri): File {

        val extension = getExtension(context, uri)

        val file = File(
            context.cacheDir,
            "temp_${System.currentTimeMillis()}.$extension"
        )

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun getExtension(context: Context, uri: Uri): String {
        val type = context.contentResolver.getType(uri) ?: return "pptx"

        return when {
            type.contains("presentationml") -> "pptx"
            type.contains("powerpoint") -> "ppt"
            type.contains("opendocument") -> "odp"
            else -> "pptx"
        }
    }
}