package com.freedcam.ui.themesample.subfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.freedcam.apis.basecamera.camera.AbstractCameraUiWrapper;
import com.freedcam.apis.basecamera.camera.parameters.manual.AbstractManualParameter;
import com.freedcam.apis.basecamera.camera.parameters.I_ParametersLoaded;
import com.freedcam.apis.sonyremote.camera.CameraUiWrapperSony;
import com.freedcam.ui.AbstractFragment;
import com.freedcam.utils.AppSettingsManager;
import com.freedcam.ui.I_Activity;
import com.freedcam.ui.themesample.views.ManualButton;
import com.freedcam.ui.views.RotatingSeekbar;
import com.freedcam.utils.Logger;
import com.troop.freedcam.R;

/**
 * Created by troop on 08.12.2015.
 */
public class ManualFragmentRotatingSeekbar extends AbstractFragment implements I_ParametersLoaded, SeekBar.OnSeekBarChangeListener, AbstractManualParameter.I_ManualParameterEvent
{
    private int currentValuePos = 0;

    private RotatingSeekbar seekbar;
    private ManualButton mf;
    private ManualButton iso;
    private ManualButton shutter;
    private ManualButton aperture;
    private ManualButton exposure;
    private ManualButton brightness;
    private ManualButton burst;
    private ManualButton wb;
    private ManualButton contrast;
    private ManualButton saturation;
    private ManualButton sharpness;
    private ManualButton programshift;
    private ManualButton zoom;
    private ManualButton skintone;
    private ManualButton fx;
    private ManualButton convergence;

    private ManualButton currentButton;

    private ManualButton previewZoom;

    private final String TAG = ManualFragmentRotatingSeekbar.class.getSimpleName();
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.manual_fragment_rotatingseekbar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        seekbar = (RotatingSeekbar)view.findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setVisibility(View.GONE);

        mf = (ManualButton)view.findViewById(R.id.manual_mf);
        mf.SetStuff(AppSettingsManager.MF,appSettingsManager);
        mf.setOnClickListener(manualButtonClickListner);

        iso = (ManualButton)view.findViewById(R.id.manual_iso);
        iso.SetStuff( AppSettingsManager.MISO,appSettingsManager);
        iso.setOnClickListener(manualButtonClickListner);

        shutter = (ManualButton)view.findViewById(R.id.manual_shutter);
        shutter.SetStuff(AppSettingsManager.MSHUTTERSPEED,appSettingsManager);
        shutter.setOnClickListener(manualButtonClickListner);

        aperture = (ManualButton)view.findViewById(R.id.manual_aperture);
        aperture.SetStuff("",appSettingsManager);
        aperture.setOnClickListener(manualButtonClickListner);

        exposure = (ManualButton)view.findViewById(R.id.manual_exposure);
        exposure.SetStuff(AppSettingsManager.MEXPOSURE,appSettingsManager);
        exposure.setOnClickListener(manualButtonClickListner);

        brightness = (ManualButton)view.findViewById(R.id.manual_brightness);
        brightness.SetStuff(AppSettingsManager.MBRIGHTNESS,appSettingsManager);
        brightness.setOnClickListener(manualButtonClickListner);

        burst = (ManualButton)view.findViewById(R.id.manual_burst);
        burst.SetStuff("",appSettingsManager);
        burst.setOnClickListener(manualButtonClickListner);

        wb = (ManualButton)view.findViewById(R.id.manual_wb);
        wb.SetStuff(AppSettingsManager.MCCT,appSettingsManager);
        wb.setOnClickListener(manualButtonClickListner);

        contrast = (ManualButton)view.findViewById(R.id.manual_contrast);
        contrast.SetStuff(AppSettingsManager.MCONTRAST,appSettingsManager);
        contrast.setOnClickListener(manualButtonClickListner);

        saturation = (ManualButton)view.findViewById(R.id.manual_saturation);
        saturation.SetStuff(AppSettingsManager.MSATURATION,appSettingsManager);
        saturation.setOnClickListener(manualButtonClickListner);

        sharpness = (ManualButton)view.findViewById(R.id.manual_sharpness);
        sharpness.SetStuff(AppSettingsManager.MSHARPNESS,appSettingsManager);
        sharpness.setOnClickListener(manualButtonClickListner);

        programshift = (ManualButton)view.findViewById(R.id.manual_program_shift);
        programshift.SetStuff("",appSettingsManager);
        programshift.setOnClickListener(manualButtonClickListner);

        zoom = (ManualButton)view.findViewById(R.id.manual_zoom);
        zoom.SetStuff("",appSettingsManager);
        zoom.setOnClickListener(manualButtonClickListner);

        skintone = (ManualButton)view.findViewById(R.id.manual_skintone);
        skintone.SetStuff("",appSettingsManager);
        skintone.setOnClickListener(manualButtonClickListner);

        fx  = (ManualButton)view.findViewById(R.id.manual_fx);
        fx.SetStuff("",appSettingsManager);
        fx.setOnClickListener(manualButtonClickListner);

        convergence = (ManualButton)view.findViewById(R.id.manual_convergence);
        convergence.SetStuff(AppSettingsManager.MCONVERGENCE,appSettingsManager);
        convergence.setOnClickListener(manualButtonClickListner);

        previewZoom = (ManualButton)view.findViewById(R.id.manual_zoom_preview);
        previewZoom.setOnClickListener(manualButtonClickListner);
        if (wrapper != null)
            setWrapper();
    }



    @Override
    public void SetCameraUIWrapper(AbstractCameraUiWrapper wrapper)
    {
        super.SetCameraUIWrapper(wrapper);
        //at this point a nullpointer could happen because the fragemnt is possible not added to the activity
        //but if its added notify it about the change
        try {
            wrapper.camParametersHandler.AddParametersLoadedListner(this);
            setWrapper();
        }
        catch (NullPointerException ex)
        {
            Logger.exception(ex);
        }


    }

    @Override
    public void SetStuff( I_Activity i_activity,AppSettingsManager appSettingsManager) {
        super.SetStuff(i_activity,appSettingsManager);
    }

    private void setWrapper() {
        contrast.SetAbstractManualParameter(wrapper.camParametersHandler.ManualContrast);
        burst.SetAbstractManualParameter(wrapper.camParametersHandler.Burst);
        brightness.SetAbstractManualParameter(wrapper.camParametersHandler.ManualBrightness);
        wb.SetAbstractManualParameter(wrapper.camParametersHandler.CCT);
        convergence.SetAbstractManualParameter(wrapper.camParametersHandler.ManualConvergence);
        exposure.SetAbstractManualParameter(wrapper.camParametersHandler.ManualExposure);
        fx.SetAbstractManualParameter(wrapper.camParametersHandler.FX);
        mf.SetAbstractManualParameter(wrapper.camParametersHandler.ManualFocus);
        saturation.SetAbstractManualParameter(wrapper.camParametersHandler.ManualSaturation);
        sharpness.SetAbstractManualParameter(wrapper.camParametersHandler.ManualSharpness);
        shutter.SetAbstractManualParameter(wrapper.camParametersHandler.ManualShutter);
        iso.SetAbstractManualParameter(wrapper.camParametersHandler.ISOManual);
        zoom.SetAbstractManualParameter(wrapper.camParametersHandler.Zoom);
        aperture.SetAbstractManualParameter(wrapper.camParametersHandler.ManualFNumber);
        skintone.SetAbstractManualParameter(wrapper.camParametersHandler.Skintone);
        programshift.SetAbstractManualParameter(wrapper.camParametersHandler.ProgramShift);
        previewZoom.SetAbstractManualParameter(wrapper.camParametersHandler.PreviewZoom);
    }

    @Override
    public void ParametersLoaded() {
        if (wrapper != null)
            setWrapper();
    }

    //######## ManualButton Stuff#####
    private View.OnClickListener manualButtonClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (currentButton != null)
                currentButton.RemoveParameterListner(ManualFragmentRotatingSeekbar.this);
            //when same button gets clicked second time
            if(v == currentButton && seekbar.getVisibility() == View.VISIBLE)
            {
                //hideseekbar and set color back from button
                seekbar.setVisibility(View.GONE);
                currentButton.SetActive(false);
            }
            //if no button was active or a different was clicked
            else
            {
                if (seekbar.getVisibility() == View.GONE)
                    seekbar.setVisibility(View.VISIBLE);
                //when already a button is active disable it
                if (currentButton != null)
                    currentButton.SetActive(false);
                //set the returned view as active and fill seekbar
                currentButton = (ManualButton) v;
                currentButton.SetActive(true);
                currentButton.SetParameterListner(ManualFragmentRotatingSeekbar.this);
                String[]vals = currentButton.getStringValues();
                if (vals == null || vals.length == 0) {
                    currentButton.SetActive(false);
                    seekbar.setVisibility(View.GONE);
                    Logger.e(TAG, "Values returned from currentButton are NULL!");
                    return;
                }
                seekbar.SetStringValues(vals);
                seekbar.setProgress(currentButton.getCurrentItem(),false);
                currentValuePos = currentButton.getCurrentItem();
                Logger.d(TAG, "CurrentvaluePos " + currentValuePos);
            }

        }
    };

    //#########################SEEKBAR STUFF#############################


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        Logger.d(TAG, "onProgressChanged:" + progress);
        currentValuePos = progress;
        if (!(wrapper instanceof CameraUiWrapperSony)) {
            currentButton.setValueToParameters(progress);
            currentButton.onCurrentValueChanged(progress);

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (wrapper instanceof CameraUiWrapperSony) {
            currentButton.setValueToParameters(currentValuePos);
            currentButton.onCurrentValueChanged(currentValuePos);
        }
    }

    @Override
    public void onIsSupportedChanged(boolean value)
    {
        if (!value) {
            seekbar.setVisibility(View.GONE);
            currentButton.SetActive(false);
        }
    }

    @Override
    public void onIsSetSupportedChanged(boolean value)
    {
        if (value)
            seekbar.setVisibility(View.VISIBLE);
        else
            seekbar.setVisibility(View.GONE);
    }


    @Override
    public void onCurrentValueChanged(int current) {

    }

    @Override
    public void onValuesChanged(String[] values) {

    }

    @Override
    public void onCurrentStringValueChanged(String value) {

    }
}
