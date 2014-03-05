import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.gui.Roi;
import ij.gui.Line;
import java.awt.*;
import ij.plugin.*;

public class test_1 implements PlugIn {

	public void run(String arg) {
		IJ.log("testme0");
		ImagePlus imp1 = WindowManager.getCurrentImage();
		ImageProcessor ip = imp1.getProcessor();

		ImageProcessor ip2=ip.convertToRGB();
		//ip2.drawLine(50, 50, 800, 800);
		//imp1.setProcessor(null,ip2);
		//imp1.updateAndDraw();
		IJ.log("testme");
		new ImagePlus("test",ip2).show();
	}

}

/*		run("RGB Color");
		//IJ.run("Colors...", "foreground=yellow background=yellow selection=yellow");
		IJ.setForegroundColor(255, 255, 0);
		IJ.setBackgroundColor(255, 255, 0);
setImage(imp1);
*/
