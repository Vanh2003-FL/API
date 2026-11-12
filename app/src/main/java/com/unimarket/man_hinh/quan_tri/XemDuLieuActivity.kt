package com.unimarket.man_hinh.quan_tri

import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.unimarket.R
import com.unimarket.databinding.ActivityXemDuLieuBinding
import com.unimarket.databinding.ChipLocBinding
import com.unimarket.du_lieu.database.AppDatabase
import com.unimarket.du_lieu.repository.QuanTriRepository
import com.unimarket.tien_ich.CanhLe
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Xem trực tiếp 9 bảng SQLite trong app (giống DB Browser for SQLite thu nhỏ) và xuất dữ liệu:
 * - Xuất CSV: cả bảng đang chọn, mở được bằng Excel.
 * - Xuất .db: nguyên file unimarket.db để mở bằng DB Browser for SQLite trên máy tính.
 * Việc lưu file dùng hộp chọn của Android (Storage Access Framework) nên không cần xin quyền bộ nhớ.
 */
class XemDuLieuActivity : AppCompatActivity() {

    companion object {
        private const val SO_DONG_HIEN = 300
        private const val RONG_O_TOI_DA = 26 // ký tự

        /** Cột lưu thời điểm dạng mili-giây → hiện "12/11/26 14:30" cho dễ đọc. */
        private val COT_THOI_GIAN = Regex("(?i)(ngay|thoiGian|Luc$|capNhat|hetHan)")
    }

    private lateinit var binding: ActivityXemDuLieuBinding
    private lateinit var repo: QuanTriRepository
    private var bangChon = QuanTriRepository.DS_BANG[1]
    private var dsDong: List<String> = emptyList()
    private val gio = SimpleDateFormat("dd/MM/yy HH:mm", Locale("vi"))

    private val luuCsv = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val so = contentResolver.openOutputStream(uri)?.let { repo.xuatCsv(bangChon, it) } ?: 0
            Toast.makeText(this@XemDuLieuActivity, "Đã xuất $so dòng của bảng $bangChon", Toast.LENGTH_SHORT).show()
        }
    }

    private val luuDb = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri ?: return@registerForActivityResult
        lifecycleScope.launch {
            val byte = contentResolver.openOutputStream(uri)?.let { repo.xuatFileDb(it) } ?: 0
            Toast.makeText(this@XemDuLieuActivity, "Đã xuất file .db (${byte / 1024} KB)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXemDuLieuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CanhLe.chuaVienHeThong(binding.root)
        repo = QuanTriRepository(this)

        binding.thanhTieuDe.setNavigationOnClickListener { finish() }
        binding.thanhTieuDe.menu.add("Xuất CSV").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener { luuCsv.launch("$bangChon.csv"); true }
        }
        binding.thanhTieuDe.menu.add("Xuất .db").apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener { luuDb.launch(AppDatabase.TEN_FILE); true }
        }

        for (ten in QuanTriRepository.DS_BANG) {
            val chip = ChipLocBinding.inflate(layoutInflater, binding.nhomBang, false).root
            chip.id = View.generateViewId()
            chip.text = ten
            chip.tag = ten
            binding.nhomBang.addView(chip)
            if (ten == bangChon) binding.nhomBang.check(chip.id)
        }
        binding.nhomBang.setOnCheckedStateChangeListener { g, ids ->
            bangChon = ids.firstOrNull()?.let { g.findViewById<Chip>(it).tag as String } ?: return@setOnCheckedStateChangeListener
            taiBang()
        }
        binding.dsDong.layoutManager = LinearLayoutManager(this)
        binding.dsDong.adapter = adapter
        QuyenQuanTri.kiemTra(this) { taiBang() }
    }

    private fun taiBang() {
        lifecycleScope.launch {
            val bang = repo.docBang(bangChon, SO_DONG_HIEN)
            // Độ rộng mỗi cột = độ dài lớn nhất (tối đa RONG_O_TOI_DA ký tự) → các dòng thẳng hàng như bảng
            val dong = bang.dong.map { d -> d.mapIndexed { i, o -> hienO(bang.cot[i], o) } }
            val rong = bang.cot.indices.map { i ->
                (listOf(bang.cot[i].length) + dong.map { it[i].length }).max().coerceAtMost(RONG_O_TOI_DA)
            }
            fun ghep(o: List<String>) = o.mapIndexed { i, s -> catNgan(s, rong[i]).padEnd(rong[i]) }.joinToString(" │ ")
            binding.tvTieuDeCot.text = ghep(bang.cot)
            dsDong = dong.map { ghep(it) }
            adapter.notifyDataSetChanged()
            binding.dsDong.scrollToPosition(0)
            binding.tvThongTin.text = "Bảng $bangChon · ${bang.tongDong} dòng · ${bang.cot.size} cột" +
                if (bang.tongDong > SO_DONG_HIEN) " · đang hiện $SO_DONG_HIEN dòng đầu (xuất CSV để xem đủ)" else ""
        }
    }

    private fun hienO(cot: String, o: String?): String = when {
        o == null -> "NULL"
        COT_THOI_GIAN.containsMatchIn(cot) && o.toLongOrNull()?.let { it > 1_000_000_000_000L } == true -> gio.format(Date(o.toLong()))
        else -> o.replace('\n', '⏎')
    }

    private fun catNgan(s: String, toiDa: Int) = if (s.length <= toiDa) s else s.take(toiDa - 1) + "…"

    private val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount() = dsDong.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val dp = parent.resources.displayMetrics.density
            val tv = TextView(parent.context).apply {
                typeface = Typeface.MONOSPACE
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.chu))
                setPadding((8 * dp).toInt(), (5 * dp).toInt(), (8 * dp).toInt(), (5 * dp).toInt())
                maxLines = 1
            }
            return object : RecyclerView.ViewHolder(tv) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val tv = holder.itemView as TextView
            tv.text = dsDong[position]
            tv.setBackgroundColor(ContextCompat.getColor(tv.context, if (position % 2 == 0) R.color.be_mat else R.color.nen))
        }
    }
}
