package com.example.onlineexamapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// 1. क्लास का नाम और जेनेरिक टाइप एकदम सही है
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private Context context;
    private List<DiscoveryActivityModel> list;

    // 2. Constructor
    public DashboardAdapter(Context context, List<DiscoveryActivityModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 3. यहाँ छोटे वाले कार्ड (item_dashboard_card) को लिंक किया है
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        DiscoveryActivityModel model = list.get(position);

        // 4. डेटा सेट किया (अब कोई लाल एरर नहीं आएगा)
        holder.title.setText(model.getTitle());
        holder.category.setText(model.getCategory());
        holder.authorName.setText(model.getAuthorId());

        // (अगर तुमने इमेजेज सेट करने का कोई कोड बनाया है, तो वो यहाँ डाल लेना)
    }

    @Override
    public int getItemCount() {
        // 5. सिर्फ 3 कार्ड दिखाने वाला जादू
        if (list.size() > 3) {
            return 3;
        } else {
            return list.size();
        }
    }

    // 6. एकदम फ्रेश ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, authorImage;
        TextView title, category, authorName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 7. सारी सही IDs लगा दी गई हैं
            image = itemView.findViewById(R.id.ivDiscoverThumb);
            authorImage = itemView.findViewById(R.id.ivAuthorThumb);
            title = itemView.findViewById(R.id.tvDiscoverTitle);
            category = itemView.findViewById(R.id.tvDiscoverStats);
            authorName = itemView.findViewById(R.id.tvAuthorName);
        }
    }
}