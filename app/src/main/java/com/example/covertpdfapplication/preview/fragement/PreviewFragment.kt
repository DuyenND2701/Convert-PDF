package com.example.covertpdfapplication.preview

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.covertpdfapplication.R
import java.io.File

class PreviewFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvFileName: TextView
    private lateinit var tvFileInfo: TextView
    private lateinit var btnUndo: Button
    private lateinit var btnConfirm: Button

    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    private var filePath: String? = null

    companion object {
        fun newInstance(path: String): PreviewFragment {
            val fragment = PreviewFragment()
            val bundle = Bundle()
            bundle.putString("path", path)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePath = arguments?.getString("path")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_pdf_pages_preview)
        tvFileName = view.findViewById(R.id.tv_file_name_result)
        tvFileInfo = view.findViewById(R.id.tv_file_info_result)
        btnUndo = view.findViewById(R.id.btn_undo)
        btnConfirm = view.findViewById(R.id.btn_confirm_convert)

        filePath?.let {
            val file = File(it)
            tvFileName.text = file.name
            openPdf(file)
        }

        btnUndo.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnConfirm.setOnClickListener {
            // xử lý lưu / share file
        }
    }

    private fun openPdf(file: File) {
        fileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        renderer = PdfRenderer(fileDescriptor!!)

        val pageCount = renderer!!.pageCount
        tvFileInfo.text = "Tổng cộng: $pageCount trang"

//        recyclerView.adapter = PdfPreviewAdapter(renderer!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderer?.close()
        fileDescriptor?.close()
    }
}