package com.example.covertpdfapplication.preview.fragement

import android.content.ContentValues
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.covertpdfapplication.R
import com.example.covertpdfapplication.preview.adapter.PdfPreviewAdapter
import java.io.File
import java.io.FileInputStream

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        filePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                tvFileName.text = file.name
                openFile(file)
            } else {
                Toast.makeText(requireContext(), "File PDF rỗng hoặc chưa tạo xong!", Toast.LENGTH_SHORT).show()
            }
        }

        btnUndo.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnConfirm.setOnClickListener {
            filePath?.let { path ->
                saveFileToDownloads(File(path))
            }
        }
    }

    private fun openFile(file: File) {
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(fileDescriptor!!)

            val pageCount = renderer!!.pageCount
            tvFileInfo.text = "Tổng cộng: $pageCount trang"

            recyclerView.adapter = PdfPreviewAdapter(renderer!!)
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Không thể mở file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFileToDownloads(file: File) {
        val fileName = file.name
        val resolver = requireContext().contentResolver

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Lưu vào thư mục Downloads công khai
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream ->
                    FileInputStream(file).use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }
                Toast.makeText(requireContext(), "Đã lưu vào thư mục Downloads!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Lỗi: Không thể tạo file!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi khi lưu file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        renderer?.close()
        fileDescriptor?.close()
    }
}