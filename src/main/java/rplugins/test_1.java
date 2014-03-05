import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.gui.Roi;
import ij.gui.Line;
import java.awt.*;
import ij.plugin.*;
import javax.swing.*;
import java.util.*;

public class test_1 implements PlugIn {

Boolean validEntry=false;

	int startradius = 5;
	int endradius = 13;
	float autoThresh = 760f;
	float threshold=800f;
	float varThresh = 12000f;
	int numSliceMask = 6;

JTextField startR= new JTextField();
JTextField endR= new JTextField();
JTextField autoT= new JTextField();
JTextField houghT= new JTextField();
JTextField varT= new JTextField();
JTextField numMask= new JTextField();
final JComponent[] inputs = new JComponent[] {
                	new JLabel("Start Radius"),
                	startR,
                	new JLabel("End Radius"),
                	endR,
		new JLabel("Mask Slices (before and after)"),
                	numMask,
	            new JLabel("Auto-fluorecence Threshold"),
               	autoT,
		new JLabel("Hough Threshold"),
		houghT,
		new JLabel("Variance threshold"),
		varT
};


	public void run(String arg) {
		startR.setText("" +startradius);
		endR.setText(""+endradius);
		numMask.setText(""+numSliceMask );
		autoT.setText(""+autoThresh );
		houghT.setText(""+threshold);
		varT.setText(""+varThresh );
		
		getUserInput();
	
		String myParams = "You entered " +
               		 startR.getText() + ", " +
               		 endR.getText() + ", " +
             		   numMask.getText();
		JOptionPane.showMessageDialog(null,myParams, "Your Params...",JOptionPane.PLAIN_MESSAGE);
	}

	void getUserInput(){
		JOptionPane.showMessageDialog(null, inputs, "Lipid Hough Input Parameters", JOptionPane.PLAIN_MESSAGE);
		startradius = Integer.parseInt(startR.getText());
		endradius =  Integer.parseInt(endR.getText());
		numSliceMask = Integer.parseInt(numMask.getText());
		autoThresh = Float.parseFloat(autoT.getText());
		threshold = Float.parseFloat(houghT.getText());
		varThresh = Float.parseFloat(varT.getText());
	}

}
