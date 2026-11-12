package com.unimarket.man_hinh.trang_chu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.unimarket.databinding.FragmentSapCoBinding

/**
 * Màn tạm cho tab chưa làm tới (Tin nhắn – Sprint 4).
 * File này sẽ bị XOÁ khi các tab đó được làm thật.
 */
class SapCoFragment : Fragment() {

    companion object {
        fun tao(icon: Int, tieuDe: String, moTa: String) = SapCoFragment().apply {
            arguments = Bundle().apply {
                putInt("icon", icon)
                putString("tieuDe", tieuDe)
                putString("moTa", moTa)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val b = FragmentSapCoBinding.inflate(inflater, container, false)
        val thamSo = requireArguments()
        b.icon.setImageResource(thamSo.getInt("icon"))
        b.tvTieuDe.text = thamSo.getString("tieuDe")
        b.tvMoTa.text = thamSo.getString("moTa")
        return b.root
    }
}
