package sie.amplifier_conctroller.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.huicheng.R;

public class dsp_setting  extends TabActivity {

    static AnimationTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dsp_setting);
        initTab();
    }

    private void initTab() {

        mTabHost = (AnimationTabHost) getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("Tab1").setIndicator("延时", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(new Intent(this, dsp_setting_delay.class)));
        mTabHost.addTab(mTabHost.newTabSpec("Tab2").setIndicator("设置", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(new Intent(this, dsp_setting_main.class)));
        mTabHost.addTab(mTabHost.newTabSpec("Tab3").setIndicator("通道", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(new Intent(this, dsp_setting_chanel.class)));


        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                if (tabId.equals("Tab1")) {
                    //TODO
                }
            }
        });

        mTabHost.setCurrentTab(1);
    }

}
