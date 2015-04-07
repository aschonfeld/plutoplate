package plutoplate.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Timer;

import plutoplate.PlutoplateConstants;
import plutoplate.model.PlutoplateModel;
import plutoplate.model.PlutoplatePreset;
import plutoplate.view.PlutoplateView;

import com.phidgets.PhidgetException;
import com.phidgets.StepperPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.StepperPositionChangeEvent;
import com.phidgets.event.StepperPositionChangeListener;

public class PlutoplateControllerImpl
  implements PlutoplateController, ErrorListener, StepperPositionChangeListener, InputChangeListener, AttachListener, DetachListener
{
  private Set<Integer> idsAttached = new HashSet<Integer>();
  private PlutoplateModel model;
  private PlutoplateView view;
  private Boolean resetInProgress = Boolean.valueOf(false);
  private Boolean lastActionReset = Boolean.valueOf(false);
  private Long finalPosition;
  private Timer speedManager = new Timer(PlutoplateConstants.SPEED_CHANGE_INTERVAL, new ActionListener()
  {
    public void actionPerformed(ActionEvent evt)
    {
      try
      {
        double v = PlutoplateControllerImpl.this.model.getStepper().getVelocity(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue());
        double a = PlutoplateControllerImpl.this.model.getStepper().getAcceleration(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue());
        if (v < PlutoplateConstants.FINAL_VELOCITY)
        {
          double vLimit = PlutoplateControllerImpl.this.model.getStepper().getVelocityLimit(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue());
          PlutoplateControllerImpl.this.model.getStepper().setVelocityLimit(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue(), vLimit + PlutoplateConstants.VELOCITY_INCREASE);
        }
        if (a < PlutoplateConstants.FINAL_ACCELERATION) {
          PlutoplateControllerImpl.this.model.getStepper().setVelocityLimit(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue(), a + PlutoplateConstants.ACCELERATION_INCREASE);
        }
      }
      catch (Exception e) {}
    }
  });
  
  public PlutoplateControllerImpl(PlutoplateModel model)
  {
    this.model = model;
    this.view = new PlutoplateView(model, this);
    this.view.createView();
  }
  
  public void deletePreset(String presetName)
  {
    this.model.getPresetDB().deletePreset(presetName);
  }
  
  public void savePreset(PlutoplatePreset preset)
  {
    this.model.getPresetDB().savePreset(preset);
  }
  
  public void openPreset(String presetName)
    throws PhidgetException
  {
    PlutoplatePreset preset = this.model.getPresetDB().getPreset(presetName);
    updatePosition(preset.getPosition());
    this.view.getMotorPosition().setValue(preset.getPosition().intValue());
  }
  
  public void selectMotor(Integer motorId)
  {
    this.model.setSelectedMotor(motorId);
  }
  
  public void resetPosition()
    throws PhidgetException
  {
    if (this.lastActionReset.booleanValue()) {
      return;
    }
    this.resetInProgress = Boolean.valueOf(true);
    this.view.motorStarted();
    this.view.disableControls();
    this.model.getStepper().setEngaged(this.model.getSelectedMotor().intValue(), true);
    this.model.getStepper().setVelocityLimit(this.model.getSelectedMotor().intValue(), PlutoplateConstants.INITIAL_VELOCITY);
    this.model.getStepper().setAcceleration(this.model.getSelectedMotor().intValue(), PlutoplateConstants.INITIAL_ACCELERATION);
    
    long min = this.model.getStepper().getPositionMin(this.model.getSelectedMotor().intValue());
    this.model.getStepper().setTargetPosition(this.model.getSelectedMotor().intValue(), min);
    this.lastActionReset = Boolean.valueOf(true);
    if (!this.speedManager.isRunning()) {
      this.speedManager.start();
    }
  }
  
  public void error(ErrorEvent ex)
  {
    System.out.println("\n--->Error: " + ex.getException());
  }
  
  public void stepperPositionChanged(StepperPositionChangeEvent stepperPositionChangeEvent)
  {
    if (this.resetInProgress.booleanValue()) {
      return;
    }
    try
    {
      long current = new Double(stepperPositionChangeEvent.getValue()).longValue();
      if (current == this.finalPosition.longValue())
      {
        this.view.getActualPosition().setText(this.view.getTargetPosition().getText());
        this.model.getStepper().setVelocityLimit(this.model.getSelectedMotor().intValue(), PlutoplateConstants.INITIAL_VELOCITY);
        this.model.getStepper().setAcceleration(this.model.getSelectedMotor().intValue(), PlutoplateConstants.INITIAL_ACCELERATION);
        this.speedManager.stop();
        this.model.getStepper().setEngaged(this.model.getSelectedMotor().intValue(), false);
        this.view.motorStopped();
        this.finalPosition = null;
      }
    }
    catch (Exception e) {}
  }
  
  public void inputChanged(InputChangeEvent inputChangeEvent)
  {
    try
    {
      if ((this.resetInProgress.booleanValue()) && (inputChangeEvent.getIndex() == 0) && (!inputChangeEvent.getState()))
      {
        this.model.getStepper().setVelocityLimit(this.model.getSelectedMotor().intValue(), 0.0D);
        long currentPos = this.model.getStepper().getCurrentPosition(this.model.getSelectedMotor().intValue());
        this.model.getStepper().setTargetPosition(this.model.getSelectedMotor().intValue(), currentPos);
        this.model.getStepper().setCurrentPosition(this.model.getSelectedMotor().intValue(), currentPos);
        this.model.getStepper().setVelocityLimit(this.model.getSelectedMotor().intValue(), PlutoplateConstants.INITIAL_VELOCITY);
        this.model.getStepper().setAcceleration(this.model.getSelectedMotor().intValue(), PlutoplateConstants.INITIAL_ACCELERATION);
        this.speedManager.stop();
        this.model.getStepper().setEngaged(this.model.getSelectedMotor().intValue(), false);
        this.view.getMotorPosition().setValue(0);
        this.view.getActualPosition().setText("0");
        this.view.getTargetPosition().setText("0");
        this.view.motorStopped();
        this.view.enableControls();
        this.resetInProgress = Boolean.valueOf(false);
      }
    }
    catch (PhidgetException e) {}
  }
  
  public void init()
    throws PhidgetException
  {
    this.model.initializeStepper();
    this.model.getStepper().addErrorListener(this);
    this.model.getStepper().addInputChangeListener(this);
    this.model.getStepper().addStepperPositionChangeListener(this);
    this.model.getStepper().addAttachListener(this);
    this.model.getStepper().addDetachListener(this);
  }
  
  public void close()
    throws PhidgetException
  {
    if (!this.idsAttached.isEmpty())
    {
      this.model.getStepper().removeErrorListener(this);
      this.model.getStepper().removeInputChangeListener(this);
      this.model.getStepper().removeStepperPositionChangeListener(this);
      this.model.getStepper().removeAttachListener(this);
      this.model.close();
    }
  }
  
  public void attached(AttachEvent attachEvent)
  {
    if (!(attachEvent.getSource() instanceof StepperPhidget)) {
      return;
    }
    try
    {
      this.idsAttached.add(Integer.valueOf(attachEvent.getSource().getDeviceID()));
      if (this.model.getLastPosition().isEmpty())
      {
        resetPosition();
        this.model.getLastPosition().put(this.model.getSelectedMotor(), Integer.valueOf(0));
      }
      else
      {
        Integer lastPosition = (Integer)this.model.getLastPosition().get(this.model.getSelectedMotor());
        this.view.getMotorPosition().setValue(lastPosition.intValue());
        this.view.getActualPosition().setText(lastPosition + "");
        this.view.getTargetPosition().setText(lastPosition + "");
        this.view.enableControls();
      }
      this.view.getNoPhidgets().setVisible(false);
    }
    catch (Exception e) {}
  }
  
  public void detached(DetachEvent detachEvent)
  {
    if (!(detachEvent.getSource() instanceof StepperPhidget)) {
      return;
    }
    try
    {
      this.idsAttached.remove(Integer.valueOf(detachEvent.getSource().getDeviceID()));
      this.model.getLastPosition().put(this.model.getSelectedMotor(), Integer.valueOf(this.view.getMotorPosition().getValue()));
      this.view.disableControls();
      this.view.getNoPhidgets().setVisible(true);
    }
    catch (Exception e) {}
  }
  
  public void updatePosition(Integer position)
    throws PhidgetException
  {
    this.view.motorStarted();
    this.lastActionReset = Boolean.valueOf(false);
    Integer currentVal = Integer.valueOf(Integer.parseInt(this.view.getTargetPosition().getText()));
    Integer increasePosBy = Integer.valueOf(position.intValue() - currentVal.intValue());
    
    this.model.getStepper().setEngaged(this.model.getSelectedMotor().intValue(), true);
    Integer trgtPosition = Integer.valueOf(increasePosBy.intValue() * PlutoplateConstants.MOTOR_MOVE_SPEED);
    Long currPosition = Long.valueOf(this.model.getStepper().getCurrentPosition(this.model.getSelectedMotor().intValue()));
    trgtPosition = Integer.valueOf(trgtPosition.intValue() + currPosition.intValue());
    this.model.getStepper().setTargetPosition(this.model.getSelectedMotor().intValue(), trgtPosition.intValue());
    if (!this.speedManager.isRunning()) {
      this.speedManager.start();
    }
    this.view.getTargetPosition().setText(position.toString());
    this.finalPosition = new Long(trgtPosition.intValue());
  }
}