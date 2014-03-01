import ij.*;
import ij.process.*;
import ij.gui.*;
import jalgs.jseg.*;

import java.awt.*;

import ij.plugin.*;
import ij.plugin.frame.*;

public class My_Plugins implements PlugIn {

	public void run(String arg) {
		ImagePlus img=WindowManager.getCurrentImage();
		binary_processing jays_binary=new binary_processing(img.getWidth(), img.getHeight());
		byte [] data=(byte [] ) img.getProcessor().getPixels();
		jays_binary.erode(data);
		jays_binary.erode(data);
		jays_binary.erode(data);
		jays_binary.erode(data);
		img.updateAndDraw();
		/*int width=1024, height=1024, x_size=1, y_size=1;
		ImagePlus new_img=NewImage.createFloatImage(("Img"), width, height, 2*x_size*y_size, NewImage.FILL_BLACK);
			new_img.setOpenAsHyperStack(true);
			new_img.setDimensions(2,1,x_size*y_size);
			new_img.show();
			new_img.updateAndDraw();*/
	}

}
