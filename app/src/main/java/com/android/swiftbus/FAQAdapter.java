package com.android.swiftbus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<FAQ> faqList;

    public FAQAdapter(List<FAQ> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQ faq = faqList.get(position);
        holder.bind(faq);
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    public class FAQViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView questionTextView;
        private TextView answerTextView;
        private ImageView expandIcon;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.faqCardView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            answerTextView = itemView.findViewById(R.id.answerTextView);
            expandIcon = itemView.findViewById(R.id.expandIcon);
        }

        public void bind(FAQ faq) {
            questionTextView.setText(faq.getQuestion());
            answerTextView.setText(faq.getAnswer());

            // Set initial visibility based on expanded state
            if (faq.isExpanded()) {
                answerTextView.setVisibility(View.VISIBLE);
                expandIcon.setRotation(180f);
            } else {
                answerTextView.setVisibility(View.GONE);
                expandIcon.setRotation(0f);
            }

            // Handle click to expand/collapse
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    faq.setExpanded(!faq.isExpanded());

                    if (faq.isExpanded()) {
                        answerTextView.setVisibility(View.VISIBLE);
                        expandIcon.animate().rotation(180f).setDuration(200).start();
                    } else {
                        answerTextView.setVisibility(View.GONE);
                        expandIcon.animate().rotation(0f).setDuration(200).start();
                    }
                }
            });
        }
    }
}