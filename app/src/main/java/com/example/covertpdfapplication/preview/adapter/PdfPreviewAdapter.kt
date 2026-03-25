package com.example.covertpdfapplication.preview.adapter

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PdfPreviewAdapter(
    private val renderer: PdfRenderer
) : RecyclerView.Adapter<PdfPreviewAdapter.PdfViewHolder>() {

    inner class PdfViewHolder(val imageView: ImageView) :
        RecyclerView.ViewHolder(imageView)

    override fun getItemCount(): Int = renderer.pageCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {

        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1200
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(8, 8, 8, 8)
        }

        return PdfViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {

        val page = renderer.openPage(position)

        val bitmap = Bitmap.createBitmap(
            page.width,
            page.height,
            Bitmap.Config.ARGB_8888
        )

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        holder.imageView.setImageBitmap(bitmap)

        page.close()
    }
}