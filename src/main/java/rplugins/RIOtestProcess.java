package rplugins;
//**********
//Given user inputed folder, this plugin gets FileInfo for every image in the folder (without opening for TIF),
//measures the stack size,and remembers the smallest stack size. It then opens each image and
//deletes slices off the end of the stack until every image is the same size as the smallest image
//stack. Output images aresaved with prescript "sz_" (for sized). Looks only for .tif / .jpg / .gif / or .zvi
//images to open. 
//Created by Richard Alexander (RLA) for Limei Ma and the Stowers Institute for Medical Research.
//**********

import ij.*;
import ij.gui.*;
import ij.gui.StackWindow;
import ij.io.*;
import java.io.*;
import ij.plugin.*;
import ij.measure.*;
import ij.process.*;
import ij.process.LUT;
import java.awt.Rectangle;
import java.awt.Color;
import java.lang.Object;

public class RIOtestProcess implements PlugIn {

	//public int TOTslices = 999999;	//This variable keeps track of the smallest stack.
	public File dir;
	public File[] fileList;

	public void run(String arg) {
		testSaveComposite();
		//testStats();
		//dir = new File(IJ.getDirectory("Choose a Directory "));
		//fileList = dir.listFiles();
		//makeFolder();
		////setMeasurements()
		//processFiles();
		//IJ.showStatus("done");
	}
	
	public void testSaveComposite(){
		IJ.run("RIObatchChannelMacro");
		/* ImagePlus imp = IJ.getImage();
		StackWindow sw = new StackWindow(imp);
		// ImageProcessor ip = imp.getProcessor();
		IJ.run("Make Composite", "display=Composite");
		// imp.setC(1);
		// java.awt.Color colBlue = java.awt.Color.blue;
		// int bl = colBlue.getBlue();
		// IJ.log(""+bl);
		// LUT lutBlue = ij.process.LUT.createLutFromColor(colBlue);
		// ip.setLut(lutBlue);
		
		sw.setPosition(0, 0, 0);
		IJ.run("Blue");
		

		sw.setPosition(2,0,0);
		IJ.run("Red");
		// try{
			// wait(500);
		// }catch(Exception e){
		// }
		// sw.setPosition(4);
		// IJ.run("Magenta");
		//Stack.setActiveChannels("0111"); */
	}
	
	public void testStats(){
		//String imageName = "green";
		//IJ.selectWindow(imageName);
		ImagePlus imp = IJ.getImage();
		OvalRoi roi = new OvalRoi(538, 237, 110, 104);
		Rectangle boundRec = roi.getBounds();
		IJ.log("x:"+boundRec.x+"  y:"+boundRec.y+"  width:"+boundRec.width+"  height:"+boundRec.height);
		float max = -1;
		float min = -1;
		float sum = 0;
		int count = 0;
		for(int i=boundRec.x; i<boundRec.x+boundRec.width; i++){
			//IJ.log(""+i);
			for(int j = boundRec.y; j<boundRec.y+boundRec.height; j++){
				//IJ.log(""+j);
				if(roi.contains(i,j)){
					int[] pixArray = imp.getPixel(i,j);
					float pixel = (float)pixArray[0];	//assume grayscale image
					sum = sum+pixel;
					count = count+1;
					if(max > -1){
						if(pixel>max){max=pixel;}
						if(pixel<min){min=pixel;}
					}else{
						max = pixel;
						min = pixel;
					}
				}
			}
		}
		float average = sum/((float)count);
		IJ.log("area:"+count+"  avg:"+average +"  min:"+min+"  max:"+max);
		
		// IJ.makeOval(538, 237, 110, 104);
		// ImagePlus imp = IJ.getImage();
		// ByteProcessor bp = new ByteProcessor(imp.getBufferedImage());
		// ImageStatistics stats = new ByteStatistics(bp);
		// IJ.log("area:"+stats.area+"  mean:"+stats.mean +"  mode:"+stats.mode+"  min:"+stats.min+"  max:"+stats.max);
	}
	
	public void setMeasurements(){
		//make sure results table will output everything
		//IJ.run("Set Measurements...", "area mean standard modal min centroid center perimeter bounding fit shape feret's integrated median skewness kurtosis area_fraction stack limit display redirect=None decimal=9");
		IJ.run("Set Measurements...", "  min redirect=None decimal=9");
	}
	
	public void makeFolder(){
		File processDir = new File(dir+"\\processed");
		if (!processDir.exists()){
			processDir.mkdir();
		}
	}
	
	public void processFiles(){
		//expecting a stack with 4 z-slices... Blue, green, red, far red.
		for(int i = 0; i<fileList.length; i++){
			if(fileList[i].toString().endsWith(".tif") || fileList[i].toString().endsWith(".tiff") || fileList[i].toString().endsWith(".jpg") || fileList[i].toString().endsWith(".gif") || fileList[i].toString().endsWith(".zvi")){
				String imageName = fileList[i].getName();
				ImagePlus imp = new ImagePlus(fileList[i].toString());
				imp.show();
				//ImageStack istack = new ImageStack();
				//istack = imp.getImageStack();
				
				//Do far red mask (positive)
				imp.setSlice(4); 
				String kernel = "title=farRed";
				IJ.run("Duplicate...",kernel);
				IJ.selectWindow("farRed");
				ImagePlus impRed = IJ.getImage();
				IJ.setAutoThreshold(impRed, "Triangle dark");
				IJ.run("Convert to Mask");
				IJ.run("Dilate");
				IJ.run("Dilate");
				IJ.run("Fill Holes");
				IJ.run("Erode");
				IJ.run("Erode");
				
				IJ.run("Analyze Particles...", "size=20-Infinity circularity=0.00-1.00 show=Nothing include add");
				kernel = "Save="+dir+"\\processed\\"+imageName+"_roi.zip";
				IJ.run("ROI Manager...",kernel);
				// IJ.makeOval(538, 237, 110, 104);
				// IJ.run("ROI Manager...","Show All with labels");
				// IJ.run("ROI Manager...","Show All");

				
				//kernel = "Select=0";
				//IJ.run("ROI Manager...",kernel);
				ByteProcessor bp = new ByteProcessor(impRed.getBufferedImage());
				ImageStatistics stats = new ByteStatistics(bp);
				//stats.getStatstics(impRed,0,Calibration.NONE);
				// ShortProcessor bp = new ShortProcessor(impRed.getBufferedImage());
				// ImageStatistics stats = new ShortStatistics(bp);
				IJ.log("area:"+stats.area+"  mean:"+stats.mean +"  mode:"+stats.mode+"  min:"+stats.min+"  max:"+stats.max);
				
				// Do green mask (negative)
				IJ.selectWindow(imageName);
				imp.setSlice(2); 
				kernel = "title=green";
				IJ.run("Duplicate...",kernel);
				IJ.selectWindow("green");
				ImagePlus impGreen = IJ.getImage();
				IJ.setAutoThreshold(impGreen, "Triangle dark");
				IJ.run("Convert to Mask");
				
				//String fname = dir+"\\processed\\sub3_"+;
				//new FileSaver(imp).saveAsTiffStack(fname);
				//imp.close();

			}
		}	

	}

}



