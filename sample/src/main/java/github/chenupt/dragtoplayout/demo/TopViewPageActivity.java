package github.chenupt.dragtoplayout.demo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import github.chenupt.dragtoplayout.DragTopLayout;
import github.chenupt.dragtoplayout.demo.adapter.HomeRecycleAdapter;
import github.chenupt.dragtoplayout.demo.bean.HomeRecycleItem;
import github.chenupt.dragtoplayout.demo.utils.DensityUtil;

public class TopViewPageActivity extends ActionBarActivity {


    @InjectView(R.id.view_pager)
    ViewPager viewPager;
    @InjectView(R.id.drag_layout)
    DragTopLayout dragLayout;
    @InjectView(R.id.ll_top)
    RelativeLayout llTop;
    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;
    @InjectView(R.id.ll_content)
    LinearLayout llContent;
    @InjectView(R.id.activity_top_view_page)
    RelativeLayout activityTopViewPage;
//    @InjectView(R.id.scroll_view)
//    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_view_page);
        ButterKnife.inject(this);
        initViewPager();
        initRecycleView();
//        scrollView.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View v, DragEvent event) {
//                dragLayout.setTouchMode(true);
//                return false;
//            }
//        });
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initRecycleView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<HomeRecycleItem> items = new ArrayList<HomeRecycleItem>(8);
        for (int i = 0; i < 8; i++) {
            HomeRecycleItem homeRecycleItem = new HomeRecycleItem(R.id.icon, "hello", new Intent(this, MainActivity.class));
            items.add(homeRecycleItem);
        }
        HomeRecycleAdapter homeRecycleAdapter = new HomeRecycleAdapter(this, items);
        recyclerView.setAdapter(homeRecycleAdapter);
        dragLayout.setOverDrag(false);
    }

    private void initViewPager() {
        viewPager.setAdapter(pagerAdapter);
    }

    private PagerAdapter pagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = TopViewPageActivity.this;
            Resources resources = context.getResources();
            ImageView imageView = new ImageView(context);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            float height = DensityUtil.dp2px(resources, 200);
            layoutParams.height = (int) height;
            imageView.setLayoutParams(layoutParams);

            try {
                Glide.with(context).load(new URL("http://www.bz55.com/uploads/allimg/141128/139-14112Q43H2.jpg")).into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    };
}
