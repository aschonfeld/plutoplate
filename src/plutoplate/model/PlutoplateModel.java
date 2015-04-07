package plutoplate.model;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.StepperPhidget;
import plutoplate.PlutoplateImages;

import java.util.Map;

public abstract interface PlutoplateModel
{
    public abstract void initializeStepper()
            throws PhidgetException;

    public abstract void initializeSensor()
            throws PhidgetException;

    public abstract PlutoplatePresetDB getPresetDB();

    public abstract StepperPhidget getStepper();

    public abstract InterfaceKitPhidget getSensor();

    public abstract Integer getSelectedMotor();

    public abstract void setSelectedMotor(Integer paramInteger);

    public abstract Integer getSelectedSensor();

    public abstract void setSelectedSensor(Integer paramInteger);

    public abstract Map<Integer, Integer> getLastPosition();

    public abstract void close()
            throws PhidgetException;

    public abstract PlutoplateImages getImageDB();
}