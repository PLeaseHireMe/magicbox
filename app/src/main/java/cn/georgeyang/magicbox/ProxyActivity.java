package cn.georgeyang.magicbox;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.georgeyang.loader.AssetUtils;
import cn.georgeyang.loader.PlugClassLoder;

/**
 * Created by george.yang on 2016-3-29.
 */
public class ProxyActivity extends PluginActivity {
    private String animType=null,action = null,version=null;
    public Fragment fragment;
    private static final List<ProxyActivity> allActivity =new ArrayList<>();

    public static void pushMessage(int type,Object object) {
        for (ProxyActivity proxyActivity:allActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (proxyActivity.isDestroyed()) {
                    continue;
                }
            }
            if (proxyActivity.isFinishing()) {
                continue;
            }


            Fragment fragment = proxyActivity.fragment;
            if (fragment==null || fragment.isDetached() || !fragment.isAdded()) {
                continue;
            }

            try {
                Method method = fragment.getClass().getMethod("onReciveMessage",new Class[]{Integer.class,Object.class});
                method.invoke(fragment,new Object[]{type,object});
            } catch (Exception e) {

            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.Anim_fade);

        try {
           animType = getIntent().getData().getQueryParameter("animType");
        } catch (Exception e) {
            Log.d("demo",Log.getStackTraceString(e));
        }

        if (TextUtils.isEmpty(animType)) {
            animType = "aaa";
        }

        switch (animType) {
            case "ww":

                break;
            default:
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;

        }


        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (!"cn.georgeyang.magicbox.ProxyActivity".equals(this.getClass().getName())) {
            Uri uri = Uri.parse("magicbox://plugin?packageName=cn.georgeyang.magicbox.lib&action=MainFragment&animType=LeftInRightOut");
            intent = new Intent("cn.magicbox.plugin");
            intent.setData(uri);
        }
        if (intent==null || intent.getData()==null) {
            Toast.makeText(this,"缺少参数",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = intent.getData();

        packageName = uri.getQueryParameter("packageName");
        if (TextUtils.isEmpty(packageName)) {
            Toast.makeText(this,"未指定插件名",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        action = uri.getQueryParameter("action");
        if (TextUtils.isEmpty(action)) {
            action = "MainFragment";
        }


        String pluginPath = "";
        try {
            pluginPath = AssetUtils.copyAsset(this,String.format("%s_%s.apk",new Object[]{packageName,version}), getFilesDir());

            FrameLayout rootView = new FrameLayout(this);
            rootView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            rootView.setBackgroundColor(Color.GRAY);
            rootView.setId(android.R.id.content);

            setContentView(rootView);

            initWithApkPathAndPackName(pluginPath,packageName);


            Class pluginActivityClass = mPluginData.classLoder.loadClass(String.format("%s.%s",new Object[]{packageName,action}));
            Constructor<?> localConstructor = pluginActivityClass.getConstructor(new Class[] {});
            fragment = (Fragment) localConstructor.newInstance();

            FragmentTransaction ft =  getFragmentManager().beginTransaction();
            ft.add(android.R.id.content,fragment,"main");
            ft.commit();

        } catch (Exception e) {
            Toast.makeText(this,"加载失败:" + e.getMessage(),Toast.LENGTH_SHORT).show();
            Log.d("demo",Log.getStackTraceString(e).toString());
            e.printStackTrace();
        }
    }


    private Method backPressedMethond;
    /**
     * 虚拟方法,如果fragment有boolean onBackPressed()方法，调用
     */
    @Override
    public void onBackPressed() {
        try {
            if (backPressedMethond==null) {
                backPressedMethond = fragment.getClass().getMethod("onBackPressed",new Class[]{});
            }
            if (backPressedMethond!=null) {
                boolean ret = (boolean) backPressedMethond.invoke(fragment,new Object[]{});
                if (ret) {
                    return;
                }
            }
        } catch (Exception e) {
        }
        super.onBackPressed();
    }

    private Method keyDownMethond;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyDownMethond==null) {
                keyDownMethond = fragment.getClass().getMethod("onKeyDown",new Class[]{Integer.class,KeyEvent.class});
            }
            if (keyDownMethond!=null) {
                boolean ret = (boolean) keyDownMethond.invoke(fragment,new Object[]{keyCode,event});
                if (ret) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return super.onKeyDown(keyCode, event);
    }
}