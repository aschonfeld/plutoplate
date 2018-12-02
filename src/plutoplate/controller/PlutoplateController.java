package plutoplate.controller;

import com.phidget22.PhidgetException;
import plutoplate.model.PlutoplatePreset;

public abstract interface PlutoplateController {
	public abstract void deletePreset(String paramString);

	public abstract void savePreset(PlutoplatePreset paramPlutoplatePreset);

	public abstract void openPreset(String paramString) throws PhidgetException;

	public abstract void selectMotor(Integer paramInteger);

	public void debug();

	public abstract void resetPosition() throws PhidgetException;

	public abstract void updatePosition(Integer paramInteger) throws PhidgetException;

	public abstract void init() throws PhidgetException;

	public abstract void close() throws PhidgetException;
}