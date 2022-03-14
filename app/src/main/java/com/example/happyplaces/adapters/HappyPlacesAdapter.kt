package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.item_happy_place.view.*

open class HappyPlacesAdapter(
    private var context: Context,
    private val list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener : OnClickListener? = null

    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(binding: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (binding is MyViewHolder) {
            binding.itemView.item_imageView.setImageURI(Uri.parse(model.image))
            binding.itemView.item_txt_title.text = model.title
            binding.itemView.item_txt_description.text = model.description
            Log.d("TAG",  binding.itemView.item_txt_title.text.toString())
            binding.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])

        (activity as MainActivity).resultLauncher.launch(intent)
        notifyItemChanged(position)
    }

    private class MyViewHolder(binding: ItemHappyPlaceBinding) :
        RecyclerView.ViewHolder(binding.root)
}