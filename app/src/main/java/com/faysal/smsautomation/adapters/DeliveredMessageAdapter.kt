package com.faysal.smsautomation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faysal.smsautomation.R
import com.faysal.smsautomation.database.DeliveredSMS

class DeliveredMessageAdapter : RecyclerView.Adapter<DeliveredMessageAdapter.DeliveredItemHolder>() {
    var list  = ArrayList<DeliveredSMS>()

    class DeliveredItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var title : TextView
        lateinit var guid : TextView
        lateinit var timestamp : TextView
        lateinit var result : TextView
        lateinit var fromSim : TextView

        init {
            title = itemView.findViewById(R.id.title)
            guid = itemView.findViewById(R.id.guid)
            timestamp = itemView.findViewById(R.id.timestamp)
            result = itemView.findViewById(R.id.result)
            fromSim = itemView.findViewById(R.id.fromSim)
        }

        fun bind(sms: DeliveredSMS) {
            title.text = "SMS has been send from server to ${sms.sender_phone} this number "
            guid.text = "GUID : "+guid
            timestamp.text = sms.delivered_time+""

            if (sms.isSend){
                result.text = "SUCCESS"
                result.setTextColor(Color.parseColor("#20AD26"))
            }else{
                result.text = "Failure"
                result.setTextColor(Color.parseColor("#FF0600"))
            }

            fromSim.text = sms.fromSim+""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveredItemHolder {
        return DeliveredItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_activites,parent,false))
    }

    override fun onBindViewHolder(holder: DeliveredItemHolder, position: Int) {
       holder.bind(list.get(position))
    }

    fun setList(nlist : List<DeliveredSMS>){
        list.addAll(nlist)
    }

    override fun getItemCount(): Int {
      return list.size
    }
}