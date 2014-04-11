package rplugins;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.*;
import ij.text.*;
import ij.io.*;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.*;

public class test_2 implements PlugIn, ActionListener{
	JButton buttonAdd;
	JButton buttonDelete;
	
	public static String TEST(){
		return "This is only a test";
	}

	public void run(String arg) {
	//ONE
		//Create frame (top level container)
		JFrame frame = new JFrame("Object Editor");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//create components
		JLabel label1 = new JLabel("Add or Delete ROIs here:");
		buttonAdd = new JButton("Add");
		buttonAdd.addActionListener(this);
		buttonDelete = new JButton("Delete");
		buttonDelete.addActionListener(this);
		
		//create panel to go inside frame, and add components
		JPanel contentPane = new JPanel();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(label1)
			.addGroup(layout.createSequentialGroup()
				.addComponent(buttonAdd)
				.addComponent(buttonDelete))
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(label1)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(buttonAdd)
				.addComponent(buttonDelete))
		);
		
		frame.add(contentPane);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		ImagePlus imp=WindowManager.getCurrentImage();
		Roi currentROI = imp.getRoi();
		if(e.getSource() == buttonAdd){
			if(currentROI == null){
				IJ.log("need ROI first");
			}else{
				IJ.setForegroundColor(255, 255, 255);
				IJ.run("Fill", "slice");
				IJ.log("Add");
			}
		}
		if(e.getSource() == buttonDelete){
			//IJ.setForegroundColor(0, 0, 0);
			IJ.log("Delete");
		}
    }
	
}