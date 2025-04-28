package com.android.swiftbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentRouteAdapter extends RecyclerView.Adapter<RecentRouteAdapter.REViewHolder>{
    Context context;
    List<RecentRoute> recentRouteList;
    public RecentRouteAdapter(Context context, List<RecentRoute> recentRouteListList) {
        this.context = context;
        this.quickAccessList = quickAccessList;
    }

    @NonNull
    @Override
    public REViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.quick_access_layout,parent,false);
        return new REViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull REViewHolder holder, int position) {
        QuickAccess quickAccess = quickAccessList.get(position);
        holder.title.setText(quickAccess.getTitle());
        holder.image.setImageResource(quickAccess.getImage());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, holder.title.getText()+"Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return quickAccessList.size();
    }

    public class REViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;
        public QAViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            image = itemView.findViewById(R.id.image);
        }


    }
}
