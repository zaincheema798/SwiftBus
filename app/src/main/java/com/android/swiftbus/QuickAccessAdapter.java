package com.android.swiftbus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class QuickAccessAdapter extends RecyclerView.Adapter<QuickAccessAdapter.QAViewHolder> {
    Context context;
    List<QuickAccess> quickAccessList;

    public QuickAccessAdapter(Context context, List<QuickAccess> quickAccessList) {
        this.context = context;
        this.quickAccessList = quickAccessList;
    }

    @NonNull
    @Override
    public QAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.quick_access_layout,parent,false);
        return new QAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QAViewHolder holder, int position) {
        QuickAccess quickAccess = quickAccessList.get(position);
        holder.title.setText(quickAccess.getTitle());
        holder.image.setImageResource(quickAccess.getImage());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0) {
                   // ((Main) context).getSupportFragmentManager().beginTransaction().replace(R.id.framecontainer, new Booking()).addToBackStack(null).commit();

                    ((BottomNavigationView) ((Main) context).findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.booking);

//                    Main main = (Main) context;
//                    BottomNavigationView bottomNavigationView = main.findViewById(R.id.bottom_navigation);
//                    bottomNavigationView.setSelectedItemId(R.id.booking);

                } else if (position==1) {
                    Intent intent = new Intent(context, Offer_Discounts.class);
                    context.startActivity(intent);
                } else if (position==2) {
                    Intent intent = new Intent(context, Travel_History.class);
                    context.startActivity(intent);
                } else if (position==3) {
                    Intent intent = new Intent(context, Help_Support.class);
                    context.startActivity(intent);
                }
        }});
    }

    @Override
    public int getItemCount() {
        return quickAccessList.size();
    }

    public class QAViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public QAViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
        }


    }
}
