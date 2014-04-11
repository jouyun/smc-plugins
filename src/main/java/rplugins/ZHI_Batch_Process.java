package rplugins;
//**********
//Given user inputed folder, this plugin ...
//Created by Richard Alexander (RLA) for the Linheng Li Lab and the Stowers Institute for Medical Research.
//**********

import ij.*;
import ij.io.*;
import java.io.*;
import ij.plugin.*;
import ij.measure.*;
import ij.process.*;
import ij.plugin.frame.RoiManager;
import ij.plugin.frame.RoiManager.*;
import java.awt.Rectangle;
import ij.gui.*;
import ij.macro.Interpreter;
import java.util.*;
import java.util.zip.*;

public class ZHI_Batch_Process implements PlugIn {

	//public int TOTslices = 999999;	//This variable keeps track of the smallest stack.
	public File dir;
	public File[] fileList;
	public RoiManager roim;
	public File processDir;
	public int RedTot = 0;
	public int redNoGreenTot = 0;

	public void run(String arg) {
		dir = new File(IJ.getDirectory("Choose a Directory "));
		fileList = dir.listFiles();
		makeFolder();
		//setMeasurements()
		processFiles();
		saveTotals();
		IJ.showStatus("done");
	}
	
	public void setMeasurements(){
		//make sure results table will output everything
		//IJ.run("Set Measurements...", "area mean standard modal min centroid center perimeter bounding fit shape feret's integrated median skewness kurtosis area_fraction stack limit display redirect=None decimal=9");
		IJ.run("Set Measurements...", "  min redirect=None decimal=9");
	}
	
	public void makeFolder(){
		processDir = new File(dir+"\\processed");
		if (!processDir.exists()){
			processDir.mkdir();
		}
	}
	
	public void saveTotals(){
		File f1 = new File(dir+"\\processed\\"+"_REDpos= "+RedTot);
		File f2 = new File(dir+"\\processed\\"+"_REDposGREENneg= "+redNoGreenTot);
		try{
			f1.createNewFile();
			f2.createNewFile();
		}catch(Exception e){
			IJ.log("Error:  "+e);
		}
	}
	
	public void processFiles(){
		//expecting a stack with 4 z-slices... Blue, green, red, far red.
		//Channel order: Red (bone),  Green (negative),  Blue (nucleus),  FarRed (pos stem cell)
		for(int i = 0; i<fileList.length; i++){
			if(fileList[i].toString().endsWith(".tif") || fileList[i].toString().endsWith(".tiff") || fileList[i].toString().endsWith(".jpg") || fileList[i].toString().endsWith(".gif") || fileList[i].toString().endsWith(".zvi")){
				String imageName = fileList[i].getName();
				ImagePlus imp = new ImagePlus(fileList[i].toString());
				imp.setSlice(2);
				ImageStatistics stats = imp.getStatistics();
				if(stats.max>500){	//only process image if green chanel has stuff (no blank images)
					imp.show();
					
					//ImageStack istack = new ImageStack();
					//istack = imp.getImageStack();
					
					//Do far red mask (positive)
					imp.setSlice(4); 
					String kernel = "title=farRed";
					IJ.run("Duplicate...",kernel);
					IJ.selectWindow("farRed");
					ImagePlus impRed = IJ.getImage();
					//IJ.setAutoThreshold(impRed, "Triangle dark");	//set triangle threshold
					IJ.setThreshold(500, 50000);	//set manual threshold
					IJ.run("Convert to Mask");
					IJ.run("Dilate");
					IJ.run("Dilate");
					IJ.run("Fill Holes");
					IJ.run("Erode");
					IJ.run("Erode");
					
					IJ.run("Analyze Particles...", "size=20-Infinity circularity=0.00-1.00 show=Nothing include add");
					kernel = "Save="+dir+"\\processed\\"+imageName+"_roi.zip";
					IJ.run("ROI Manager...",kernel);
					
					// Do green mask (negative)
					IJ.selectWindow(imageName);
					imp.setSlice(2); 
					kernel = "title=green";
					IJ.run("Duplicate...",kernel);
					IJ.selectWindow("green");
					ImagePlus impGreen = IJ.getImage();
					//IJ.setAutoThreshold(impGreen, "Triangle dark");	//set triangle threshold
					IJ.setThreshold(285, 50000);	//set manual threshold
					IJ.run("Convert to Mask");
					String fileSave1 = processDir +"\\"+imageName+"_green";
					IJ.saveAs("Tiff", fileSave1);
					
					// Loop through ROIs in RoiManager
					roim = RoiManager.getInstance2();	//Interpreter interp = Interpreter.getInstance(); //RoiManager roim =interp.getBatchModeRoiManager(); //
					int countAll = roim.getCount();
					
					if(countAll>0){
						//IJ.log(""+countAll);
						Roi[] roiArray = roim.getRoisAsArray();
						List<Integer> goodIndex = new ArrayList<Integer>();
						int[] allIndex = new int[countAll];
						for(int q=0; q<countAll; q++){
							IJ.showProgress(q, countAll);
							allIndex[q] = q;
							Roi roi = roiArray[q];
							int current =q;
							//IJ.log(""+current);
							if(getStats(roi)){
								goodIndex.add(q);
							}
						}
						//Integer[] indexesInteger = goodIndex.toArray(new Integer[goodIndex.size()]);
						int[] indexes = toIntArray(goodIndex);
						String fname = processDir+"\\"+imageName+"_FREDpos("+countAll+").zip";
						if(countAll>0){
							RedTot = RedTot + countAll;
							saveMultiple(roiArray, allIndex, fname, roim);	//save all
						}
						int countGood = indexes.length;
						fname = processDir+"\\"+imageName+"_FREDpos_GREENneg("+countGood+").zip";
						if(countGood>0){
							redNoGreenTot = redNoGreenTot + countGood;
							saveMultiple(roiArray, indexes, fname, roim);	//save only good ones
						}
						
						//make sure to delete rois before moving to next image
						//roim.close();
						roim.runCommand("Deselect");
						roim.runCommand("Delete");
					}
				/* 	// Do red mask(sinusoid)
					IJ.selectWindow(imageName);
					imp.setSlice(1); 
					kernel = "title=Red";
					IJ.run("Duplicate...",kernel);
					IJ.selectWindow("Red");
					ImagePlus impRedSin = IJ.getImage();
					IJ.setAutoThreshold(impRedSin, "Triangle dark");
					IJ.run("Convert to Mask");
					// IJ.run("Dilate");
					// IJ.run("Dilate");
					// IJ.run("Fill Holes");
					// IJ.run("Erode");
					// IJ.run("Erode");
					
					IJ.run("Analyze Particles...", "size=20-Infinity circularity=0.00-1.00 show=Nothing include add");
					kernel = "Save="+dir+"\\processed\\"+imageName+"_RED.zip";
					IJ.run("ROI Manager...",kernel);
					RoiManager roim2 = RoiManager.getInstance2();
					countAll = roim2.getCount();
					roiArray = roim2.getRoisAsArray();
					
					int[] redIndex = new int[countAll];
					for(int q=0; q<countAll; q++){
						redIndex[q] = q;
					}
					fname = processDir+"\\"+imageName+"_RED.zip";
					if(countAll>0){
						saveMultiple(roiArray, redIndex, fname, roim2);	//save only good ones
					} 
					
					//make sure to delete rois before moving to next image
					//roim2.close(); 
					roim.runCommand("Deselect");
					roim.runCommand("Delete");
					//goodIndex = new ArrayList<Integer>();
				*/	
					IJ.selectWindow(imageName);
					IJ.run("RIObatchChannelMacro");
					String fileSave = processDir +"\\"+imageName;
					IJ.saveAs("Tiff", fileSave);
					
					IJ.run("Close All");
				}
			}
		}	
	}
	
	int[] toIntArray(List<Integer> list){
		int[] ret = new int[list.size()];
		for(int i = 0;i < ret.length;i++)
		ret[i] = list.get(i);
		return ret;
	}
	
	boolean saveMultiple(Roi[] roiArray, int[] indexes, String path, RoiManager roiMan) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);
			IJ.showStatus("Saving Rois");
            for (int i=0; i<indexes.length; i++) {
				IJ.showProgress(i, indexes.length);
                String label = roiMan.getName(indexes[i]);	//list.getItem(indexes[i]);
                Roi roi = roiArray[indexes[i]];				//(Roi)rois.get(label);
                if (!label.endsWith(".roi")){
					label += ".roi";
				}
                zos.putNextEntry(new ZipEntry(label));
                re.write(roi);
                out.flush();
            }
            out.close();
        }
        catch (IOException e) {
            IJ.log(""+e);
            return false;
        }
        return true;
    }
	
	public boolean getStats(Roi roi){
		//String imageName = "green";
		//IJ.selectWindow(imageName);
		ImagePlus imp = IJ.getImage();
		//OvalRoi roi = new OvalRoi(538, 237, 110, 104);
		Rectangle boundRec = roi.getBounds();
		//IJ.log("x:"+boundRec.x+"  y:"+boundRec.y+"  width:"+boundRec.width+"  height:"+boundRec.height);
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
		//IJ.log("area:"+count+"  avg:"+average +"  min:"+min+"  max:"+max);
		if(max==0){
			return true;
		}else{
			return false;
		}
	}

}
