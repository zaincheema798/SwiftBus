package com.android.swiftbus;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentRouteAdapter extends RecyclerView.Adapter<RecentRouteAdapter.RRViewHolder> {
    private Context context;
    private List<RecentRoute> recentRouteList;

    public RecentRouteAdapter(Context context, List<RecentRoute> recentRouteList) {
        this.context = context;
        this.recentRouteList = recentRouteList;
    }

    @NonNull
    @Override
    public RRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_route_layout, parent, false);
        return new RRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RRViewHolder holder, int position) {
        RecentRoute recentRoute = recentRouteList.get(position);
        holder.title.setText(recentRoute.getTitle());
        Picasso.get()
                .load(recentRoute.getImage())
                .placeholder(R.drawable.ic_route)
                .into(holder.image);
        holder.itemView.setOnClickListener(v -> {
            // Handle item click if needed
            Bundle bundle = new Bundle();
            bundle.putSerializable("route", recentRoute);
            Fragment fragment = new Booking();
            fragment.setArguments(bundle);
            ((Main) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.framecontainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return recentRouteList != null ? recentRouteList.size() : 0;
    }

    public static class RRViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView image;

        public RRViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rot);  // Ensure ID exists in XML
            image = itemView.findViewById(R.id.image); // Ensure ID exists in XML
        }
    }
}
