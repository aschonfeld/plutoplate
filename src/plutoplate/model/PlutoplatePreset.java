package plutoplate.model;

public class PlutoplatePreset {
	private String name;
	private Integer position;

	public PlutoplatePreset(String name, Integer position) {
		this.name = name;
		this.position = position;
	}

	public static PlutoplatePreset loadFromDBRecond(String record) {
		String[] splitRec = record.split("=");
		if ((splitRec != null) && (splitRec.length > 1)) {
			Integer position = Integer.valueOf(Integer.parseInt(splitRec[1]));
			return new PlutoplatePreset(splitRec[0], position);
		}
		return null;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public boolean equals(Object obj) {
		if ((obj instanceof PlutoplatePreset)) {
			return this.name.equalsIgnoreCase(((PlutoplatePreset) obj).getName());
		}
		return false;
	}
}