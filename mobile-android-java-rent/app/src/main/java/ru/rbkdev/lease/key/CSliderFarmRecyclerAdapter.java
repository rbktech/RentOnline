package ru.rbkdev.lease.key;

import ru.rbkdev.lease.R;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.view.LayoutInflater;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class CSliderFarmRecyclerAdapter extends RecyclerView.Adapter<CSliderFarmRecyclerAdapter.CFarmViewHolder>
{
    public static byte[] g_tag = new byte[11];

    private final List<CFarmSlider> m_listFarm;

    public CSliderFarmRecyclerAdapter() {
        m_listFarm = new ArrayList<>();
    }

    @NotNull
    @Override
    public CFarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ferm_slider, viewGroup, false);
        return new CFarmViewHolder(view);
    }

    public static class CFarmViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_data;
        TextView m_farmId;
        SwitchCompat m_switchFarmEnable;

        public CFarmViewHolder(View itemView) {
            super (itemView);
            m_switchFarmEnable = itemView.findViewById(R.id.switchEnableFerm);
            m_data = itemView.findViewById(R.id.txtDateKey);
            m_farmId = itemView.findViewById(R.id.txtFermID);
        }
    }

    public void onItemDel(int position)
    {
        if(position != -1 && position < m_listFarm.size()) {
            m_listFarm.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    public void onClear()
    {
        int listSize = m_listFarm.size();
        for(int i = 0; i < listSize; i++)
            onItemDel(0);
    }

    public void onItemAdd(@NotNull ArrayList<ByteArrayOutputStream> data)
    {
        CFarmSlider farmSlider = new CFarmSlider();
        farmSlider.m_farmId = data.get(1).toString();
        farmSlider.m_dataBegin = "1.1.2020";
        farmSlider.m_dataEnd = "31.12.2020";
        farmSlider.m_tag = data.get(4).toString();
        farmSlider.m_enable = false;

        m_listFarm.add(0, farmSlider);
        notifyItemInserted(0);
    }

    @Override
    public void onBindViewHolder(CFarmViewHolder viewHolder, final int position) {
        String data = m_listFarm.get(position).m_dataBegin + " - " + m_listFarm.get(position).m_dataEnd;
        viewHolder.m_data.setText(data);
        viewHolder.m_farmId.setText(m_listFarm.get(position).m_tag);
        viewHolder.m_switchFarmEnable.setText(m_listFarm.get(position).m_farmId);
        viewHolder.m_switchFarmEnable.setChecked(m_listFarm.get(position).m_enable);

        viewHolder.m_switchFarmEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked)
            {
                for(int i = 0; i < 10; i++)
                    g_tag[i] = m_listFarm.get(position).m_tag.getBytes()[i];

                if(isChecked)
                    g_tag[10] = 1;
                else
                    g_tag[10] = 0;
            }
        });

    }

    @Override
    public int getItemCount() { return m_listFarm.size(); }
}