package splugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;

public class My_Plugin implements PlugIn {

	public void run(String arg) {
		//IJ.showMessage("My_Plugin","Hello world!");
		IJ.runMacroFile("U:\\smc\\FiJi.app\\macros\\Call_From_Plugin_Worm_Stitch.ijm","bah");
	}

}
