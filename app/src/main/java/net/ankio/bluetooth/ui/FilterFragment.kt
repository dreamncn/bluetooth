package net.ankio.bluetooth.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quickersilver.themeengine.ThemeEngine
import net.ankio.bluetooth.R
import net.ankio.bluetooth.databinding.FragmentFilterBinding
import net.ankio.bluetooth.utils.BleConstant
import net.ankio.bluetooth.utils.SpUtils

class FilterFragment : DialogFragment() {



    private lateinit var filterCloseInterface: FilterCloseInterface
    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        _binding = FragmentFilterBinding.inflate(inflater, null, false)
        builder.setView(binding.root)
            .setTitle(R.string.filter)
            .setPositiveButton(R.string.save_webdav) { _, _ ->
                dismiss()
            }

        return builder.create()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.switchDeviceName.setOnCheckedChangeListener { _, isChecked ->
            SpUtils.putBoolean(
                BleConstant.NULL_NAME,
                isChecked
            )
        }
        binding.sbRssi.addOnChangeListener { _, value, _ ->
            binding.tvRssi.text = "-"+value.toInt().toString()+" dBm"
            SpUtils.putInt(BleConstant.RSSI, value.toInt())
        }

        binding.textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                SpUtils.putString(BleConstant.COMPANY, s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        //显示效果
        binding.switchDeviceName.isChecked = SpUtils.getBoolean(BleConstant.NULL_NAME)
        //对同一个值进行配置，显示在Seekbar和TextView上
        SpUtils.getInt(BleConstant.RSSI, 100)
            .apply { binding.sbRssi.value = this.toFloat(); binding.tvRssi.text = "-$this dBm" }
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