package com.troop.freedcam.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.lge.real3d.Real3D;
import com.lge.real3d.Real3DInfo;
import com.troop.freedcam.camera.modules.ModuleHandler;
import com.troop.freedcam.camera.parameters.modes.PreviewSizeParameter;
import com.troop.freedcam.i_camera.modules.I_ModuleEvent;
import com.troop.freedcam.i_camera.parameters.AbstractParameterHandler;
import com.troop.freedcam.i_camera.parameters.I_ParametersLoaded;
import com.troop.freedcam.ui.I_PreviewSizeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by troop on 21.08.2014.
 */
public class ExtendedSurfaceView extends SurfaceView implements I_PreviewSizeEvent, I_ParametersLoaded, I_ModuleEvent
{
    boolean hasReal3d = false;
    boolean hasOpenSense = false;
    private static String TAG = "freedcam.ExtendedTextureView";
    Context context;

    public SurfaceHolder mHolder;
    SharedPreferences preferences;

    static int CX;
    static int CY;

    Real3D mReal3D;
    boolean is3D = false;

    public com.troop.freedcam.ui.AppSettingsManager appSettingsManager;
    public AbstractParameterHandler ParametersHandler;
    String currentModule;

    I_PreviewSizeEvent uiPreviewSizeCHangedListner;

    public ExtendedSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public ExtendedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExtendedSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        this.context = context;
        if (Build.VERSION.SDK_INT < 21)
        {

            try {
                isopensense();
                isReald3d();

                preferences = PreferenceManager.getDefaultSharedPreferences(context);
                mHolder = getHolder();
                mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


                if (hasReal3d) {
                    mReal3D = new Real3D(mHolder);
                    mReal3D.setMinimumNegative(-1);
                    SwitchViewMode();
                }
            } catch (NoSuchMethodError noSuchMethodError) {
                Log.d("Not", " 3D Device");
            }
        }
        else
        {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            mHolder = getHolder();
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(960, 720);
            this.setLayoutParams(params);
        }
    }

    private void isReald3d()
    {
        try {
            Class c = Class.forName("com.lge.real3d.Real3D");
            final String LGE_3D_DISPLAY = "lge.hardware.real3d.barrier.landscape";
            if(context.getPackageManager().hasSystemFeature(LGE_3D_DISPLAY))
                hasReal3d = true;
        } catch (ClassNotFoundException e) {
            hasReal3d = false;
        }

    }

    private void isopensense()
    {
        try {
            Class c = Class.forName("com.htc.view.DisplaySetting");
            Log.d(TAG, "Found class com.htc.view.DisplaySetting");
            hasOpenSense = true;

        } catch (ClassNotFoundException e) {

            hasOpenSense = false;
            Log.d(TAG, "didnt find class com.htc.view.DisplaySetting, NO 3D!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (uiPreviewSizeCHangedListner != null)
            uiPreviewSizeCHangedListner.OnPreviewSizeChanged(left,right);
    }

    public void SetOnPreviewSizeCHangedListner(I_PreviewSizeEvent previewSizeEventListner)
    {
        this.uiPreviewSizeCHangedListner = previewSizeEventListner;
    }

    public  void SwitchViewMode()
    {
        if (hasReal3d)
        {
            if (preferences.getInt(com.troop.freedcam.ui.AppSettingsManager.SETTING_CURRENTCAMERA, 0) == 2)
            {
                is3D = true;
                mReal3D.setReal3DInfo(new Real3DInfo(true, Real3D.REAL3D_TYPE_SS, Real3D.REAL3D_ORDER_LR));
                Log.d(TAG, "Set 3d");
            }
            else
            {
                is3D = false;
                mReal3D.setReal3DInfo(new Real3DInfo(true, Real3D.REAL3D_TYPE_NONE, 0));
                Log.d(TAG, "Set 2d");
            }
        }
    }

    private class Size
    {
        public int width;
        public int height;
        public Size(int w, int h)
        {
            this.height = h;
            this.width = w;
        }
        public Size(String s)
        {
            String[] split = s.split("x");
            this.height = Integer.parseInt(split[1]);
            this.width = Integer.parseInt(split[0]);;
        }

    }

    @Override
    public void OnPreviewSizeChanged(int w, int h)
    {
        Log.d(TAG, "#########PREVIEW SIZE CHANGED##########");
        if (currentModule.equals(""))
            currentModule = appSettingsManager.GetCurrentModule();
        if (hasReal3d && is3D)
        {
            ParametersHandler.PreviewSize.SetValue(800 + "x" + 480, true);
            setPreviewToDisplay(800, 480);
        }
        else if (currentModule.equals(ModuleHandler.MODULE_PICTURE))
        {
                PreviewSizeParameter previewSizeParameter = (PreviewSizeParameter) ParametersHandler.PreviewSize;
                List<Size> sizes = new ArrayList<Size>();
                String[] stringsSizes = previewSizeParameter.GetValues();
                for (String s : stringsSizes) {
                    sizes.add(new Size(s));
                }
                Size size = getOptimalPreviewSize(sizes, w, h);
                ParametersHandler.PreviewSize.SetValue(size.width + "x" + size.height, true);
                setPreviewToDisplay(size.width, size.height);
        }
        else
        {
            ParametersHandler.PreviewSize.SetValue(w+"x"+h, true);
            setPreviewToDisplay(w, h);
        }
        //[1.00 = square] [1.25 = 5:4] [1.33 = 4:3] [1.50 = 3:2] [1.60 = 16:10] [1.67 = 5:3] [1.71 = 128:75] [1.78 = 16:9] [1.85] [2.33 = 21:9 (1792x768)] [2.35 = Cinamascope] [2.37 = "21:9" (2560x1080)] [2.39 = Panavision]

    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void ParametersLoaded()
    {
        /*String previewsize = "";
        if (appSettingsManager.GetCurrentModule().equals(ModuleHandler.MODULE_PICTURE)
                || appSettingsManager.GetCurrentModule().equals(ModuleHandler.MODULE_HDR) )
            previewsize = ParametersHandler.PictureSize.GetValue();
        if (appSettingsManager.GetCurrentModule().equals(ModuleHandler.MODULE_LONGEXPO))
            previewsize = ParametersHandler.PreviewSize.GetValue();
        setPreviewSize(previewsize);*/
    }

    public void setPreviewSize(String previewsize)
    {
        //TODO crash on s5
        if (previewsize  == null || previewsize.equals("")) {
            previewsize = "640x480";
            Log.d(TAG, "set preview size was null!!!! fix to 640x480");
        }
        String[] split = previewsize.split("x");
        int w = Integer.parseInt(split[0]);
        int h = Integer.parseInt(split[1]);
        OnPreviewSizeChanged(w, h);
    }

    private double getRatio(int w, int h)
    {
        double newratio = (double)w/(double)h;
        newratio = Math.round(newratio*100.0)/100.0;
        return newratio;
    }

    private void setPreviewToDisplay(int w, int h)
    {
        CX = w;
        CY = h;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            int ht = w;
            int wt = h;
            w = wt;
            h = ht;
        }


        double newratio = getRatio(w, h);
        Log.d(TAG, "preview Ratio : " + newratio + " w:"+ w+ " h:" +h);
        int width = 0;
        int height = 0;

        if (Build.VERSION.SDK_INT >= 17)
        {
            WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Point size =  new Point();
            wm.getDefaultDisplay().getRealSize(size);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                width = size.x;
                height = size.y;
            }
            else
            {
                height = size.x;
                width = size.y;
            }
        }
        else
        {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                width = metrics.widthPixels;
                height = metrics.heightPixels;
            }
            else
            {
                width = metrics.heightPixels;
                height = metrics.widthPixels;
            }

        }
        double displayratio = getRatio(width, height);
        Log.d(TAG, "display Ratio : " + displayratio + " w:"+ width+ " h:" +height);
        if (newratio == displayratio)
        {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                layoutParams.topMargin = 0;
                layoutParams.bottomMargin = 0;
            }
            else {
                layoutParams.rightMargin = 0;
                layoutParams.leftMargin = 0;
            }
            this.setLayoutParams(layoutParams);
        }
        else if (newratio == 1.33)
        {
            int tmo = (int)((double)width / displayratio * newratio);
            int newwidthdiff = width - tmo;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                layoutParams.topMargin = newwidthdiff / 2;
                layoutParams.bottomMargin = newwidthdiff / 2;
            }
            else {
                layoutParams.rightMargin = newwidthdiff / 2;
                layoutParams.leftMargin = newwidthdiff / 2;
            }
            this.setLayoutParams(layoutParams);

        }
        else
        {
            int tmo = (int)((double)width / displayratio * newratio);
            int newwidthdiff = width - tmo;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                layoutParams.topMargin = newwidthdiff / 2;
                layoutParams.bottomMargin = newwidthdiff / 2;
            }
            else {
                layoutParams.rightMargin = newwidthdiff/2;
                layoutParams.leftMargin = newwidthdiff /2;
            }

            this.setLayoutParams(layoutParams);
        }
    }

    @Override
    public String ModuleChanged(String module)
    {
        if (ParametersHandler.PictureSize == null && ParametersHandler.PreviewSize == null)
            return null;
        this.currentModule = module;
        if(module.equals(ModuleHandler.MODULE_PICTURE) || module.equals(ModuleHandler.MODULE_HDR))
        {
            if (ParametersHandler.PictureSize == null)
                return null;
            setPreviewSize(ParametersHandler.PictureSize.GetValue());
        }
        if (module.equals(ModuleHandler.MODULE_LONGEXPO) || module.equals(ModuleHandler.MODULE_VIDEO))
            setPreviewSize(ParametersHandler.PreviewSize.GetValue());
        return null;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


}
