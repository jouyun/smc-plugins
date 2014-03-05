import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class Thresh_NaN_to_Zero implements PlugIn {

	public void run(String arg) {
		ImagePlus imp1=WindowManager.getCurrentImage();
		int Nchan = imp1.getNChannels();
		int width=imp1.getWidth();
		int height=imp1.getHeight();
		for(int chan=1; chan<=Nchan; chan++){
			ij.WindowManager.setTempCurrentImage(imp1);
			imp1.setPosition(chan);
			ImagePlus imp=WindowManager.getCurrentImage();
			FloatProcessor fp=(FloatProcessor)imp.getProcessor();
			float[] pixels=(float[])fp.getPixels();
			int numpix= width*height;
			for(int i=0; i<numpix; i++){
				if(Float.isNaN(pixels[i])){
					pixels[i]=0;
				}
			}
		}
	}

}
