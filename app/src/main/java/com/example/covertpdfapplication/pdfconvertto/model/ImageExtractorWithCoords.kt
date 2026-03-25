package com.example.covertpdfapplication.pdfconvertto.model

import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PointF
import com.tom_roush.pdfbox.contentstream.PDFGraphicsStreamEngine
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImage
import java.io.ByteArrayOutputStream
import java.util.Base64

class ImageExtractorWithCoords(page: PDPage): PDFGraphicsStreamEngine(page) {
    val images = mutableListOf<PdfContent.Image>()
    override fun appendRectangle(p0: PointF?, p1: PointF?, p2: PointF?, p3: PointF?) {
    }

    override fun drawImage(pdImage: PDImage?) {
        val ctm = graphicsState.currentTransformationMatrix
        val x = ctm.translateX
        val y = ctm.translateY
        val height = Math.hypot(ctm.scaleY.toDouble(), ctm.shearY.toDouble()).toFloat()
        val width = Math.hypot(ctm.scaleX.toDouble(), ctm.shearX.toDouble()).toFloat()
        val bufferImage = pdImage?.image ?: return

        val baos = ByteArrayOutputStream()
        bufferImage.compress(Bitmap.CompressFormat.PNG, 100, baos)

        val base64Image = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.NO_WRAP)
        images.add(PdfContent.Image(base64Image, x, y, width, height))
        baos.close()
    }

    override fun clip(windingRule: Path.FillType?) {
    }

    override fun moveTo(x: Float, y: Float) {
    }

    override fun lineTo(x: Float, y: Float) {
    }

    override fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
    }

    override fun getCurrentPoint(): PointF {
        TODO("Not yet implemented")
    }

    override fun closePath() {
    }

    override fun endPath() {
    }

    override fun strokePath() {
        TODO("Not yet implemented")
    }

    override fun fillPath(windingRule: Path.FillType?) {
        TODO("Not yet implemented")
    }

    override fun fillAndStrokePath(windingRule: Path.FillType?) {
        TODO("Not yet implemented")
    }

    override fun shadingFill(shadingName: COSName?) {
        TODO("Not yet implemented")
    }

}