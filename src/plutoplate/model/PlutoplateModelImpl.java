package plutoplate.model;

import com.phidgets.PhidgetException;
import com.phidgets.StepperPhidget;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import plutoplate.PlutoplateImages;

public class PlutoplateModelImpl
  implements PlutoplateModel
{
  private PlutoplatePresetDB presetDB;
  private PlutoplateImages imageDB;
  private StepperPhidget stepper;
  private Integer selectedMotor = Integer.valueOf(0);
  private Map<Integer, Integer> lastPosition = new HashMap();
  
  public PlutoplateModelImpl(PlutoplateImages imageDB)
  {
    this.presetDB = new PlutoplatePresetDB();
    this.presetDB.initialize();
    this.imageDB = imageDB;
  }
  
  public void initializeStepper()
    throws PhidgetException
  {
    this.stepper = new StepperPhidget();
    this.stepper.openAny();
    System.out.println("Waiting for the Phidget Stepper to be attached...");
  }
  
  public StepperPhidget getStepper()
  {
    return this.stepper;
  }
  
  public Integer getSelectedMotor()
  {
    return this.selectedMotor;
  }
  
  public void setSelectedMotor(Integer motorId)
  {
    this.selectedMotor = motorId;
  }
  
  public Map<Integer, Integer> getLastPosition()
  {
    return this.lastPosition;
  }
  
  public void close()
    throws PhidgetException
  {
    if (this.stepper != null)
    {
      if (this.stepper.isAttached()) {
        for (int i = 0; i < this.stepper.getMotorCount(); i++) {
          this.stepper.setEngaged(i, false);
        }
      }
      this.stepper.close();
      this.stepper = null;
    }
  }
  
  public PlutoplatePresetDB getPresetDB()
  {
    return this.presetDB;
  }
  
  public PlutoplateImages getImageDB()
  {
    return this.imageDB;
  }
}