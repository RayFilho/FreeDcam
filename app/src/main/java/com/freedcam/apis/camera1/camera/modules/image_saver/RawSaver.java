package com.freedcam.apis.camera1.camera.modules.image_saver;

import android.content.Context;
import android.support.v4.provider.DocumentFile;

import com.freedcam.apis.camera1.camera.CameraHolderApi1;
import com.freedcam.utils.AppSettingsManager;
import com.freedcam.utils.FileUtils;
import com.freedcam.utils.FreeDPool;
import com.freedcam.utils.Logger;
import com.freedcam.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by troop on 15.04.2015.
 */
public class RawSaver extends JpegSaver
{
    final public String fileEnding = ".bayer";
    public RawSaver(CameraHolderApi1 cameraHolder, I_WorkeDone i_workeDone, Context context, AppSettingsManager appSettingsManager) {
        super(cameraHolder, i_workeDone,context, appSettingsManager);
    }

    private final String TAG = "RawSaver";

    @Override
    public void TakePicture()
    {
        Logger.d(TAG, "Start Take Picture");
        if (ParameterHandler.ZSL != null && ParameterHandler.ZSL.IsSupported() && ParameterHandler.ZSL.GetValue().equals("on"))
        {
            ParameterHandler.ZSL.SetValue("off",true);
        }
        awaitpicture = true;
        FreeDPool.Execute(new Runnable() {
            @Override
            public void run() {
                cameraHolder.TakePicture(null, RawSaver.this);
            }
        });
    }

    @Override
    public void onPictureTaken(final byte[] data)
    {
        super.onPictureTaken(data);
    }

    @Override
    public void saveBytesToFile(byte[] bytes, File fileName, boolean workdone) {
        checkFileExists(fileName);

        Logger.d(TAG, "Start Saving Bytes");
        OutputStream outStream = null;
        try {
            if (!StringUtils.IS_L_OR_BIG()
                    || StringUtils.WRITE_NOT_EX_AND_L_ORBigger(appSettingsManager))
                outStream = new FileOutputStream(fileName);
            else
            {
                DocumentFile df = FileUtils.getFreeDcamDocumentFolder(appSettingsManager,context);
                DocumentFile wr = df.createFile("image/*", fileName.getName());
                outStream = context.getContentResolver().openOutputStream(wr.getUri());
            }
            outStream.write(bytes);
            outStream.flush();
            outStream.close();


        } catch (IOException e) {
            Logger.exception(e);
        }
        Logger.d(TAG, "End Saving Bytes");
        iWorkeDone.OnWorkDone(fileName);
    }
}
