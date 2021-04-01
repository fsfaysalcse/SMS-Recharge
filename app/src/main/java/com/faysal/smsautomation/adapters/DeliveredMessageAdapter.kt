package com.faysal.smsautomation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faysal.smsautomation.R
import com.faysal.smsautomation.database.Activites

class DeliveredMessageAdapter : RecyclerView.Adapter<DeliveredMessageAdapter.DeliveredItemHolder>() {
    var list  = ArrayList<Activites>()

    class DeliveredItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var title : TextView
        lateinit var ex_note : TextView
        lateinit var timestamp : TextView
        lateinit var result : TextView
        lateinit var fromSim : TextView

        init {
            title = itemView.findViewById(R.id.title)
            ex_note = itemView.findViewById(R.id.ex_note)
            timestamp = itemView.findViewById(R.id.timestamp)
            result = itemView.findViewById(R.id.result)
            fromSim = itemView.findViewById(R.id.fromSim)
        }

        fun bind(activites : Activites) {
            title.text = "${activites.message}"+""
           // ex_note.text = "GUID : "+activites.sender_phone
            timestamp.text = activites.timestamp+""

            if (activites.status){
                result.text = "SUCCESS"
                result.setTextColor(Color.parseColor("#20AD26"))
            }else{
                result.text = "Failure"
                result.setTextColor(Color.parseColor("#FF0600"))
            }

            fromSim.text = activites.fromSim+""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveredItemHolder {
        return DeliveredItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_activites,parent,false))
    }

    override fun onBindViewHolder(holder: DeliveredItemHolder, position: Int) {

       holder.bind(list.get(position))
    }

    fun setList(nlist : List<Activites>){
        list.clear()
        list.addAll(nlist)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
      return list.size
    }
}