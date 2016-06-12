/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package com.freedcam.apis.camera2.modules;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Allocation.OnBufferAvailableListener;
import android.renderscript.Element;
import android.renderscript.Type.Builder;
import android.view.Surface;

import com.freedcam.apis.KEYS;
import com.freedcam.apis.basecamera.Size;
import com.freedcam.apis.basecamera.interfaces.I_CameraUiWrapper;
import com.freedcam.apis.basecamera.modules.AbstractModuleHandler.CaptureStates;
import com.freedcam.apis.basecamera.modules.ModuleEventHandler;
import com.freedcam.apis.camera1.parameters.modes.StackModeParameter;
import com.freedcam.ui.handler.MediaScannerManager;
import com.freedcam.utils.FreeDPool;
import com.freedcam.utils.Logger;
import com.freedcam.utils.RenderScriptHandler;
import com.freedcam.utils.StringUtils;
import com.imageconverter.ScriptField_MinMaxPixel;

import java.io.File;

/**
 * Created by troop on 16.05.2016.
 */
@TargetApi(VERSION_CODES.LOLLIPOP)
public class StackingModuleApi2 extends AbstractModuleApi2
{
    private final String TAG = StackingModuleApi2.class.getSimpleName();
    private Size previewSize;
    private Surface previewsurface;
    private Surface camerasurface;
    private int mHeight;
    private int mWidth;
    private ScriptField_MinMaxPixel medianMinMax;
    private ProcessingTask mProcessingTask;
    private HandlerThread mProcessingThread;
    private Handler mProcessingHandler;
    private boolean keepstacking = false;
    private boolean afterFilesave = false;
    private RenderScriptHandler renderScriptHandler;


    public StackingModuleApi2(Context context, I_CameraUiWrapper cameraUiWrapper, RenderScriptHandler renderScriptHandler) {
        super(context, cameraUiWrapper);
        name = KEYS.MODULE_STACKING;
        this.renderScriptHandler =renderScriptHandler;
    }

    @Override
    public String ShortName() {
        return "Stack";
    }

    @Override
    public String LongName() {
        return "Stacking";
    }

    @Override
    public void startPreview()
    {
        if (afterFilesave)
        {
            afterFilesave =false;
            cameraHolder.CaptureSessionH.StartRepeatingCaptureSession();
        }
        else
        {
            previewSize = new Size(ParameterHandler.PictureSize.GetValue());
            mHeight = previewSize.height;
            mWidth = previewSize.width;
            Builder yuvTypeBuilder = new Builder(renderScriptHandler.GetRS(), Element.YUV(renderScriptHandler.GetRS()));
            yuvTypeBuilder.setX(mWidth);
            yuvTypeBuilder.setY(mHeight);
            yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888);
            Builder rgbTypeBuilder = new Builder(renderScriptHandler.GetRS(), Element.RGBA_8888(renderScriptHandler.GetRS()));
            rgbTypeBuilder.setX(mWidth);
            rgbTypeBuilder.setY(mHeight);
            renderScriptHandler.SetAllocsTypeBuilder(yuvTypeBuilder,rgbTypeBuilder,Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT,  Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);
            medianMinMax = new ScriptField_MinMaxPixel(renderScriptHandler.GetRS(), mWidth * mHeight);

            cameraHolder.CaptureSessionH.SetTextureViewSize(mWidth, mHeight, 0, 180, false);
            SurfaceTexture texture = cameraHolder.CaptureSessionH.getSurfaceTexture();

            texture.setDefaultBufferSize(previewSize.width, previewSize.height);
            previewsurface = new Surface(texture);
            renderScriptHandler.SetSurfaceToOutputAllocation(previewsurface);
            camerasurface = renderScriptHandler.GetInputAllocationSurface();
            cameraHolder.CaptureSessionH.AddSurface(camerasurface, true);

            renderScriptHandler.imagestack.set_yuvinput(true);
            renderScriptHandler.imagestack.set_gCurrentFrame(renderScriptHandler.GetIn());
            renderScriptHandler.imagestack.set_gLastFrame(renderScriptHandler.GetOut());
            renderScriptHandler.imagestack.bind_medianMinMaxPixel(medianMinMax);
            renderScriptHandler.yuvToRgbIntrinsic.setInput(renderScriptHandler.GetIn());
            cameraHolder.CaptureSessionH.CreateCaptureSession();

            if (mProcessingTask != null) {

                while (mProcessingTask.working) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Logger.exception(e);
                    }
                }
                mProcessingTask = null;
            }
            mProcessingTask = new ProcessingTask(renderScriptHandler.GetIn());
        }
    }

    @Override
    public void stopPreview()
    {
        cameraHolder.CaptureSessionH.StopRepeatingCaptureSession();
    }

    @Override
    public boolean DoWork()
    {
        if (!keepstacking && !isWorking) {
            changeCaptureState(CaptureStates.continouse_capture_start);
            keepstacking = true;
            return true;
        }
        else
        {
            changeCaptureState(CaptureStates.cont_capture_stop_while_working);
            stopPreview();
            saveImageToFile();
            afterFilesave = true;
            startPreview();
            return false;
        }
    }

    private void saveImageToFile() {
        final Bitmap outputBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        renderScriptHandler.GetOut().copyTo(outputBitmap);
        FreeDPool.Execute(new Runnable() {
            @Override
            public void run() {
                File stackedImg = new File(StringUtils.getFilePath(appSettingsManager.GetWriteExternal(), "_stack.jpg"));
                SaveBitmapToFile(outputBitmap,stackedImg);
                changeCaptureState(CaptureStates.continouse_capture_stop);
                MediaScannerManager.ScanMedia(context, stackedImg);
                cameraUiWrapper.GetModuleHandler().WorkFinished(stackedImg);
                isWorking = false;
            }
        });

        keepstacking =false;
    }

    @Override
    public void InitModule() {
        super.InitModule();
        mProcessingThread = new HandlerThread("StackingModuleApi2");
        mProcessingThread.start();
        mProcessingHandler = new Handler(mProcessingThread.getLooper());
        startPreview();
    }

    @Override
    public void DestroyModule()
    {
        Logger.d(TAG, "DestroyModule");
        cameraHolder.CaptureSessionH.CloseCaptureSession();
        if (mProcessingTask != null) {

            while (mProcessingTask.working)
            {
                keepstacking = false;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Logger.exception(e);
                }
            }
            mProcessingTask = null;
        }
        if (renderScriptHandler.GetIn() != null) {
            renderScriptHandler.GetIn().setOnBufferAvailableListener(null);
        }
        if (renderScriptHandler.GetOut() != null)
        {
            renderScriptHandler.GetOut().setSurface(null);
            //mOutputAllocation = null;
        }

    }

    /**
     * Class to process buffer from camera and output to buffer to screen
     */
    @TargetApi(VERSION_CODES.KITKAT)
    class ProcessingTask implements Runnable, OnBufferAvailableListener {
        private int mPendingFrames = 0;
        private Allocation mInputAllocation;
        private boolean working = false;
        public ProcessingTask(Allocation input) {
            mInputAllocation = input;
            mInputAllocation.setOnBufferAvailableListener(this);
        }
        @Override
        public void onBufferAvailable(Allocation a) {
            synchronized (this) {
                mPendingFrames++;
                mProcessingHandler.post(this);
            }
        }
        @Override
        public void run()
        {

            // Find out how many frames have arrived
            int pendingFrames;
            synchronized (this) {
                pendingFrames = mPendingFrames;
                mPendingFrames = 0;
                // Discard extra messages in case processing is slower than frame rate
                mProcessingHandler.removeCallbacks(this);
            }
            // Get to newest input
            for (int i = 0; i < pendingFrames; i++)
            {
                mInputAllocation.ioReceive();
            }
            if (renderScriptHandler.GetOut() == null)
                return;
            if (keepstacking)
            {

                // Run processing pass
                if (ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.AVARAGE))
                    renderScriptHandler.imagestack.forEach_stackimage_avarage(renderScriptHandler.GetOut());
                else if (ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.AVARAGE1x2))
                    renderScriptHandler.imagestack.forEach_stackimage_avarage1x2(renderScriptHandler.GetOut());
                else if (ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.AVARAGE1x3))
                    renderScriptHandler.imagestack.forEach_stackimage_avarage1x3(renderScriptHandler.GetOut());
                else if (ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.AVARAGE3x3))
                    renderScriptHandler.imagestack.forEach_stackimage_avarage3x3(renderScriptHandler.GetOut());
                else if(ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.LIGHTEN))
                    renderScriptHandler.imagestack.forEach_stackimage_lighten(renderScriptHandler.GetOut());
                else if(ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.LIGHTEN_V))
                    renderScriptHandler.imagestack.forEach_stackimage_lightenV(renderScriptHandler.GetOut());
                else if (ParameterHandler.imageStackMode.GetValue().equals(StackModeParameter.MEDIAN))
                {
                    renderScriptHandler.imagestack.forEach_stackimage_median(renderScriptHandler.GetOut());
                }
            }
            else
            {
                renderScriptHandler.yuvToRgbIntrinsic.forEach(renderScriptHandler.GetOut());
            }
            renderScriptHandler.GetOut().ioSend();
        }
    }


}