package plutoplate.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PlutoplatePresetDB {

	private List<PlutoplatePreset> presets = new LinkedList<>();

	transient Comparator<PlutoplatePreset> presetComparator = new Comparator<PlutoplatePreset>() {
		public int compare(PlutoplatePreset dp0, PlutoplatePreset dp1) {
			return dp0.getName().compareToIgnoreCase(dp1.getName());
		}
	};

	public void initialize() {
		try {
			File presetFile = new File("./presets.txt");
			if (presetFile.exists()) {
				FileInputStream fstream = new FileInputStream(presetFile);

				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String record;
				while ((record = br.readLine()) != null) {
					System.out.println(record);
					this.presets.add(PlutoplatePreset.loadFromDBRecond(record));
				}
				in.close();
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public List<PlutoplatePreset> getPresets() {
		return this.presets;
	}

	public void savePreset(PlutoplatePreset preset) {
		this.presets.add(preset);
		Collections.sort(this.presets, this.presetComparator);
		writeRecords();
	}

	public void deletePreset(String presetName) {
		for (int i = 0; i < this.presets.size(); i++) {
			if (((PlutoplatePreset) this.presets.get(i)).getName().equals(presetName)) {
				this.presets.remove(i);
			}
		}
		writeRecords();
	}

	public PlutoplatePreset getPreset(String presetName) {
		for (PlutoplatePreset preset : this.presets) {
			if (preset.getName().equalsIgnoreCase(presetName)) {
				return preset;
			}
		}
		return null;
	}

	private void writeRecords() {
		StringBuffer content = new StringBuffer();
		for (PlutoplatePreset dp : this.presets) {
			content.append(dp.getName()).append("=").append(dp.getPosition()).append("\n");
		}
		try {
			File presetFile = new File("./presets.txt");
			presetFile.createNewFile();

			FileWriter fw = new FileWriter(presetFile.getName());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}