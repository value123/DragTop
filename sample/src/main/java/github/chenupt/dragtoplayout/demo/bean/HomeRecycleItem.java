package github.chenupt.dragtoplayout.demo.bean;

import android.content.Intent;

/**
 * Created by shenwenjie on 6/7/2016.
 */
public class HomeRecycleItem {
    int iconId;
    public String title;
    public Intent intent;

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public HomeRecycleItem(int iconId, String title, Intent intent) {
        this.iconId = iconId;
        this.title = title;
        this.intent = intent;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
