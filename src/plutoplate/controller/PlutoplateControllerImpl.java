package plutoplate.controller;

import com.phidgets.PhidgetException;
import com.phidgets.StepperPhidget;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.event.*;
import plutoplate.PlutoplateConstants;
import plutoplate.model.PlutoplateModel;
import plutoplate.model.PlutoplatePreset;
import plutoplate.view.PlutoplateView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class PlutoplateControllerImpl
        implements PlutoplateController, ErrorListener, StepperPositionChangeListener, InputChangeListener, AttachListener, DetachListener
{
    private Set<Integer> idsAttached = new HashSet<Integer>();
	private Set<Integer> sensorIdsAttached = new HashSet<Integer>();
    private PlutoplateModel model;
    private PlutoplateView view;
    private Boolean resetInProgress = false;
    private Boolean lastActionReset = false;
    private Boolean debug = false;
    private Long finalPosition;
    private Timer speedManager = new Timer(PlutoplateConstants.SPEED_CHANGE_INTERVAL, new ActionListener()
    {
        public void actionPerformed(ActionEvent evt)
        {
            try
            {
                double v = PlutoplateControllerImpl.this.model.getStepper().getVelocity(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue());
                v = Math.abs(v);
                if(PlutoplateControllerImpl.this.debug){
                    System.out.println("Velocity: " + v);
                }

                double a = PlutoplateControllerImpl.this.model.getStepper().getAcceleration(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue());
                if(PlutoplateControllerImpl.this.debug){
                    System.out.println("Acceleration: " + a);
                }

                if (v < PlutoplateConstants.FINAL_VELOCITY)
                {
                    double vLimit = PlutoplateControllerImpl.this.model.getStepper().getVelocityLimit(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue());
                    if(PlutoplateControllerImpl.this.debug){
                        System.out.println("Limit: " + vLimit);
                    }
                    vLimit += PlutoplateConstants.VELOCITY_INCREASE;
                    if(vLimit > PlutoplateConstants.FINAL_VELOCITY){
                        vLimit = PlutoplateConstants.FINAL_VELOCITY;
                    }

                    PlutoplateControllerImpl.this.model.getStepper().setVelocityLimit(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue(), vLimit);

                }
                if (a < PlutoplateConstants.FINAL_ACCELERATION) {
                    a += PlutoplateConstants.ACCELERATION_INCREASE;
                    if(a > PlutoplateConstants.FINAL_ACCELERATION){
                        a = PlutoplateConstants.FINAL_ACCELERATION;
                    }
                    PlutoplateControllerImpl.this.model.getStepper().setAcceleration(PlutoplateControllerImpl.this.model.getSelectedMotor().intValue(), a);


                }
            }
            catch (Exception e) {

                if(PlutoplateControllerImpl.this.debug){
                    e.printStackTrace();
                }
            }
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
        if (this.lastActionReset) {
            return;
        }
        this.resetInProgress = true;
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
        if(this.debug){
            ex.getException().printStackTrace();
        }
    }

    public void stepperPositionChanged(StepperPositionChangeEvent stepperPositionChangeEvent)
    {
        if (this.resetInProgress) {
            return;
        }
        try
        {
            long current = new Double(stepperPositionChangeEvent.getValue()).longValue();
            if (this.finalPosition != null && current == this.finalPosition.longValue())
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
        catch (Exception e) {
            if(this.debug){
                e.printStackTrace();
            }
        }
    }

    public void inputChanged(InputChangeEvent inputChangeEvent)
    {
        try
        {
            if (this.resetInProgress && (inputChangeEvent.getIndex() == 0) && (!inputChangeEvent.getState()))
            {
                this.resetComplete();
            }
        }
        catch (PhidgetException e) {
            if(this.debug){
                e.printStackTrace();
            }
        }
    }

    public void init()
            throws PhidgetException
    {
        this.model.initializeStepper();
		this.model.initializeSensor();
		this.model.getStepper().addErrorListener(this);
		this.model.getStepper().addStepperPositionChangeListener(this);
		this.model.getStepper().addAttachListener(this);
		this.model.getStepper().addDetachListener(this);

		this.model.getSensor().addInputChangeListener(this);
		this.model.getSensor().addErrorListener(this);
		this.model.getSensor().addAttachListener(this);
		this.model.getSensor().addDetachListener(this);
    }

    public void close()
            throws PhidgetException
    {
        if (!this.idsAttached.isEmpty())
		{
			this.model.getStepper().removeErrorListener(this);
			this.model.getStepper().removeStepperPositionChangeListener(this);
			this.model.getStepper().removeAttachListener(this);
			this.model.getStepper().removeDetachListener(this);

			this.model.getSensor().removeInputChangeListener(this);
			this.model.getSensor().removeErrorListener(this);
			this.model.getSensor().removeAttachListener(this);
			this.model.getSensor().removeDetachListener(this);
			this.model.close();
		}
    }

    public void attached(AttachEvent attachEvent)
    {
        try
        {
            if (attachEvent.getSource() instanceof StepperPhidget) {
                if(this.debug){
                    System.out.println("Stepper Id Attached: " + (attachEvent.getSource().getDeviceID()));
                    System.out.println("Stepper Inputs: " + ((StepperPhidget)attachEvent.getSource()).getInputCount());
                }
                this.idsAttached.add(Integer.valueOf(attachEvent.getSource().getDeviceID()));
                if (this.model.getLastPosition().isEmpty())
                {
                    this.resetPosition();
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
            } else if(attachEvent.getSource() instanceof InterfaceKitPhidget){
				if(this.debug){
					System.out.println("Sensor Id Attached: " + (attachEvent.getSource().getDeviceID()));
				}
				this.sensorIdsAttached.add(Integer.valueOf(attachEvent.getSource().getDeviceID()));
			} else {
                System.out.println("Phidget Attached: " + (attachEvent.getSource().getDeviceID()));
            }
        }
        catch (Exception e) {
            if(this.debug){
                e.printStackTrace();
            }
        }
    }

    public void detached(DetachEvent detachEvent)
    {
        try
        {
            if (detachEvent.getSource() instanceof StepperPhidget) {
                if(this.debug){
                    System.out.println("Stepper Id Detached: " + (detachEvent.getSource().getDeviceID()));
                }
                this.idsAttached.remove(Integer.valueOf(detachEvent.getSource().getDeviceID()));
                this.model.getLastPosition().put(this.model.getSelectedMotor(), Integer.valueOf(this.view.getMotorPosition().getValue()));
                this.view.disableControls();
                this.view.getNoPhidgets().setVisible(true);
            } else if (detachEvent.getSource() instanceof InterfaceKitPhidget) {
				if(this.debug){
					System.out.println("Sensor Id Detached: " + (detachEvent.getSource().getDeviceID()));
				}
				this.sensorIdsAttached.remove(Integer.valueOf(detachEvent.getSource().getDeviceID()));
			} else {
                System.out.println("Phidget Detached: " + (detachEvent.getSource().getDeviceID()));
            }
        }
        catch (Exception e) {
            if(this.debug){
                e.printStackTrace();
            }
        }
    }

    public void updatePosition(Integer position)
            throws PhidgetException
    {
        this.view.motorStarted();
        this.lastActionReset = false;
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

    @Override
    public void debug() {
        this.debug = true;
    }

    private void resetComplete() throws PhidgetException{
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
        this.resetInProgress = false;
    }
}