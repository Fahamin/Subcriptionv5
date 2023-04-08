package medication.smartpatient.subcriptionv5

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import medication.smartpatient.subcriptionv5.RemoveAdsAdapter.RemoveADS

class RemoveAdsAdapter(
    var context: Context,
    var productDetailsList: List<ProductDetails>,
    var activity: Activity,
    var itemClickListener: ItemClickListener
) : RecyclerView.Adapter<RemoveADS>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemoveADS {
        val view =
            LayoutInflater.from(context).inflate(R.layout.carousel_items_will_dev, null, false)
        return RemoveADS(view)
    }

    override fun onBindViewHolder(holder: RemoveADS, @SuppressLint("RecyclerView") position: Int) {
        if (position == 0) {
            holder.price.text = "$1.29"
            holder.title.text = "1 Month"
        }
        if (position == 1) {
            holder.price.text = "$3.49"
            holder.title.text = "3 Month"
        }
        if (position == 2) {
            holder.price.text = "$0.50"
            holder.title.text = "14 Days"
        }
        holder.itemView.setOnClickListener { itemClickListener.clickListener(position) }
    }

    override fun getItemCount(): Int {
        return productDetailsList.size
    }

    inner class RemoveADS(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var price: TextView
        var title: TextView

        init {
            price = itemView.findViewById(R.id.tvPrice)
            title = itemView.findViewById(R.id.tvMonth)
        }
    }
}