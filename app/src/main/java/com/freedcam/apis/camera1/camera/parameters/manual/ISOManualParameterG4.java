package com.freedcam.apis.camera1.camera.parameters.manual;

import com.freedcam.apis.camera1.camera.CameraHolderApi1;
import com.freedcam.apis.basecamera.camera.parameters.AbstractParameterHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class ISOManualParameterG4 extends BaseManualParameter
{
    private CameraHolderApi1 cameraHolderApi1;
    private AE_Handler_LGG4.AeManualEvent manualEvent;

    public ISOManualParameterG4(HashMap<String, String> parameters, CameraHolderApi1 cameraHolder, AbstractParameterHandler camParametersHandler, AE_Handler_LGG4.AeManualEvent manualevent) {
        super(parameters, "", "", "", camParametersHandler,1);

        this.cameraHolderApi1 = cameraHolder;

        this.isSupported = true;
        this.isVisible = isSupported;
        ArrayList<String> s = new ArrayList<>();
        for (int i =0; i <= 2700; i +=50)
        {
            if (i == 0)
                s.add("Auto");
            else
                s.add(i + "");
        }
        stringvalues = new String[s.size()];
        s.toArray(stringvalues);
        this.manualEvent = manualevent;
    }

    @Override
    public boolean IsSupported() {
        return super.IsSupported();
    }

    @Override
    public boolean IsVisible() {
        return super.IsSupported();
    }

    @Override
    public int GetValue() {
        return  currentInt;
    }

    @Override
    protected void setvalue(int valueToSet)
    {
        currentInt = valueToSet;
        if (valueToSet == 0)
        {
            manualEvent.onManualChanged(AE_Handler_LGG4.AeManual.iso, true, valueToSet);
        }
        else
        {
            manualEvent.onManualChanged(AE_Handler_LGG4.AeManual.iso, false,valueToSet);
        }
    }

    public void setValue(int value)
    {

        if (value == 0)
        {
            parameters.put("lg-iso", "auto");
        }
        else
        {
            currentInt = value;
            parameters.put("lg-iso", stringvalues[value]);
        }
        ThrowCurrentValueStringCHanged(stringvalues[value]);
    }

    @Override
    public String GetStringValue() {
        try {
            return stringvalues[currentInt];
        } catch (NullPointerException ex) {
            return "Auto";
        }
    }

    @Override
    public String[] getStringValues() {
        return stringvalues;
    }
}

