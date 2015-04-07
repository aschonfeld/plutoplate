package plutoplate.model;

import com.phidgets.PhidgetException;
import com.phidgets.StepperPhidget;
import java.util.Map;
import plutoplate.PlutoplateImages;

public abstract interface PlutoplateModel
{
  public abstract void initializeStepper()
    throws PhidgetException;
  
  public abstract PlutoplatePresetDB getPresetDB();
  
  public abstract StepperPhidget getStepper();
  
  public abstract Integer getSelectedMotor();
  
  public abstract void setSelectedMotor(Integer paramInteger);
  
  public abstract Map<Integer, Integer> getLastPosition();
  
  public abstract void close()
    throws PhidgetException;
  
  public abstract PlutoplateImages getImageDB();
}