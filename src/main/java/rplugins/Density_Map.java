import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

public class Density_Map implements PlugIn {

	JTextField winSize= new JTextField();
	final JComponent[] inputs = new JComponent[] {
                	new JLabel("Window Size"),
                	winSize
	};
	
	public void run(String arg) {
		ImagePlus imp=WindowManager.getCurrentImage();
		ImageStack origStack = imp.getStack();
		int stackSize = origStack.getSize();
		//**********User input START
		winSize.setText("10");
		JOptionPane.showMessageDialog(null, inputs, "Input", JOptionPane.PLAIN_MESSAGE);
		int winDim = Integer.parseInt(winSize.getText());
		//**********User input END
		int width=imp.getWidth();
		int height=imp.getHeight();
		if(winDim>width || winDim>height){
			
		}
		ImageStack retstack=new ImageStack(width,height);
		for(int k=1; k<=stackSize; k++){
			float[] densImg = new float[width*height];
			int sumWin;
			float[] pixels=(float[])origStack.getPixels(k);
			for(int x=0; x<width; x+=winDim){
				for(int y=0; y<height; y+=winDim){
					sumWin = 0;
					for(int i=x; i<x+winDim; i++){
						for(int j=y; j<y+winDim; j++){
							if(i<width && j<height){
								sumWin+=pixels[i+(width*j)];
							}
						}
					}
					//window is summed
					//assin sum to new image
					for(int i=x; i<x+winDim; i++){
						for(int j=y; j<y+winDim; j++){
							if(i<width && j<height){
								densImg[i+(width*j)] = sumWin;
							}
						}
					}
				}
			}
			retstack.addSlice("",densImg);
		}
		

		new ImagePlus("Density Map",retstack).show();
	}

}
