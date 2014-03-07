package rplugins;
//*****************************************************
//  This plugin draws a line on an image stack. The user inputs a CSV file,
//  with the frame number, x1, y1, x2, and y2 (the start and end point of the line).
//  The plugin reads the file and plots the points indicated. There should be no 
//  header in the CSV file.
//  Created by Richard Alexander (RLA) for Bertrand Benzaff and the Stowers Institute for Medical Research.
//*****************************************************
import ij.*;
import ij.io.*;
import ij.io.OpenDialog;
import ij.process.*;
import ij.gui.*;
import java.io.*;
import java.awt.*;
//import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import ij.plugin.*;

public class DrawLine_RLA extends JPanel implements PlugIn {
	int[] frameNum;
	int[] x1, y1, x2, y2;
	int numLines = 0;
	ImageProcessor ip3;

	public void run(String arg) {
		
		OpenDialog getFile = new OpenDialog("Choose a CSV file","",".csv");
		String csvFile = getFile.getDirectory()  + getFile.getFileName();
		//IJ.log(csvFile);

		//Open the CSV File
		File CSVfile = new File(csvFile);
		if (!CSVfile.exists() || !CSVfile.canRead()){
			//System.out.println("Cannot read CSVfile. It did not work.");
			IJ.showMessage("Cannot read CSV File. It did not work.");
		}

		//Read the CSV File
		try{
			FileReader fr1 = new FileReader (CSVfile);		//Open the CSV to see how many cells there are
			BufferedReader in = new BufferedReader(fr1);			
			
			while ((in.readLine()) != null){
				numLines = numLines+1;
			}
			fr1.close();
			in.close();										//Close the Cells file
			
			String[] csvLine = new String[numLines];			//Create a new array now that we know the number of elements
			
			FileReader fr2 = new FileReader (CSVfile);		//Open the CSV file again and dump all data into the new array
			BufferedReader csvIn = new BufferedReader(fr2);
			int n=0;  //initialize counter: first line is line 0
			while (n<numLines){
				csvLine[n] = csvIn.readLine();
				//System.out.println(csvLine[n]);			
				n=n+1;
			}
			fr2.close();
			csvIn.close();

			//Get x coord and y coord from the csvLine string
			int m = 0;
			frameNum = new int[numLines];
			x1=new int[numLines];
			y1=new int[numLines];
			x2=new int[numLines];
			y2=new int[numLines];
			while(m < numLines){
				String[] LineXY = csvLine[m].split(",");				
				frameNum[m] = Integer.parseInt(LineXY[0]);
				x1[m] = Integer.parseInt(LineXY[1]);
				y1[m] = Integer.parseInt(LineXY[2]);
				x2[m] = Integer.parseInt(LineXY[3]);
				y2[m] = Integer.parseInt(LineXY[4]);
				//IJ.log(frameNum[m]+", "+x1[m]+", "+y1[m]+", "+x2[m]+", "+y2[m]);
				m=m+1;
			}
		}
		catch (IOException e){
			//System.out.println("File not found.");
			IJ.showMessage("File not found.");
		}
		
		///// Get image, convert to RGB, then get stack
		ImagePlus imp1 = WindowManager.getCurrentImage();
		IJ.run("RGB Color");
		ImageStack stack=imp1.getStack();

		////// Draw lines on each slice of the image
		for(int i = 0; i<numLines; i++){
			ip3=stack.getProcessor(i+1);
			ip3.setColor(Color.RED);
			ip3.setLineWidth(5);
			ip3.drawLine(x1[i], y1[i], x2[i], y2[i]);
			imp1.updateAndDraw();
		}
		
	}

}
