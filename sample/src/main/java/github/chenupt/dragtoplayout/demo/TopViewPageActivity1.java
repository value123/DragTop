package github.chenupt.dragtoplayout.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import github.chenupt.dragtoplayout.demo.adapter.HomeRecycleAdapter;
import github.chenupt.dragtoplayout.demo.bean.HomeRecycleItem;
import github.chenupt.dragtoplayout.demo.utils.DensityUtil;

public class TopViewPageActivity1 extends Activity {

    private static final int RECYCLE_SIZE = 20;
//    @InjectView(R.id.view_pager)
//    ViewPager viewPager;
    @InjectView(R.id.recycler_view)
    RecyclerView contentRecyclerView;
//    @InjectView(R.id.top_recycler_view)
//    RecyclerView topRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_view_page1);
        ButterKnife.inject(this);
        // 第一步，初始化ViewPager和TabLayout
//        initViewPager();
//        initRecycleView(topRecyclerView);
        initRecycleView(contentRecyclerView);
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

    int currentItemIndex = 0;
    boolean isScroll = false;
    @Override
    protected void onResume() {
        super.onResume();

//        Timer timer = new Timer();
//
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(isScroll)
//                            return;
//                        currentItemIndex++;
//                        if(currentItemIndex>2){
//                            viewPager.setCurrentItem(currentItemIndex%3);
//                        }else{
//                            viewPager.setCurrentItem(currentItemIndex);
//
//                        }
////                        tvTest.setText(tvTest.getText().toString()+tvTest.getText().toString());
//                    }
//                });
//            }
//        };
//        timer.scheduleAtFixedRate(timerTask, 0, 2_000);
    }

    private void initRecycleView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new FullyLinearLayoutManager(this));

        ArrayList<HomeRecycleItem> items = new ArrayList<HomeRecycleItem>(RECYCLE_SIZE);
        for (int i = 0; i < RECYCLE_SIZE; i++) {
            HomeRecycleItem homeRecycleItem = new HomeRecycleItem(R.id.icon, "hello::"+i, new Intent(this, MainActivity.class));
            items.add(homeRecycleItem);
        }
        HomeRecycleAdapter homeRecycleAdapter = new HomeRecycleAdapter(this, items);
        recyclerView.setAdapter(homeRecycleAdapter);
        recyclerView.setNestedScrollingEnabled(false);

    }

    private void initViewPager() {

//        viewPager.setAdapter(pagerAdapter);
    }

    private PagerAdapter pagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = TopViewPageActivity1.this;
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
