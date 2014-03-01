import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;

public class My_Plugins implements PlugIn {

	public void run(String arg) {
		int width=1024, height=1024, x_size=1, y_size=1;
		ImagePlus new_img=NewImage.createFloatImage(("Img"), width, height, 2*x_size*y_size, NewImage.FILL_BLACK);
			new_img.setOpenAsHyperStack(true);
			new_img.setDimensions(2,1,x_size*y_size);
			new_img.show();
			new_img.updateAndDraw();
	}

}
