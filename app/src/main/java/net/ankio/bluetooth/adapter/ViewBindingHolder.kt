package net.ankio.bluetooth.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.lang.reflect.ParameterizedType

/**
 *
 * @description ViewBindingHolder
 * @author llw
 * @date 2021/9/10 12:43
 */
class ViewBindingHolder<VB : ViewBinding>(val vb: VB, view: View) : BaseViewHolder(view)

abstract class ViewBindingAdapter<VB : ViewBinding, T>(data: MutableList<T>? = null) :
    BaseQuickAdapter<T, ViewBindingHolder<VB>>(0, data) {
    //重写返回自定义 ViewHolder
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder<VB> {
        //这里为了使用简洁性，使用反射来实例ViewBinding
        val viewBindingClass: Class<VB> =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>
        val inflate = viewBindingClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
        val mBinding =
            inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        return ViewBindingHolder(mBinding, mBinding.root)
    }
}