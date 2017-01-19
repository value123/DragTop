package github.chenupt.dragtoplayout.demo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by chengxin on 3/2/15.
 */
public class DensityUtil {
    private static final String TAG = DensityUtil.class.getSimpleName();

    public static float getScreenWidth(Resources resources) {
        final float w = resources.getDisplayMetrics().widthPixels;
        return w;
    }

    public static float getScreenHeight(Resources resources) {
        final float h = resources.getDisplayMetrics().heightPixels;
        return h;
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static int convertDpToPx(Context context, int dp){
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }
}
