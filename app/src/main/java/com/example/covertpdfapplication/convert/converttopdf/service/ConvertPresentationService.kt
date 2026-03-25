package com.example.covertpdfapplication.convert.converttopdf.service

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
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
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextShape
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import java.io.*
import java.util.zip.ZipInputStream

class ConvertPresentationService(
    private val context: Context
) : IConverterToPdf {

    override suspend fun convertToPdf(context: Context, uris: List<Uri>): File? {
        return withContext(Dispatchers.IO) {
            if (uris.isEmpty()) return@withContext null

            val outputFile = File(
                context.cacheDir,
                "presentation_${System.currentTimeMillis()}.pdf"
            )

            try {
                val pdfWriter = PdfWriter(outputFile)
                val pdfDoc = PdfDocument(pdfWriter)
                val document = Document(pdfDoc)

                uris.forEach { uri ->
                    // 1. Copy file kèm theo extension đúng để nhận diện (CỰC KỲ QUAN TRỌNG)
                    val file = copyUriToFile(context, uri)
                    val ext = file.extension.lowercase()

                    Log.d("DEBUG_CONVERT", "Đang xử lý file: ${file.name} | Định dạng: $ext")

                    when (ext) {
                        "pptx" -> convertPptx(file, document)
                        "ppt" -> convertPpt(file, document)
                        "odp" -> convertOdp(file, document)
                        else -> Log.e("DEBUG_CONVERT", "Không nhận diện được định dạng: $ext")
                    }

                    // Xóa file tạm sau khi convert xong 1 file
                    if (file.exists()) file.delete()
                }

                document.close()
                Log.d("DEBUG_CONVERT", "Convert thành công! Size: ${outputFile.length()} bytes")

                if (outputFile.exists() && outputFile.length() > 0) outputFile else null

            } catch (e: Exception) {
                Log.e("DEBUG_CONVERT", "Lỗi tổng quát: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    private fun copyUriToFile(context: Context, uri: Uri): File {
        // Lấy đuôi file thực tế từ hệ thống
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri)
        val extension = when {
            type?.contains("presentationml.presentation") == true -> "pptx"
            type?.contains("powerpoint") == true -> "ppt"
            type?.contains("opendocument.presentation") == true -> "odp"
            else -> uri.toString().substringAfterLast(".", "pptx")
        }

        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.$extension")

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    // ================= PPTX =================
    private fun convertPptx(file: File, document: Document) {
        FileInputStream(file).use { fis ->
            val ppt = XMLSlideShow(fis)
            Log.d("DEBUG_CONVERT", "Số lượng slide PPTX: ${ppt.slides.size}")

            ppt.slides.forEachIndexed { index, slide ->
                val bitmap = createBitmap()
                val canvas = Canvas(bitmap)
                val paint = createPaint()

                var y = 100f
                canvas.drawText("Slide ${index + 1}", 50f, y, paint)
                y += 80f

                slide.shapes.forEach { shape ->
                    if (shape is XSLFTextShape) {
                        shape.text.split("\n").forEach { line ->
                            if (line.isNotBlank()) {
                                canvas.drawText(line.trim(), 50f, y, paint)
                                y += 50f
                            }
                        }
                    }
                }
                addToPdf(bitmap, document, index == ppt.slides.size - 1)
            }
            ppt.close()
        }
    }

    // ================= PPT =================
    private fun convertPpt(file: File, document: Document) {
        FileInputStream(file).use { fis ->
            val ppt = HSLFSlideShow(fis)
            Log.d("DEBUG_CONVERT", "Số lượng slide PPT: ${ppt.slides.size}")

            ppt.slides.forEachIndexed { index, slide ->
                val bitmap = createBitmap()
                val canvas = Canvas(bitmap)
                val paint = createPaint()

                var y = 100f
                canvas.drawText("Slide ${index + 1}", 50f, y, paint)
                y += 80f

                slide.shapes.forEach { shape ->
                    if (shape is HSLFTextShape) {
                        shape.text.split("\n").forEach { line ->
                            if (line.isNotBlank()) {
                                canvas.drawText(line.trim(), 50f, y, paint)
                                y += 50f
                            }
                        }
                    }
                }
                addToPdf(bitmap, document, index == ppt.slides.size - 1)
            }
            ppt.close()
        }
    }

    // ================= ODP =================
    private fun convertOdp(file: File, document: Document) {
        val tempDir = File(context.cacheDir, "odp_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        unzip(file, tempDir)

        val xmlFile = File(tempDir, "content.xml")
        if (!xmlFile.exists()) return

        val xml = xmlFile.readText()
        val texts = Regex("<text:p[^>]*>(.*?)</text:p>")
            .findAll(xml)
            .map { it.groupValues[1].replace(Regex("<.*?>"), "").trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val chunks = texts.chunked(12)
        chunks.forEachIndexed { index, chunk ->
            val bitmap = createBitmap()
            val canvas = Canvas(bitmap)
            val paint = createPaint()

            var y = 100f
            canvas.drawText("Slide ${index + 1}", 50f, y, paint)
            y += 80f

            chunk.forEach { text ->
                canvas.drawText(text, 50f, y, paint)
                y += 50f
            }
            addToPdf(bitmap, document, index == chunks.size - 1)
        }
        tempDir.deleteRecursively()
    }

    // ================= COMMON =================
    private fun createBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)
        return bmp
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            color = Color.BLACK
            textSize = 32f
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }
    }

    private fun addToPdf(bitmap: Bitmap, document: Document, isLastSlide: Boolean) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageData = ImageDataFactory.create(stream.toByteArray())
        val image = Image(imageData)

        image.setAutoScale(true)
        document.add(image)

        // Nếu không phải slide cuối cùng thì mới ngắt trang
        if (!isLastSlide) {
            document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
        }

        bitmap.recycle() // Giải phóng bộ nhớ tránh OutOfMemory
    }

    private fun unzip(zip: File, target: File) {
        ZipInputStream(FileInputStream(zip)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(target, entry.name)
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}