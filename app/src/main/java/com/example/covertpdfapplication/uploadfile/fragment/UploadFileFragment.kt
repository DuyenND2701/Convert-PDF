package com.example.covertpdfapplication.uploadfile.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.covertpdfapplication.R
import com.example.covertpdfapplication.common.FormatData
import com.example.covertpdfapplication.convert.converttopdf.factory.ConvertToPdfFactory
import com.example.covertpdfapplication.preview.fragement.PreviewFragment
import com.example.covertpdfapplication.uploadfile.model.SelectedFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadFileFragment : Fragment() {

    private val PREFS_NAME = "UploadPrefs"
    private val KEY_LAST_FILE = "LastFileName"
    private val globalSelectedFiles = mutableListOf<SelectedFile>()

    // View thuộc Fragment chính
    private var spinnerFrom: AutoCompleteTextView? = null
    private var spinnerTo: AutoCompleteTextView? = null
    private var btnConvert: MaterialButton? = null

    // View thuộc BottomSheet
    private var tvRecentDisplay: TextView? = null
    private var layoutFileList: LinearLayout? = null
    private var tvEmptyStatus: TextView? = null
    private var ivMainPreview: ImageView? = null

    private val pickFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.let { handleSelectedFiles(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_file, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddFileMain = view.findViewById<MaterialButton>(R.id.btn_add_file)
        spinnerFrom = view.findViewById(R.id.spinner_from)
        spinnerTo = view.findViewById(R.id.spinner_to)
        btnConvert = view.findViewById(R.id.btn_convert)

        btnAddFileMain?.setOnClickListener { showBottomSheet() }

        btnConvert?.setOnClickListener {

            val selectedFiles = globalSelectedFiles.filter { it.isSelected }

            if (selectedFiles.isEmpty()) {
                Toast.makeText(requireContext(), "Chọn file trước", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {

                val factory = ConvertToPdfFactory(requireContext())

                val firstFile = selectedFiles.first()
                val ext = firstFile.name.substringAfterLast(".", "")

                val converter = factory.getConverter(ext)

                if (converter == null) {
                    Toast.makeText(requireContext(), "Không hỗ trợ format", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val result = converter.convertToPdf(
                    requireContext(),
                    selectedFiles.map { it.uri }
                )

                if (result != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Convert thành công", Toast.LENGTH_SHORT).show()
                        openPreview(result.absolutePath)
                    }
                } else {
                    Toast.makeText(requireContext(), "Convert thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch { setupSourceSpinner() }
    }

    private fun openPreview(path: String) {
        // 1. Khởi tạo Fragment Preview kèm theo đường dẫn file PDF vừa convert xong
        val previewFragment = PreviewFragment.newInstance(path)

        // 2. Thực hiện transaction để thay thế màn hình Upload bằng màn hình Preview
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, previewFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_file_manager, null)

        tvRecentDisplay = bottomSheetView.findViewById(R.id.tv_recent_display)
        layoutFileList = bottomSheetView.findViewById(R.id.layout_file_list)
        tvEmptyStatus = bottomSheetView.findViewById(R.id.tv_empty_status)


        val btnUploadMultiple = bottomSheetView.findViewById<TextView>(R.id.btn_upload_multiple)
        val btnAddFileDialog = bottomSheetView.findViewById<MaterialButton>(R.id.btn_add_file)

        tvRecentDisplay?.text = "Gần đây"

        reloadFilesToUI()

        btnUploadMultiple?.setOnClickListener { pickFilesLauncher.launch("*/*") }
        btnAddFileDialog?.setOnClickListener { pickFilesLauncher.launch("*/*") }

        dialog.setContentView(bottomSheetView)
        dialog.show()
    }

    private fun handleSelectedFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return

        uris.forEach { uri ->
            val name = getFileName(uri)
            val size = getFileSize(uri)
            val time = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date())

            // Kiểm tra xem file đã tồn tại trong danh sách chưa
            val existingFile = globalSelectedFiles.find { it.name == name }

            if (existingFile != null) {
                // Ghi đè thông tin và tăng version
                val index = globalSelectedFiles.indexOf(existingFile)
                val newVersion = existingFile.version + 1

                globalSelectedFiles[index] = SelectedFile(
                    name = name,
                    size = size,
                    time = time,
                    uri = uri,
                    isSelected = existingFile.isSelected,
                    version = newVersion
                )
            } else {
                // Thêm mới với mặc định V1
                globalSelectedFiles.add(SelectedFile(name, size, time, uri, version = 1))
            }
        }

        // cập nhật V1 -> V2
        reloadFilesToUI()

        if (globalSelectedFiles.isNotEmpty()) {
            val lastFile = globalSelectedFiles.last()
            saveFileToPrefs(lastFile.name)
            updatePreview(lastFile.uri, lastFile.name)
        }
    }

    private fun reloadFilesToUI() {
        layoutFileList?.removeAllViews()
        if (globalSelectedFiles.isEmpty()) {
            tvEmptyStatus?.visibility = View.VISIBLE
        } else {
            tvEmptyStatus?.visibility = View.GONE
            globalSelectedFiles.forEach { file -> addFileEntryToUI(file) }
        }
    }

    private fun addFileEntryToUI(file: SelectedFile) {
        val context = context ?: return
        tvEmptyStatus?.visibility = View.GONE

        val rowContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(15, 25, 15, 25)
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = context.getDrawable(android.R.drawable.list_selector_background)
        }

        // 1. CheckBox để chọn nhiều
        val checkBox = CheckBox(context).apply {
            isChecked = file.isSelected
            setOnCheckedChangeListener { _, isChecked -> file.isSelected = isChecked }
        }

        // 2. Icon file
        val imgIcon = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_save)
            layoutParams = LinearLayout.LayoutParams(60, 60).apply { marginEnd = 20 }
        }

        // 3. Text Info
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvName = TextView(context).apply {
            text = file.name
            textSize = 14f
            setTextColor(android.graphics.Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val tvInfo = TextView(context).apply {
            text = "${file.size} • ${file.time}"
            textSize = 11f
            setTextColor(android.graphics.Color.GRAY)
        }

        textContainer.addView(tvName)
        textContainer.addView(tvInfo)

        // 4. Badge Version (Hiển thị động V1, V2...)
        val tvVersion = TextView(context).apply {
            text = "V${file.version}"
            textSize = 10f
            setPadding(14, 4, 14, 4)
            setTextColor(0xFF4285F4.toInt())
            setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame)
        }

        rowContainer.addView(checkBox)
        rowContainer.addView(imgIcon)
        rowContainer.addView(textContainer)
        rowContainer.addView(tvVersion)

        layoutFileList?.addView(rowContainer)
    }

    private fun updatePreview(uri: Uri, name: String) {
        val lowerName = name.lowercase()
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".png") || lowerName.endsWith(".jpeg")) {
            ivMainPreview?.setImageURI(uri)
        } else {
            ivMainPreview?.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun saveFileToPrefs(name: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_FILE, name).apply()
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown"
        requireContext().contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx != -1) name = it.getString(idx)
            }
        }
        return name
    }

    private fun getFileSize(uri: Uri): String {
        var size: Long = 0
        requireContext().contentResolver.query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.SIZE)
                if (idx != -1) size = it.getLong(idx)
            }
        }
        return if (size > 1024 * 1024) String.format("%.1f MB", size / (1024.0 * 1024.0)) else "${size / 1024} KB"
    }

    private suspend fun setupSourceSpinner() {
        val items = withContext(Dispatchers.Default) {
            val list = mutableListOf<String>()
            FormatData.sourceFormatMap.forEach { (cat, formats) ->
                formats.forEach { list.add("$cat ($it)") }
            }
            list
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        spinnerFrom?.setAdapter(adapter)
        if (items.isNotEmpty()) {
            spinnerFrom?.setText(items[0], false)
            updateTargetLogic(items[0])
        }
        spinnerFrom?.setOnItemClickListener { _, _, pos, _ -> updateTargetLogic(items[pos]) }
    }

    private fun updateTargetLogic(selected: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val targets = withContext(Dispatchers.Default) {
                if (selected.contains("(PDF)")) {
                    val list = mutableListOf<String>()
                    FormatData.targetFormatMap.forEach { (cat, formats) ->
                        formats.forEach { list.add("$cat ($it)") }
                    }
                    list
                } else listOf("Document (PDF)")
            }
            spinnerTo?.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, targets))
            if (targets.isNotEmpty()) spinnerTo?.setText(targets[0], false)
        }
    }
}