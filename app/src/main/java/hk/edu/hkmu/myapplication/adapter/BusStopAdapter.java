package hk.edu.hkmu.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hk.edu.hkmu.myapplication.R;
import hk.edu.hkmu.myapplication.model.BusStop;

/**
 * 巴士站點列表適配器
 */
public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.ViewHolder> {
    private List<BusStop> stopList = new ArrayList<>();
    private boolean isEnglish = false;
    
    public BusStopAdapter() {
        // 检查当前语言设置
        Locale currentLocale = Locale.getDefault();
        isEnglish = !currentLocale.getLanguage().equals(Locale.CHINESE.getLanguage());
    }
    
    public void updateData(List<BusStop> newStops) {
        this.stopList = newStops;
        notifyDataSetChanged();
    }
    
    public void updateLanguageSetting(boolean isEnglish) {
        this.isEnglish = isEnglish;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_stop, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusStop stop = stopList.get(position);
        
        // 设置站点名称（包括序号）
        String stopName = (position + 1) + ". " + (isEnglish ? stop.getNameEN() : stop.getNameTC());
        holder.stopName.setText(stopName);
        
        // 设置顶部和底部连接线的可见性
        holder.stopLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.stopLineBottom.setVisibility(position == getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);
        
        // 设置站点图标颜色
        holder.stopIcon.setBackgroundResource(R.drawable.circle_background_red);
        
        // 设置每个位置的唯一ID，以便进行动画处理
        holder.itemView.setId(position);
        
        // 设置点击监听器（可选）
        holder.itemView.setOnClickListener(v -> {
            // 根据需要处理点击事件，例如展开/折叠详情
            if (holder.eta.getVisibility() == View.GONE) {
                holder.eta.setVisibility(View.VISIBLE);
                holder.eta.setText(isEnglish ? "Stop details will show here" : "站點詳情會顯示在這里");
            } else {
                holder.eta.setVisibility(View.GONE);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return stopList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View stopLineTop;
        View stopLineBottom;
        ImageView stopIcon;
        TextView stopName;
        TextView eta;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stopLineTop = itemView.findViewById(R.id.stop_line_top);
            stopLineBottom = itemView.findViewById(R.id.stop_line_bottom);
            stopIcon = itemView.findViewById(R.id.stop_icon);
            stopName = itemView.findViewById(R.id.tv_stop_name);
            eta = itemView.findViewById(R.id.tv_eta);
        }
    }
} 