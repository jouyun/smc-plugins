package rplugins;
//**********
//This macro performs the same function as the "multi" button in the multi measure plugin.
//The functions below were taken from that plugin and modified into a batch-able format.
//Given user inputed folder, it takes a RoiSet.zip file and measures every slice in every stack
//contained in the folder. Looks only for .tif / .jpg / .gif / or .zvi images to open. RoiSet.zip file
//must be in the user selected folder.
//Created by Richard Alexander (RLA) for Limei Ma and the Stowers Institute for Medical Research.
//**********
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.*;
import ij.measure.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.text.*;


public class LIM_Batch_MultiMeasure implements PlugIn {
	public Vector roiVector = new Vector();
	public void run(String arg) {
		
		File dir = new File(IJ.getDirectory("Choose a Directory "));
		File[] list = dir.listFiles();
		openZip(dir + "\\RoiSet.zip");
//IJ.log(roiVector.elementAt(5).toString());

		for(int i = 0; i<list.length; i++){
			if(list[i].toString().endsWith(".tif") || list[i].toString().endsWith(".jpg") || list[i].toString().endsWith(".gif") || list[i].toString().endsWith(".zvi")){
				ImagePlus openImage = new ImagePlus(list[i].toString());
				openImage.show();
				Multi();
				IJ.saveAs( ".txt", list[i].toString());  //WindowManager.getCurrentImage(),
				openImage.close();
				TextWindow tw = (TextWindow)WindowManager.getFrame("Results");
				tw.close();
				//IJ.ImagePlus.close();
				//IJ.log(list[i].toString());
			}
		}	
	}

	public void openZip(String path) {
		//Start RLA additions
		//Vector roiVector = new Vector();
		//End additions
		ZipInputStream in = null;
		ByteArrayOutputStream out;
		boolean noFilesOpened = true; // we're pessimistic and expect that the zip file dosent contain any .roi
		try {
			in = new ZipInputStream(new FileInputStream(path));
			byte[] buf = new byte[1024];
			int len;
			// The original while was: while(true) do something which is not very good
			ZipEntry entry = in.getNextEntry();
			while (entry!=null) {
				/* If we try to open a non-roi file an error is thrown and nothing is opened into
				 * the Roi manager - not a very nice thing to do! Of course we'd expect the zip file to
				 * contain nothing but .roi files, but who knows what users do?
				 *
				 * The easy solution to this problem is to open only .roi files in the zip file.
				 * Another solution is to play with the getRoi of the RoiDecoder. This solution is more
				 * difficult and may not better in a general perspective.
				 *
				 * At any rate I'm a lazy b'stard - I only open files if they end with '.roi'
				 */

				String name = entry.getName();
				if (name.endsWith(".roi")) {
					out = new ByteArrayOutputStream();
					while ((len = in.read(buf)) > 0)
						out.write(buf, 0, len);
					out.close();
					byte[] bytes = out.toByteArray();
					RoiDecoder rd = new RoiDecoder(bytes, name);
					Roi roi = rd.getRoi();
					if (roi!=null) {
						roiVector.addElement(roi);
						//IJ.log("1__ " + roiVector.lastElement().toString());
						/*name = name.substring(0, name.length()-4);

						name = getUniqueName(name);
						tmodel.addRoi(name, roi);
						noFilesOpened = false; // We just added a .roi
						*/
					}
				}
				entry = in.getNextEntry();
			}
			in.close();
//IJ.log(roiVector.elementAt(0).toString());
		} catch (IOException e) { /*error(e.toString());*/ }
		if(noFilesOpened){ /*do something*/ }
	
		//String[] roiArray = new String[roiVector.size()];
		//roiVector.toArray(roiArray);

//		for(int i=0; i<=3; i++){
//			IJ.log((String)roiArray[i]);
//		}

		IJ.run("Set Measurements...", "  mean redirect=None decimal=3");




	}

	public void Multi(){
//For Multi Measure
		Object[] roiArray = roiVector.toArray();
		//IJ.log(roiVector.elementAt(5).toString());
/*
		int[] index = new int[roiVector.size()];
		for(int j =0; j<roiVector.size(); j++){
			IJ.log(j + ":  "+roiArray[j]);
			index[j] = j;
			//IJ.log(""+index[j]);
		}
*/

		ImagePlus imp = WindowManager.getCurrentImage();
		int nSlices = imp.getStackSize();
		int measurements = Analyzer.getMeasurements();
		Analyzer aSys = new Analyzer(); //System Analyzer
		ResultsTable rtSys = Analyzer.getResultsTable();
		ResultsTable rtMulti = new ResultsTable();
		Analyzer aMulti = new Analyzer(imp,Analyzer.getMeasurements(),rtMulti); //Private Analyzer

		for (int slice=1; slice<=nSlices; slice++) {
			int sliceUse = slice;
			//if(nSlices == 1)sliceUse = currentSlice;
			imp.setSlice(sliceUse);
			rtMulti.incrementCounter();
			int roiIndex = 0;
			//if(slices.getState())			//This loop only adds "slice" label and is not needed
			//	rtMulti.addValue("Slice",sliceUse);
			for (int i=0; i<roiVector.size(); i++) {

				Roi roiSet = (Roi)roiArray[i];
				imp.setRoi(roiSet);


				//if (restore(index[i])){

/*
synchronized(this){
try{wait(100L);}
catch(InterruptedException e){}
}
*/
					roiIndex++;
					Roi roi = imp.getRoi();
					ImageStatistics stats = imp.getStatistics(measurements);
					aSys.saveResults(stats,roi); //Save measurements in system results table;
					for (int j = 0; j < ResultsTable.MAX_COLUMNS; j++){
						float[] col = rtSys.getColumn(j);
						String head = rtSys.getColumnHeading(j);
						if ((head != null)&&(col != null))
							rtMulti.addValue(head+roiIndex,rtSys.getValue(j,rtSys.getCounter()-1));
					}
			//	}
			//	else
			//		break;
			}
			aMulti.displayResults();
			aMulti.updateHeadings();
		}

	}


/*	public boolean restore(int index) {
		Roi roi = (Roi)roiArray[index];
		ImagePlus imp = getImage();
		if (imp==null)
			return error("No image selected.");
		//Rectangle r = roi.getBoundingRect();
		//if (r.x+r.width>imp.getWidth() || r.y+r.height>imp.getHeight())
		//	return error("This ROI does not fit the current image.");
		imp.setRoi(roi);
		return true;
	}
*/

}









/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////

/* //For Measure only
		for (int slice=1; slice<=nSlices; slice++) {
			imp.setSlice(slice);
			for (int i=0; i<roiVector.size(); i++) {
				imp.setRoi((Roi)roiArray[i]);
				IJ.run("Measure");

				//if (restore(index[i]))
				//	IJ.run("Measure");
				//else
				//	break;
				//
			}
		}
*/

/*
	public boolean measure() {
		ImagePlus imp = IJ.getImage();
		if (imp==null)
			return false;
		//int[] index = table.getSelectedRows();//list.getSelectedIndexes();
		//if (index.length==0)
		//	return error("At least one ROI must be selected from the list.");

		//int setup = IJ.setupDialog(imp, 0);
		//if (setup==PlugInFilter.DONE)
		//	return false;
		int nSlices = imp.getStackSize();
		int currentSlice = imp.getCurrentSlice();
		for (int slice=1; slice<=nSlices; slice++) {
			imp.setSlice(slice);
			for (int i=0; i<roiVector.size(); i++) {
				imp.setRoi(roiArray[i]);

				//if (restore(index[i]))
				//	IJ.run("Measure");
				//else
				//	break;
				//
			}
		}
		imp.setSlice(currentSlice);
		//if (index.length>1)
		//	IJ.run("Select None");
		return true;
	}
*/
