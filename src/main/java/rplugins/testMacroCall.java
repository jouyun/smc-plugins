package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;

public class testMacroCall implements PlugIn {

	public void run(String arg) {
		IJ.showMessage("My_Plugin","Hello world!");
	}

	public static String TEST(){
		return "This is only a test";
	}

}
