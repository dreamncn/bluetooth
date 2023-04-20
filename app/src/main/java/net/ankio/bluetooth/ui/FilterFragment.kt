package net.ankio.bluetooth.ui

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quickersilver.themeengine.ThemeEngine
import net.ankio.bluetooth.databinding.FragmentFilterBinding
import net.ankio.bluetooth.utils.BleConstant
import net.ankio.bluetooth.utils.SpUtils

class FilterFragment : BottomSheetDialogFragment() {

    private lateinit var themeEngine: ThemeEngine

    private lateinit var filterCloseInterface: FilterCloseInterface
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeEngine = ThemeEngine.getInstance(requireContext())
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.switchDeviceName.setOnCheckedChangeListener { _, isChecked ->
            SpUtils.putBoolean(
                BleConstant.NULL_NAME,
                isChecked
            )
        }
        binding.sbRssi.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.tvRssi.text = "-$progress dBm"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                SpUtils.putInt(BleConstant.RSSI, seekBar.progress)
            }
        })
        binding.textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                SpUtils.putString(BleConstant.COMPANY, s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        binding.save.setOnClickListener { dismiss() }
        //显示效果
        binding.switchDeviceName.isChecked = SpUtils.getBoolean(BleConstant.NULL_NAME)
        //对同一个值进行配置，显示在Seekbar和TextView上
        SpUtils.getInt(BleConstant.RSSI, 100)
            .apply { binding.sbRssi.progress = this; binding.tvRssi.text = "-$this dBm" }
        SpUtils.getString(BleConstant.COMPANY, "")
            .apply { binding.textField.text = Editable.Factory.getInstance().newEditable(this) }
    }

    fun setOnCloseListener(filterCloseInterface: FilterCloseInterface): FilterFragment {
        this.filterCloseInterface = filterCloseInterface
        return this
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (::filterCloseInterface.isInitialized) {
            filterCloseInterface.onClose()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (::filterCloseInterface.isInitialized) {
            filterCloseInterface.onClose()
        }
    }


    interface FilterCloseInterface {
        fun onClose()
    }

}