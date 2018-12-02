package plutoplate.model;

import com.phidget22.*;
import plutoplate.PlutoplateImages;

import java.util.HashMap;
import java.util.Map;

public class PlutoplateModelImpl implements PlutoplateModel {
	private PlutoplatePresetDB presetDB;
	private PlutoplateImages imageDB;
	private Stepper stepper;
	private DigitalInput sensor;
	private Integer selectedMotor = Integer.valueOf(0);
	private Integer selectedSensor = Integer.valueOf(0);
	private Map<Integer, Integer> lastPosition = new HashMap<>();

	public PlutoplateModelImpl(PlutoplateImages imageDB) {
		this.presetDB = new PlutoplatePresetDB();
		this.presetDB.initialize();
		this.imageDB = imageDB;
	}

	public void initializeStepper() throws PhidgetException {
		this.stepper = new Stepper();
	}

	public void initializeSensor() throws PhidgetException {
		this.sensor = new DigitalInput();
	}

	public Stepper getStepper() {
		return this.stepper;
	}

	public DigitalInput getSensor() {
		return this.sensor;
	}

	public Integer getSelectedMotor() {
		return this.selectedMotor;
	}

	public void setSelectedMotor(Integer motorId) {
		this.selectedMotor = motorId;
	}

	public Integer getSelectedSensor() {
		return this.selectedSensor;
	}

	public void setSelectedSensor(Integer sensorId) {
		this.selectedSensor = sensorId;
	}

	public Map<Integer, Integer> getLastPosition() {
		return this.lastPosition;
	}

	public void close() throws PhidgetException {
		if (this.stepper != null) {
			if (this.stepper.getEngaged()) {
				this.stepper.setEngaged(false);
			}
			this.stepper.close();
			this.stepper = null;
		}
		if (this.sensor != null) {
			this.sensor.close();
			this.sensor = null;
		}
	}

	public PlutoplatePresetDB getPresetDB() {
		return this.presetDB;
	}

	public PlutoplateImages getImageDB() {
		return this.imageDB;
	}
}