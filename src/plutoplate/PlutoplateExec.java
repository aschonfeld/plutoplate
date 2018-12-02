package plutoplate;

import plutoplate.controller.PlutoplateController;
import plutoplate.controller.PlutoplateControllerImpl;
import plutoplate.model.PlutoplateModel;
import plutoplate.model.PlutoplateModelImpl;

public class PlutoplateExec {
	public static void main(String[] args) {
		PlutoplateModel model = new PlutoplateModelImpl(new PlutoplateImages());

		@SuppressWarnings("unused")
		PlutoplateController controller = new PlutoplateControllerImpl(model);

		for (String arg : args) {
			if ("-debug".equalsIgnoreCase(arg)) {
				controller.debug();
			}
		}
	}
}