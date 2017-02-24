/*
 * Copyright 2015 chenupt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 */

package github.chenupt.dragtoplayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by chenupt@gmail.com on 2015/1/18.
 * Description : Drag down to show a menu panel on the top.
 */
public class DragTopLongLayout extends FrameLayout {

    private static final String TAG = "DragTopLayout";
    private ViewDragHelper dragHelper;
    private int dragRange;
    private View dragContentView;
    private ViewGroup topView;

    private int contentTop;
    private int topViewHeight;
    private float ratio;
    private boolean isRefreshing;
    private boolean shouldIntercept = true;

    private PanelListener panelListener;
    private float refreshRatio = 1.5f;
    private boolean overDrag = true;
    private int collapseOffset;
    private int topViewId = -1;
    private int dragContentViewId = -1;
    private boolean captureTop = true;

    // Used for scrolling
    private boolean dispatchingChildrenDownFaked = false;
    private boolean dispatchingChildrenContentView = false;
    private float dispatchingChildrenStartedAtY = Float.MAX_VALUE;

    private PanelState panelState = PanelState.EXPANDED;


    public static enum PanelState {

        COLLAPSED(0),
        EXPANDED(1),
        SLIDING(2);

        private int asInt;

        PanelState(int i) {
            this.asInt = i;
        }

        static PanelState fromInt(int i) {
            switch (i) {
                case 0:
                    return COLLAPSED;
                case 2:
                    return SLIDING;
                default:
                case 1:
                    return EXPANDED;
            }
        }

        public int toInt() {
            return asInt;
        }
    }



    public DragTopLongLayout(Context context) {
        this(context, null);
    }

    public DragTopLongLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragTopLongLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    GestureDetectorCompat mDetectorCompat;
    private void init(AttributeSet attrs) {
        dragHelper = ViewDragHelper.create(this, 1.0f, callback);
        mDetectorCompat = new GestureDetectorCompat(getContext(),
                mGestureListener);

        // init from attrs
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DragTopLayout);
        setCollapseOffset(a.getDimensionPixelSize(R.styleable.DragTopLayout_dtlCollapseOffset, collapseOffset));
        overDrag = a.getBoolean(R.styleable.DragTopLayout_dtlOverDrag, overDrag);
        dragContentViewId = a.getResourceId(R.styleable.DragTopLayout_dtlDragContentView, -1);
        topViewId = a.getResourceId(R.styleable.DragTopLayout_dtlTopView, -1);
        initOpen(a.getBoolean(R.styleable.DragTopLayout_dtlOpen, true));
        captureTop = a.getBoolean(R.styleable.DragTopLayout_dtlCaptureTop, true);
        a.recycle();
    }

    private void initOpen(boolean initOpen){
        if (initOpen) {
            panelState = PanelState.EXPANDED;
        } else {
            panelState = PanelState.COLLAPSED;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        if (getChildCount() < 2) {
            throw new RuntimeException("Content view must contains two child views at least.");
        }

        if (topViewId != -1 && dragContentViewId == -1) {
            throw new IllegalArgumentException("You have set \"dtlTopView\" but not \"dtlDragContentView\". Both are required!");
        }

        if (dragContentViewId != -1 && topViewId == -1) {
            throw new IllegalArgumentException("You have set \"dtlDragContentView\" but not \"dtlTopView\". Both are required!");
        }

        if (dragContentViewId != -1 && topViewId != -1) {
            bindId(this);
        } else {
            topView = (ViewGroup) getChildAt(0);
            dragContentView = getChildAt(1);
        }
    }

    private void bindId(View view) {
        topView = (ViewGroup) view.findViewById(topViewId);
        dragContentView = view.findViewById(dragContentViewId);

        if (topView == null) {
            throw new IllegalArgumentException("\"dtlTopView\" with id = \"@id/"
                    + getResources().getResourceEntryName(topViewId)
                    + "\" has NOT been found. Is a child with that id in this " + getClass().getSimpleName() + "?");
        }


        if (dragContentView == null) {
            throw new IllegalArgumentException("\"dtlDragContentView\" with id = \"@id/"
                    + getResources().getResourceEntryName(dragContentViewId)
                    + "\" has NOT been found. Is a child with that id in this "
                    + getClass().getSimpleName()
                    + "?");
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        dragRange = getHeight();
        Log.d(TAG,"onLayout dragRange = " + dragRange);
        // In case of resetting the content top to target position before sliding.
        int contentTopTemp = contentTop;
        Log.d(TAG,"onLayout contentTop = " + contentTop);
        resetTopViewHeight();
        resetContentHeight();

        topView.layout(left, Math.min(topView.getPaddingTop(), contentTop - topViewHeight), right,
                contentTop);
        dragContentView.layout(left, contentTopTemp, right,
                contentTopTemp + dragContentView.getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG,"onMeasure");
        measureTopHeight();
        Log.d(TAG,"onMeasure topContainerHeight="+topContainerHeight);
    }

    int topContainerHeight = 0;
    private void measureTopHeight() {
        topContainerHeight = 0;
        if(topView instanceof LinearLayout /*&& ((LinearLayout) topView).getOrientation() == LinearLayout.VERTICAL*/) {
            int childCount = topView.getChildCount();
            if(childCount==0){
                topContainerHeight = topView.getHeight();
            }else{
                for (int i = 0; i < childCount; i++) {
                    View view = topView.getChildAt(i);
                    if (view.getVisibility() == View.VISIBLE) {
                        topContainerHeight += view.getMeasuredHeight();
                    }
                }
            }
        }else{
            topContainerHeight = topView.getHeight();
        }

    }

    private void resetTopViewHeight() {

        int newTopHeight = topContainerHeight;
        // Top layout is changed
        Log.d(TAG,"onLayout newTopHeight = " + newTopHeight );
        if (topViewHeight != newTopHeight) {
            if (panelState == PanelState.EXPANDED) {
                contentTop = newTopHeight;
                Log.d(TAG,"onLayout EXPANDED contentTop>>>"+contentTop);
                handleSlide(newTopHeight);
            } else if(panelState == PanelState.COLLAPSED){
                // update the drag content top when it is collapsed.
                contentTop = collapseOffset;
                Log.d(TAG,"onLayout collapseOffset>>>"+collapseOffset);
            }
            topViewHeight = newTopHeight;
            Log.d(TAG,"onLayout topViewHeight>>>"+topViewHeight);
        }
    }

    private void resetContentHeight() {
        Log.d(TAG,"resetContentHeight getHeight()="+getHeight()+">>>collapseOffset="+collapseOffset);
        if (dragContentView != null && dragContentView.getHeight() != 0) {
            ViewGroup.LayoutParams layoutParams = dragContentView.getLayoutParams();
            layoutParams.height = getHeight() - collapseOffset;
            dragContentView.setLayoutParams(layoutParams);
        }
    }

    private void handleSlide(final int top) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                dragHelper.smoothSlideViewTo(dragContentView, getPaddingLeft(), top);
                postInvalidate();
            }
        });
    }

    private void resetDragContent(boolean anim, int top) {
        contentTop = top;
        if (anim) {
            dragHelper.smoothSlideViewTo(dragContentView, getPaddingLeft(), contentTop);
            postInvalidate();
        } else {
            requestLayout();
        }
    }

    private void calculateRatio(float top) {
        Log.d(TAG,"calculateRatio top="+top+"》》》collapseOffset="+collapseOffset);
        ratio = (top-collapseOffset) / (topViewHeight - collapseOffset);
        if (dispatchingChildrenContentView) {
            resetDispatchingContentView();
        }

        if (panelListener != null) {
            // Calculate the ratio while dragging.
            panelListener.onSliding(ratio);
            if (ratio > refreshRatio && !isRefreshing) {
                isRefreshing = true;
                panelListener.onRefresh();
            }
        }
    }

    private void updatePanelState(){
        if (contentTop <= getPaddingTop() + collapseOffset) {
            panelState = PanelState.COLLAPSED;
        } else if(contentTop >= topView.getHeight()){
            panelState = PanelState.EXPANDED;
        } else {
            panelState = PanelState.SLIDING;
        }

        if (panelListener != null) {
            panelListener.onPanelStateChanged(panelState);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        Parcelable superState = super.onSaveInstanceState();
        SavedState state = new SavedState(superState);
        state.panelState = panelState.toInt();

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (!(state instanceof SavedState)) {
            // FIX #10
            super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE);
            return;
        }

        SavedState s = (SavedState) state;
        super.onRestoreInstanceState(s.getSuperState());

        this.panelState = PanelState.fromInt(s.panelState);
        if (panelState == PanelState.COLLAPSED) {
            closeTopView(false);
        } else {
            openTopView(false);
        }
    }


    GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if((Math.abs(distanceX) < Math.abs(distanceY))){
                return true;

            }else {
                return false;
            }
        };
    };

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            Log.d(TAG,"tryCaptureView>>"+pointerId+">>>isTopView="+(child == topView));
            if (child == topView && captureTop) {
                dragHelper.captureChildView(dragContentView, pointerId);
                return false;
            }
            return child == dragContentView;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            Log.d(TAG,"onViewPositionChanged>>>");
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            contentTop = top;
            requestLayout();
            calculateRatio(contentTop);
            updatePanelState();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            Log.d(TAG,"getViewVerticalDragRange>>>dragRange="+dragRange);
            return dragRange;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            Log.d(TAG,"clampViewPositionVertical>>>overDrag="+overDrag);
            if (overDrag) {
                // Drag over the top view height.
                return Math.max(top, getPaddingTop() + collapseOffset);
            } else {
                return Math.min(topViewHeight, Math.max(top, getPaddingTop() + collapseOffset));
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.d(TAG,"onViewReleased>>>");
            super.onViewReleased(releasedChild, xvel, yvel);
            // yvel > 0 Fling down || yvel < 0 Fling up
            int top;
            if (yvel > 0 || contentTop > topViewHeight) {
                top = topViewHeight + getPaddingTop();
            } else {
                top = getPaddingTop() + collapseOffset;
            }
            dragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            postInvalidate();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            Log.d(TAG,"onViewDragStateChanged>>>"+state);
            super.onViewDragStateChanged(state);
        }
    };

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }




    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean onTouchEvent = mDetectorCompat.onTouchEvent(ev);
        //将Touch事件传递给ViewDragHelper
        try {
            boolean intercept;
            Log.d(TAG,"onTouchEvent = " +onTouchEvent);
            if((dragHelper.getCapturedView()==null)||(dragHelper.getCapturedView()!=null && dragHelper.getCapturedView() == topView)){
                Log.d(TAG,"111");
                intercept = shouldIntercept && (dragHelper.shouldInterceptTouchEvent(ev) | onTouchEvent);
            }else{
                Log.d(TAG,"222");
                intercept = shouldIntercept && (dragHelper.shouldInterceptTouchEvent(ev));
            }
            Log.d(TAG,"intercept = " +intercept);
            return intercept;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = MotionEventCompat.getActionMasked(event);
        Log.d(TAG,"ratio = " +ratio+">>>dispatchingChildrenContentView="+dispatchingChildrenContentView);
        if (!dispatchingChildrenContentView) {
            try {
                // There seems to be a bug on certain devices: "pointerindex out of range" in viewdraghelper
                // https://github.com/umano/AndroidSlidingUpPanel/issues/351
                dragHelper.processTouchEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (action == MotionEvent.ACTION_MOVE && ratio == 0.0f) {
            Log.d(TAG,"MotionEvent.ACTION_MOVE");
            dispatchingChildrenContentView = true;
            if (!dispatchingChildrenDownFaked) {
                dispatchingChildrenStartedAtY = event.getY();
                event.setAction(MotionEvent.ACTION_DOWN);
                dispatchingChildrenDownFaked = true;
            }
            dragContentView.dispatchTouchEvent(event);
        }

        Log.d(TAG,"dispatchingChildrenStartedAtY="+dispatchingChildrenStartedAtY+">>>event.getY()="+event.getY());
        if (dispatchingChildrenContentView && dispatchingChildrenStartedAtY < event.getY()) {
            resetDispatchingContentView();
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            resetDispatchingContentView();
            dragContentView.dispatchTouchEvent(event);
        }

        return true;
    }

    private void resetDispatchingContentView() {
        dispatchingChildrenDownFaked = false;
        dispatchingChildrenContentView = false;
        dispatchingChildrenStartedAtY = Float.MAX_VALUE;
    }


    //================
    // public
    //================
    
    public PanelState getState() {
        return panelState;
    }
    
    public void openTopView(boolean anim) {
        // Before created
        if (dragContentView.getHeight() == 0) {
            panelState = PanelState.EXPANDED;
            if (panelListener != null) {
                panelListener.onSliding(1.0f);
            }
        } else {
            resetDragContent(anim, topViewHeight);
        }
    }

    public void closeTopView(boolean anim) {
        if (dragContentView.getHeight() == 0) {
            panelState = PanelState.COLLAPSED;
            if (panelListener != null) {
                panelListener.onSliding(0.0f);
            }
        }else{
            resetDragContent(anim, getPaddingTop() + collapseOffset);
        }
    }

    public void updateTopViewHeight(int height){
        ViewGroup.LayoutParams layoutParams = topView.getLayoutParams();
        layoutParams.height = height;
        topView.setLayoutParams(layoutParams);
    }

    public void toggleTopView() {
        toggleTopView(false);
    }

    public void toggleTopView(boolean touchMode) {
        switch (panelState) {
            case COLLAPSED:
                openTopView(true);
                if (touchMode) {
                    setTouchMode(true);
                }
                break;
            case EXPANDED:
                closeTopView(true);
                if (touchMode) {
                    setTouchMode(false);
                }
                break;
        }
    }

    public DragTopLongLayout setTouchMode(boolean shouldIntercept) {
        this.shouldIntercept = shouldIntercept;
        return this;
    }

    /**
     * Setup the drag listener.
     *
     * @return SetupWizard
     */
    public DragTopLongLayout listener(PanelListener panelListener) {
        this.panelListener = panelListener;
        return this;
    }

    /**
     * Set the refresh position while dragging you want.
     * The default value is 1.5f.
     *
     * @return SetupWizard
     */
    public DragTopLongLayout setRefreshRatio(float ratio) {
        this.refreshRatio = ratio;
        return this;
    }

    /**
     * Set enable drag over.
     * The default value is true.
     *
     * @return SetupWizard
     */
    public DragTopLongLayout setOverDrag(boolean overDrag) {
        this.overDrag = overDrag;
        return this;
    }

    /**
     * Set the content view. Pass the id of the view (R.id.xxxxx).
     * This one will be set as the content view and will be dragged together with the topView
     *
     * @param id The id (R.id.xxxxx) of the content view.
     * @return
     */
    public DragTopLongLayout setDragContentViewId(int id) {
        this.dragContentViewId = id;
        return this;
    }

    /**
     * Set the top view. The top view is the header view that will be dragged out.
     * Pass the id of the view (R.id.xxxxx)
     *
     * @param id The id (R.id.xxxxx) of the top view
     * @return
     */
    public DragTopLongLayout setTopViewId(int id) {
        this.topViewId = id;
        return this;
    }

    public boolean isOverDrag() {
        return overDrag;
    }

    /**
     * Get refresh state
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
    }

    /**
     * Complete refresh and reset the refresh state.
     */
    public void onRefreshComplete() {
        isRefreshing = false;
    }

    /**
     * Set the collapse offset
     *
     * @return SetupWizard
     */
    public DragTopLongLayout setCollapseOffset(int px) {
        collapseOffset = px;
        resetContentHeight();
        return this;
    }

    public int getCollapseOffset() {
        return collapseOffset;
    }


    // ---------------------

    public interface PanelListener {
        /**
         * Called while the panel state is changed.
         */
        public void onPanelStateChanged(PanelState panelState);

        /**
         * Called while dragging.
         * ratio >= 0.
         */
        public void onSliding(float ratio);

        /**
         * Called while the ratio over refreshRatio.
         */
        public void onRefresh();
    }

    public static class SimplePanelListener implements PanelListener {

        @Override
        public void onPanelStateChanged(PanelState panelState) {

        }

        @Override
        public void onSliding(float ratio) {

        }

        @Deprecated
        @Override
        public void onRefresh() {

        }
    }

    /**
     * Save the instance state
     */
    private static class SavedState extends BaseSavedState {

        int panelState;

        SavedState(Parcelable superState) {
            super(superState);
        }

    }
}
