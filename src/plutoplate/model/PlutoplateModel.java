package plutoplate.model;

import com.phidget22.*;
import plutoplate.PlutoplateImages;

import java.util.Map;

public abstract interface PlutoplateModel {
	public abstract void initializeStepper() throws PhidgetException;

	public abstract void initializeSensor() throws PhidgetException;

	public abstract PlutoplatePresetDB getPresetDB();

	public abstract Stepper getStepper();

	public abstract DigitalInput getSensor();

	public abstract Integer getSelectedMotor();

	public abstract void setSelectedMotor(Integer paramInteger);

	public abstract Integer getSelectedSensor();

	public abstract void setSelectedSensor(Integer paramInteger);

	public abstract Map<Integer, Integer> getLastPosition();

	public abstract void close() throws PhidgetException;

	public abstract PlutoplateImages getImageDB();
}