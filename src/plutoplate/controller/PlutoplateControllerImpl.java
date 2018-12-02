package plutoplate.controller;

import com.phidget22.*;
import plutoplate.PlutoplateConstants;
import plutoplate.model.PlutoplateModel;
import plutoplate.model.PlutoplatePreset;
import plutoplate.view.CustomSliderUI;
import plutoplate.view.PlutoplateView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class PlutoplateControllerImpl implements PlutoplateController, ErrorListener, StepperPositionChangeListener,
		DigitalInputStateChangeListener, AttachListener, DetachListener {
	private Set<Integer> idsAttached = new HashSet<>();
	private Set<Integer> sensorIdsAttached = new HashSet<>();
	private PlutoplateModel model;
	private PlutoplateView view;
	private Boolean resetInProgress = false;
	private Boolean lastActionReset = false;
	private Boolean debug = false;
	private Integer previousSliderPosition = 0;
	private Long finalPosition;
	private Long targetDistance;
	private Timer speedManager = new Timer(PlutoplateConstants.SPEED_CHANGE_INTERVAL, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			try {
				double v = PlutoplateControllerImpl.this.model.getStepper().getVelocity();
				v = Math.abs(v);
				if (PlutoplateControllerImpl.this.debug) {
					System.out.println("Velocity: " + v);
				}

				double a = PlutoplateControllerImpl.this.model.getStepper().getAcceleration();
				if (PlutoplateControllerImpl.this.debug) {
					System.out.println("Acceleration: " + a);
				}

				if (v < PlutoplateConstants.FINAL_VELOCITY) {
					double vLimit = PlutoplateControllerImpl.this.model.getStepper().getVelocityLimit();
					if (PlutoplateControllerImpl.this.debug) {
						System.out.println("Limit: " + vLimit);
					}
					vLimit += PlutoplateConstants.VELOCITY_INCREASE;
					if (vLimit > PlutoplateConstants.FINAL_VELOCITY) {
						vLimit = PlutoplateConstants.FINAL_VELOCITY;
					}

					PlutoplateControllerImpl.this.model.getStepper().setVelocityLimit(vLimit);

				}
				if (a < PlutoplateConstants.FINAL_ACCELERATION) {
					a += PlutoplateConstants.ACCELERATION_INCREASE;
					if (a > PlutoplateConstants.FINAL_ACCELERATION) {
						a = PlutoplateConstants.FINAL_ACCELERATION;
					}
					PlutoplateControllerImpl.this.model.getStepper().setAcceleration(a);

				}
			} catch (Exception e) {

				if (PlutoplateControllerImpl.this.debug) {
					e.printStackTrace();
				}
			}
		}
	});

	public PlutoplateControllerImpl(PlutoplateModel model) {
		this.model = model;
		this.view = new PlutoplateView(model, this);
		this.view.createView();
	}

	public void deletePreset(String presetName) {
		this.model.getPresetDB().deletePreset(presetName);
	}

	public void savePreset(PlutoplatePreset preset) {
		this.model.getPresetDB().savePreset(preset);
	}

	public void openPreset(String presetName) throws PhidgetException {
		PlutoplatePreset preset = this.model.getPresetDB().getPreset(presetName);
		updatePosition(preset.getPosition());
		this.view.getMotorPosition().setValue(preset.getPosition().intValue());
	}

	public void selectMotor(Integer motorId) {
		this.model.setSelectedMotor(motorId);
	}

	public void resetPosition() throws PhidgetException {
		if (this.lastActionReset) {
			return;
		}
		if(!this.model.getSensor().getState()) {
			this.view.getMotorPosition().setValue(0);
			this.view.getActualPosition().setText("0");
			this.view.getTargetPosition().setText("0");
			this.view.motorStopped();
			this.view.enableControls();
			return;
		}
		this.resetInProgress = true;
		this.view.motorStarted();
		this.view.disableControls();
		this.model.getStepper().setEngaged(true);
		this.model.getStepper().setVelocityLimit(PlutoplateConstants.INITIAL_VELOCITY);
		this.model.getStepper().setAcceleration(PlutoplateConstants.INITIAL_ACCELERATION);

		long min = Double.valueOf(this.model.getStepper().getMinPosition()).longValue();
		this.model.getStepper().setTargetPosition(min);
		this.lastActionReset = Boolean.valueOf(true);
		if (!this.speedManager.isRunning()) {
			this.speedManager.start();
		}
	}

	public void onError(ErrorEvent ex) {
		if (this.debug) {
			System.out.println(ex.getDescription());
		}
	}

	public void onPositionChange(StepperPositionChangeEvent stepperPositionChangeEvent) {
		if (this.resetInProgress) {
			return;
		}
		try {
			long current = Double.valueOf(stepperPositionChangeEvent.getPosition()).longValue();
			if (this.finalPosition != null && current == this.finalPosition.longValue()) {
				this.view.getActualPosition().setText(this.view.getTargetPosition().getText());
				this.model.getStepper().setVelocityLimit(PlutoplateConstants.INITIAL_VELOCITY);
				this.model.getStepper().setAcceleration(PlutoplateConstants.INITIAL_ACCELERATION);
				this.speedManager.stop();
				this.model.getStepper().setEngaged(false);
				this.view.motorStopped();
				this.view.enableControls();
				this.finalPosition = null;
			} else {
				if(!this.resetInProgress && this.targetDistance != null) {
					Float pctTraveled = (float) (1.0 - Math.abs((Float.valueOf(current) - Float.valueOf(this.finalPosition)) / Float.valueOf(this.targetDistance)));
					Float sliderTraveled = pctTraveled * (float) (Integer.parseInt(this.view.getTargetPosition().getText()) - this.previousSliderPosition);
					Integer currSlider = Float.valueOf(this.previousSliderPosition + sliderTraveled).intValue();
					this.view.getActualPosition().setText(currSlider.toString());
				}
			}
		} catch (Exception e) {
			if (this.debug) {
				e.printStackTrace();
			}
		}
	}

	public void onStateChange(DigitalInputStateChangeEvent inputChangeEvent) {
		try {
			if (this.resetInProgress && (inputChangeEvent.getSource().getChannel() == 0) && (!inputChangeEvent.getState())) {
				this.resetComplete();
			}
		} catch (PhidgetException e) {
			if (this.debug) {
				e.printStackTrace();
			}
		}
	}

	public void init() throws PhidgetException {
		this.model.initializeStepper();
		this.model.initializeSensor();
		this.model.getStepper().addErrorListener(this);
		this.model.getStepper().addPositionChangeListener(this);
		this.model.getStepper().addAttachListener(this);
		this.model.getStepper().addDetachListener(this);

		this.model.getSensor().addStateChangeListener(this);
		this.model.getSensor().addErrorListener(this);
		this.model.getSensor().addAttachListener(this);
		this.model.getSensor().addDetachListener(this);
		
		this.model.getSensor().setHubPort(1);
		this.model.getSensor().setIsHubPortDevice(true);
		this.model.getSensor().setChannel(0);
		this.model.getSensor().open(5000);
		this.model.getStepper().open(5000);
		
	}

	public void close() throws PhidgetException {
		if (!this.idsAttached.isEmpty()) {
			this.model.getStepper().removeErrorListener(this);
			this.model.getStepper().removePositionChangeListener(this);
			this.model.getStepper().removeAttachListener(this);
			this.model.getStepper().removeDetachListener(this);

			this.model.getSensor().removeStateChangeListener(this);
			this.model.getSensor().removeErrorListener(this);
			this.model.getSensor().removeAttachListener(this);
			this.model.getSensor().removeDetachListener(this);
			this.model.close();
		}
	}

	public void onAttach(AttachEvent attachEvent) {
		try {
			if (attachEvent.getSource() instanceof Stepper) {
				Stepper stepper = (Stepper) attachEvent.getSource();
				if (this.debug) {
					int serialNumber = stepper.getDeviceSerialNumber();
					String channelClass = stepper.getChannelClassName();
					int channel = stepper.getChannel();

					DeviceClass deviceClass = stepper.getDeviceClass();
					if (deviceClass != DeviceClass.VINT) {
						System.out.print("\n\t-> Channel Class: " + channelClass + "\n\t-> Serial Number: "
								+ serialNumber + "\n\t-> Channel:  " + channel + "\n");
					} else {
						int hubPort = stepper.getHubPort();
						System.out.print("\n\t-> Channel Class: " + channelClass + "\n\t-> Serial Number: "
								+ serialNumber + "\n\t-> Hub Port: " + hubPort + "\n\t-> Channel:  " + channel + "\n");
					}
				}
				this.idsAttached.add(Integer.valueOf(stepper.getDeviceID().getCode()));
				if (this.model.getLastPosition().isEmpty()) {
					this.resetPosition();
					this.model.getLastPosition().put(this.model.getSelectedMotor(), Integer.valueOf(0));
				} else {
					Integer lastPosition = (Integer) this.model.getLastPosition().get(this.model.getSelectedMotor());
					this.view.getMotorPosition().setValue(lastPosition.intValue());
					this.view.getActualPosition().setText(lastPosition + "");
					this.view.getTargetPosition().setText(lastPosition + "");
					this.view.enableControls();
				}
				this.view.getNoPhidgets().setVisible(false);
			} else if (attachEvent.getSource() instanceof DigitalInput) {
				DigitalInput ph = (DigitalInput) attachEvent.getSource();
				
				if (this.debug) {
					int serialNumber = ph.getDeviceSerialNumber();
					String channelClass = ph.getChannelClassName();
					int channel = ph.getChannel();
					
					DeviceClass deviceClass = ph.getDeviceClass();
					if (deviceClass != DeviceClass.VINT) {
						System.out.print("\n\t-> Channel Class: " + channelClass + "\n\t-> Serial Number: " + serialNumber +
							  "\n\t-> Channel:  " + channel + "\n");
					} 
					else {            
						int hubPort = ph.getHubPort();
						System.out.print("\n\t-> Channel Class: " + channelClass + "\n\t-> Serial Number: " + serialNumber +
							  "\n\t-> Hub Port: " + hubPort + "\n\t-> Channel:  " + channel + "\n");
					}
				}
				this.sensorIdsAttached.add(Integer.valueOf(ph.getDeviceID().getCode()));
			} else {
				System.out.println("Phidget Attached: " + (attachEvent.getSource().getDeviceID()));
			}
		} catch (Exception e) {
			if (this.debug) {
				e.printStackTrace();
			}
		}
	}

	public void onDetach(DetachEvent detachEvent) {
		try {
			if (detachEvent.getSource() instanceof Stepper) {
				Stepper stepper = (Stepper) detachEvent.getSource();
				if (this.debug) {
					int serialNumber = stepper.getDeviceSerialNumber();
					String channelClass = stepper.getChannelClassName();
					int channel = stepper.getChannel();

					DeviceClass deviceClass = stepper.getDeviceClass();
					if (deviceClass != DeviceClass.VINT) {
						System.out.print("\n\t-> Channel Class: " + channelClass + "\n\t-> Serial Number: "
								+ serialNumber + "\n\t-> Channel:  " + channel + "\n");
					} else {
						int hubPort = stepper.getHubPort();
						System.out.print("\n\t-> Channel Class: " + channelClass + "\n\t-> Serial Number: "
								+ serialNumber + "\n\t-> Hub Port: " + hubPort + "\n\t-> Channel:  " + channel + "\n");
					}
				}
				this.idsAttached.remove(Integer.valueOf(stepper.getDeviceID().getCode()));
				this.model.getLastPosition().put(this.model.getSelectedMotor(),
						Integer.valueOf(this.view.getMotorPosition().getValue()));
				this.view.disableControls();
				this.view.getNoPhidgets().setVisible(true);
			} else if (detachEvent.getSource() instanceof DigitalInput) {
				if (this.debug) {
					System.out.println("Sensor Id Detached: " + (detachEvent.getSource().getDeviceID()));
				}
				this.sensorIdsAttached.remove(Integer.valueOf(detachEvent.getSource().getDeviceID().getCode()));
			} else {
				System.out.println("Phidget Detached: " + (detachEvent.getSource().getDeviceID()));
			}
		} catch (Exception e) {
			if (this.debug) {
				e.printStackTrace();
			}
		}
	}

	public void updatePosition(Integer position) throws PhidgetException {
		this.view.motorStarted();
		this.lastActionReset = false;
		this.previousSliderPosition = Integer.valueOf(Integer.parseInt(this.view.getTargetPosition().getText()));
		Integer increasePosBy = Integer.valueOf(position.intValue() - previousSliderPosition.intValue());

		this.model.getStepper().setEngaged(true);
		Integer trgtDistance = Integer.valueOf(increasePosBy.intValue() * PlutoplateConstants.MOTOR_MOVE_SPEED);
		Integer currPosition = Double.valueOf(this.model.getStepper().getPosition()).intValue();
		Integer trgtPosition = Integer.valueOf(trgtDistance.intValue() + currPosition.intValue());
		this.model.getStepper().setTargetPosition(trgtPosition.intValue());
		if (!this.speedManager.isRunning()) {
			this.speedManager.start();
		}
		this.view.getTargetPosition().setText(position.toString());
		this.targetDistance = Long.valueOf(trgtDistance.intValue());
		this.finalPosition = Long.valueOf(trgtPosition.intValue());
	}

	@Override
	public void debug() {
		this.debug = true;
	}

	private void resetComplete() throws PhidgetException {
		this.model.getStepper().setVelocityLimit(0.0D);
		double currentPos = this.model.getStepper().getPosition();
		this.model.getStepper().setTargetPosition(currentPos);
		this.model.getStepper().setVelocityLimit(PlutoplateConstants.INITIAL_VELOCITY);
		this.model.getStepper().setAcceleration(PlutoplateConstants.INITIAL_ACCELERATION);
		this.speedManager.stop();
		this.model.getStepper().setEngaged(false);
		this.view.getMotorPosition().setValue(0);
		this.view.getMotorPosition().setUI(new CustomSliderUI(this.view.getMotorPosition()));
		this.view.getActualPosition().setText("0");
		this.view.getTargetPosition().setText("0");
		this.view.motorStopped();
		this.view.enableControls();
		this.resetInProgress = false;
	}
}