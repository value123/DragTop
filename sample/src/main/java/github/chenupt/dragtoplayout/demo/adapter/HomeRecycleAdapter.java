package github.chenupt.dragtoplayout.demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import github.chenupt.dragtoplayout.demo.R;
import github.chenupt.dragtoplayout.demo.bean.HomeRecycleItem;

/**
 * Created by shenwenjie on 6/7/2016.
 */
public class HomeRecycleAdapter extends RecyclerView.Adapter {
    private ArrayList<HomeRecycleItem> homeRecycleItems;
    private Context mContext;
    private LayoutInflater inflater ;

    public HomeRecycleAdapter(Context context, ArrayList<HomeRecycleItem> homeRecycleItems) {
        this.mContext = context;
        this.homeRecycleItems = homeRecycleItems;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.home_recycle_item,parent,false);
        HomeRecycleHolder meRecycleHolder = new HomeRecycleHolder(view);
        return meRecycleHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HomeRecycleItem meRecycleItem = homeRecycleItems.get(position);
        HomeRecycleHolder viewHolder = (HomeRecycleHolder) holder;
        viewHolder.ivIcon.setImageResource(meRecycleItem.getIconId());
        viewHolder.tvTitle.setText(meRecycleItem.getTitle());
    }

    @Override
    public int getItemCount() {
        return isEmpty() ? 0 : homeRecycleItems.size();
    }

    public boolean isEmpty() {
        return homeRecycleItems.isEmpty() || homeRecycleItems.size() == 0;

    }

    class HomeRecycleHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ll_container)
        LinearLayout llContainer;
        @InjectView(R.id.iv_icon)
        ImageView ivIcon;
        @InjectView(R.id.tv_title)
        TextView tvTitle;

        public HomeRecycleHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeRecycleItem item = homeRecycleItems.get(0);
                    mContext.startActivity(item.getIntent());
                }
            });
        }
    }
}
