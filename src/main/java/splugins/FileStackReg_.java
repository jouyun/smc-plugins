package splugins;
/*====================================================================	
| Version: August 28, 2007
\===================================================================*/

/* Usage:
 * MultiStackReg can align a stack to itself, as in regular StackReg, or one stack to another.
 *
 * To align one stack to another, place the reference stack in the first slot, and the stack to be 
 * aligned in the second.  MultiStackReg will align each slice of the second stack to the 
 * corresponding slice in the first stack.  Note that both stacks must be the same length.
 *
 * To align a single stack, place it in the first slot and nothing in the second.  Each slice will 
 * be aligned as in normal stackreg.
 *
 * The save checkbox can be used to save the transformation matrix alignment results in,
 * and the load dropdown will apply a previously saved matrix to the selected stack.
 */

/*====================================================================
| EPFL/STI/IOA/LIB
| Philippe Thevenaz
| Bldg. BM-Ecublens 4.137
| Station 17
| CH-1015 Lausanne VD
| Switzerland
|
| phone (CET): +41(21)693.51.61
| fax: +41(21)693.37.01
| RFC-822: philippe.thevenaz@epfl.ch
| X-400: /C=ch/A=400net/P=switch/O=epfl/S=thevenaz/G=philippe/
| URL: http://bigwww.epfl.ch/
\===================================================================*/

/*====================================================================
| This work is based on the following paper:
|
| P. Thevenaz, U.E. Ruttimann, M. Unser
| A Pyramid Approach to Subpixel Registration Based on Intensity
| IEEE Transactions on Image Processing
| vol. 7, no. 1, pp. 27-41, January 1998.
|
| This paper is available on-line at
| http://bigwww.epfl.ch/publications/thevenaz9801.html
|
| Other relevant on-line publications are available at
| http://bigwww.epfl.ch/publications/
\===================================================================*/

/*====================================================================
| Additional help available at http://bigwww.epfl.ch/thevenaz/stackreg/
| Ancillary TurboReg_ plugin available at: http://bigwww.epfl.ch/thevenaz/turboreg/
|
| You'll be free to use this software for research purposes, but you
| should not redistribute it without our consent. In addition, we expect
| you to include a citation or acknowledgment whenever you present or
| publish results that are based on it.
\===================================================================*/

/* A few small changes (loadTransform, appendTransform, multi stack support) to 
 * support load/save functionality and multiple stacks were added by Brad Busse 
 * (  bbusse@stanford.edu ) and released into the public domain, so go by
 * their ^^ guidelines for distribution, etc.
 */

// ImageJ
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.GenericDialog;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ShortProcessor;

// Java 1.1
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;

/*====================================================================
|	StackReg_
\===================================================================*/

/********************************************************************/
public class FileStackReg_
	implements
		PlugIn

{ /* begin class StackReg_ */

/*....................................................................
	Private global variables
....................................................................*/
private static final double TINY = (double)Float.intBitsToFloat((int)0x33FFFFFF);
private String loadPathAndFilename;
private String savePath;
private String saveFile;
private String loadPath;
private String loadFile;
private int transformNumber;
private int tSlice;
private int transformation;
private boolean saveTransform;
private boolean twoStackAlign;
private boolean viewManual;
private boolean loadSingleMatrix;
private boolean fairlyWarned;
private ImagePlus srcImg;
private ImagePlus tgtImg;	
private String srcAction;
private String tgtAction;

/*....................................................................
	Public methods
....................................................................*/

/********************************************************************/
public void run (
	final String arg
) {
	loadPathAndFilename="";
	savePath="";
	transformNumber=0;
	Runtime.getRuntime().gc();
	
	final ImagePlus[] admissibleImageList = createAdmissibleImageList();
	final String[] sourceNames = new String[1+admissibleImageList.length];
    sourceNames[0]="None";
    fairlyWarned=false;
	for (int k = 0; (k < admissibleImageList.length); k++) {
		sourceNames[k+1]=admissibleImageList[k].getTitle();
	}
	
	final String[] targetNames = new String[1+admissibleImageList.length];
    targetNames[0]="None";
	for (int k = 0; (k < admissibleImageList.length); k++) {
		targetNames[k+1]=admissibleImageList[k].getTitle();
	}
	
	ImagePlus imp = WindowManager.getCurrentImage();
	if (imp == null) {
		IJ.error("No image available");
		return;
	}

    //if there are no grayscale images, just quit
    if (admissibleImageList.length == 0) return;
	
    
	
	GenericDialog gd = new GenericDialog("StackReg");
	final String[] transformationItem = {
		"Translation",
		"Rigid Body",
		"Scaled Rotation",
		"Affine"
	};
	gd.addChoice("Transformation:", transformationItem, "Rigid Body");
	gd.addCheckbox("Credits", false);
	gd.showDialog();
	if (gd.wasCanceled()) {
		return;
	}
    

    
    
	transformation = gd.getNextChoiceIndex();
	//twoStackAlign = gd.getNextBoolean();
    
    //We've read all the values in.  Let's try to figure out what the user wants us to do.
    twoStackAlign=false;
    loadPath="";
    loadFile="None";
    
    
    
   
    
    savePath="";
    
    
    
    loadFile="D:\\TransformationMatrices.txt";
    processDirectives(imp,true); 
    return;
                
    
}//end run

/*....................................................................
	Private methods
....................................................................*/
private int processDirectives(ImagePlus imp, boolean loadBool){


	loadPathAndFilename=loadFile;
	int tgt = loadTransform(0, null, null);
	transformation = loadTransform(1, null, null);
	imp.setSlice(tgt);

	final int width = imp.getWidth();
	final int height = imp.getHeight();
	final int targetSlice = imp.getCurrentSlice();
	tSlice=targetSlice;
	double[][] globalTransform = {
		{1.0, 0.0, 0.0},
		{0.0, 1.0, 0.0},
		{0.0, 0.0, 1.0}
	};
	double[][] anchorPoints = null;
	switch (transformation) {
		case 0: {
			anchorPoints = new double[1][3];
			anchorPoints[0][0] = (double)(width / 2);
			anchorPoints[0][1] = (double)(height / 2);
			anchorPoints[0][2] = 1.0;
			break;
		}
		case 1: {
			anchorPoints = new double[3][3];
			anchorPoints[0][0] = (double)(width / 2);
			anchorPoints[0][1] = (double)(height / 2);
			anchorPoints[0][2] = 1.0;
			anchorPoints[1][0] = (double)(width / 2);
			anchorPoints[1][1] = (double)(height / 4);
			anchorPoints[1][2] = 1.0;
			anchorPoints[2][0] = (double)(width / 2);
			anchorPoints[2][1] = (double)((3 * height) / 4);
			anchorPoints[2][2] = 1.0;
			break;
		}
		case 2: {
			anchorPoints = new double[2][3];
			anchorPoints[0][0] = (double)(width / 4);
			anchorPoints[0][1] = (double)(height / 2);
			anchorPoints[0][2] = 1.0;
			anchorPoints[1][0] = (double)((3 * width) / 4);
			anchorPoints[1][1] = (double)(height / 2);
			anchorPoints[1][2] = 1.0;
			break;
		}
		case 3: {
			anchorPoints = new double[3][3];
			anchorPoints[0][0] = (double)(width / 2);
			anchorPoints[0][1] = (double)(height / 4);
			anchorPoints[0][2] = 1.0;
			anchorPoints[1][0] = (double)(width / 4);
			anchorPoints[1][1] = (double)((3 * height) / 4);
			anchorPoints[1][2] = 1.0;
			anchorPoints[2][0] = (double)((3 * width) / 4);
			anchorPoints[2][1] = (double)((3 * height) / 4);
			anchorPoints[2][2] = 1.0;
			break;
		}
		default: {
			IJ.error("Unexpected transformation");
			return 0;
		}
	}
	ImagePlus source = null;
	ImagePlus target = null;
	double[] colorWeights = null;
	switch (imp.getType()) {
		case ImagePlus.GRAY8: {
			target = new ImagePlus("StackRegTarget",
				new ByteProcessor(width, height, new byte[width * height],
				imp.getProcessor().getColorModel()));
			target.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
			break;
		}
		case ImagePlus.GRAY16: {
			target = new ImagePlus("StackRegTarget",
				new ShortProcessor(width, height, new short[width * height],
				imp.getProcessor().getColorModel()));
			target.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
			break;
		}
		case ImagePlus.GRAY32: {
			target = new ImagePlus("StackRegTarget",
				new FloatProcessor(width, height, new float[width * height],
				imp.getProcessor().getColorModel()));
			target.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
			break;
		}
		default: {
			IJ.error("Unexpected image type");
			return 0;
		}
	}
	//we've specified a file to load.  Load it, process it
	String path="";
	
	
	source = registerSlice(source, target, imp, width, height,
		transformation, globalTransform, anchorPoints, colorWeights, targetSlice);
	if (source == null) return 2;
	
	for (int s = targetSlice - 1; (0 < s); s--) {

		globalTransform[0][0] = globalTransform[1][1] = globalTransform[2][2] = 1.0;
		globalTransform[0][1] = globalTransform[0][2] = globalTransform[1][0] = 0.0;
		globalTransform[1][2] = globalTransform[2][0] = globalTransform[2][1] = 0.0;
		source = registerSlice(source, target, imp, width, height,
		transformation, globalTransform, anchorPoints, colorWeights, s);
		if (source == null)	return 2;
	}
	if ((1 < targetSlice) && (targetSlice < imp.getStackSize())) {
		globalTransform[0][0] = 1.0;
		globalTransform[0][1] = 0.0;
		globalTransform[0][2] = 0.0;
		globalTransform[1][0] = 0.0;
		globalTransform[1][1] = 1.0;
		globalTransform[1][2] = 0.0;
		globalTransform[2][0] = 0.0;
		globalTransform[2][1] = 0.0;
		globalTransform[2][2] = 1.0;
		imp.setSlice(targetSlice);
		target.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
	
	}
	for (int s = targetSlice + 1; (s <= imp.getStackSize()); s++) {

		globalTransform[0][0] = globalTransform[1][1] = globalTransform[2][2] = 1.0;
		globalTransform[0][1] = globalTransform[0][2] = globalTransform[1][0] = 0.0;
		globalTransform[1][2] = globalTransform[2][0] = globalTransform[2][1] = 0.0;
		source = registerSlice(source, target, imp, width, height,
		transformation, globalTransform, anchorPoints, colorWeights, s);
		if (source == null) return 2;
	}
	imp.setSlice(targetSlice);
	imp.updateAndDraw();
    return 1;
} 



private ImagePlus getSlice(ImagePlus imp, int index){
	final int width = imp.getWidth();
	final int height = imp.getHeight();
    ImagePlus out = null;
   	imp.setSlice(index);
	double[] colorWeights = null;
    switch (imp.getType()) {

		case ImagePlus.GRAY8: {
			out = new ImagePlus("StackRegTarget",
				new ByteProcessor(width, height, new byte[width * height],
				imp.getProcessor().getColorModel()));
            out.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
			break;
		}
		case ImagePlus.GRAY16: {
			out = new ImagePlus("StackRegTarget",
				new ShortProcessor(width, height, new short[width * height],
				imp.getProcessor().getColorModel()));
            out.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
			break;
		}
		case ImagePlus.GRAY32: {
			out = new ImagePlus("StackRegTarget",
				new FloatProcessor(width, height, new float[width * height],
				imp.getProcessor().getColorModel()));
            out.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
			break;
		}
		default: {
			IJ.error("Unexpected image type");
			return null;
		}
	}
	return out;
}

/*------------------------------------------------------------------*/
//This is kind of an overloaded function.
//'Sgot three different actions it can do,
//and some of these vary depending on the file loaded.
//
private int loadTransform(int action, double[][] src, double[][] tgt){
	try{
	final FileReader fr=new FileReader(loadPathAndFilename);
	BufferedReader br = new BufferedReader(fr);
	String record;
	int separatorIndex;
	String[] fields=new String[3];
	//src=new double[2][3];
	//tgt=new double[2][3];
	
		switch (action){
			case 0:{ //return the index of the former target image, or detect if the 
				//selected file contains only one transformation matrix and start from the 1st
				record = br.readLine();	
				record = record.trim();
				if (record.equals("Transformation")) 
				{
					loadSingleMatrix = true;
					fr.close();
					return 1;
				}else{
					loadSingleMatrix = false;
				}
				record = br.readLine();
				record = br.readLine();
				record = br.readLine();				
				record = br.readLine();
				record = record.trim();
				separatorIndex = record.indexOf("Target img: ");			
				fields[0] = record.substring(separatorIndex+11).trim();
				fr.close();
				return (new Integer(fields[0])).intValue();
			}
			case 1:{ //return the transform used and set twoStack boolean if needed
				int transformation=3;
				if (loadSingleMatrix){
					record = br.readLine();
					record = br.readLine();
					record = record.trim();
					if (record.equals("TRANSLATION")) {
						transformation = 0;
					}
					else if (record.equals("RIGID_BODY")) {
						transformation = 1;
					}
					else if (record.equals("SCALED_ROTATION")) {
						transformation = 2;
					}
					else if (record.equals("AFFINE")) {
						transformation = 3;
					}
					twoStackAlign=false;
					fr.close();
				}else{
					record = br.readLine();		
					record = br.readLine();
					record = br.readLine();
					int discardGlobal=(new Integer(record.trim())).intValue();
					if (discardGlobal==1) 
						twoStackAlign=true;
					else 
						twoStackAlign=false;
					record = br.readLine();				
					record = record.trim();
					if (record.equals("TRANSLATION")) {
						transformation = 0;
					}
					else if (record.equals("RIGID_BODY")) {
						transformation = 1;
					}
					else if (record.equals("SCALED_ROTATION")) {
						transformation = 2;
					}
					else if (record.equals("AFFINE")) {
						transformation = 3;
					}
					fr.close();
				}
				return transformation;
			}
			case 2:{ //return the next transformation in src and tgt, the next src index as return value
				int rtnvalue = -1;
				if (loadSingleMatrix){
					for (int j=0;j<10;j++)
						record = br.readLine();	
					for (int i=0;i<3;i++){
						record = br.readLine();		
						record = record.trim();
						separatorIndex = record.indexOf('\t');			
						fields[0] = record.substring(0, separatorIndex);
						fields[1] = record.substring(separatorIndex);
						fields[0] = fields[0].trim();
						fields[1] = fields[1].trim();
						src[i][0]=(new Double(fields[0])).doubleValue();
						src[i][1]=(new Double(fields[1])).doubleValue();
					}
					record = br.readLine();	
					record = br.readLine();	
					for (int i=0;i<3;i++){
						record = br.readLine();		
						record = record.trim();
						separatorIndex = record.indexOf('\t');
						
						fields[0] = record.substring(0, separatorIndex);
						fields[1] = record.substring(separatorIndex);
						fields[0] = fields[0].trim();
						fields[1] = fields[1].trim();
						tgt[i][0]=(new Double(fields[0])).doubleValue();
						tgt[i][1]=(new Double(fields[1])).doubleValue();
					}
					
				}else{
					record = br.readLine();	
					record = br.readLine();	
					record = br.readLine();	
					for (int i=0;i<transformNumber;i++){
						for (int j=0;j<10;j++)
							record = br.readLine();	
					}
					//read the target and source index
					record = br.readLine();		
					record = br.readLine();		
					record = record.trim();
					separatorIndex = record.indexOf("Target img: ");			
					fields[0] = record.substring(11,separatorIndex).trim();
					rtnvalue = (new Integer(fields[0])).intValue();
					
					for (int i=0;i<3;i++){
						record = br.readLine();		
						record = record.trim();
						separatorIndex = record.indexOf('\t');			
						fields[0] = record.substring(0, separatorIndex);
						fields[1] = record.substring(separatorIndex);
						fields[0] = fields[0].trim();
						fields[1] = fields[1].trim();
						src[i][0]=(new Double(fields[0])).doubleValue();
						src[i][1]=(new Double(fields[1])).doubleValue();
					}
					record = br.readLine();	
					for (int i=0;i<3;i++){
						record = br.readLine();		
						record = record.trim();
						separatorIndex = record.indexOf('\t');
						
						fields[0] = record.substring(0, separatorIndex);
						fields[1] = record.substring(separatorIndex);
						fields[0] = fields[0].trim();
						fields[1] = fields[1].trim();
						tgt[i][0]=(new Double(fields[0])).doubleValue();
						tgt[i][1]=(new Double(fields[1])).doubleValue();
					}
				}
				fr.close();
				return rtnvalue;
			}
			
		}
	}catch(FileNotFoundException e){
		IJ.error("Could not find proper transformation matrix.");
	}catch (IOException e) {
		IJ.error("Error reading from file.");
	}
	return 0;
}

/*------------------------------------------------------------------*/
private void appendTransform(String path, int sourceID, int targetID,double[][] src,double[][] tgt,int transform){
	String Transform="RIGID_BODY";
	switch(transform){
		case 0:{
			Transform="TRANSLATION";
			break;
		}
		case 1:{
			Transform="RIGID_BODY";
			break;
		}
		case 2:{
			Transform="SCALED_ROTATION";
			break;
		}
		case 3:{
			Transform="AFFINE";
			break;
		}
	}
	try {
		final FileWriter fw = new FileWriter(path,true);
		fw.append(Transform+"\n");
		fw.append("Source img: "+sourceID+" Target img: "+targetID+"\n"); 
		fw.append(src[0][0] +"\t"+src[0][1]+"\n");
		fw.append(src[1][0] +"\t"+src[1][1]+"\n");
		fw.append(src[2][0] +"\t"+src[2][1]+"\n");
		fw.append("\n");
		fw.append(tgt[0][0] +"\t"+tgt[0][1]+"\n");
		fw.append(tgt[1][0] +"\t"+tgt[1][1]+"\n");
		fw.append(tgt[2][0] +"\t"+tgt[2][1]+"\n");
		fw.append("\n");
		fw.close();
	}catch (IOException e) {
		IJ.error("Error writing to file.");
	}
}/*appendTransform*/

/*------------------------------------------------------------------*/
private double[][] getTransformationMatrix (
	final double[][] fromCoord,
	final double[][] toCoord,
	final int transformation
) {
	double[][] matrix = new double[3][3];
	switch (transformation) {
		case 0: {
			matrix[0][0] = 1.0;
			matrix[0][1] = 0.0;
			matrix[0][2] = toCoord[0][0] - fromCoord[0][0];
			matrix[1][0] = 0.0;
			matrix[1][1] = 1.0;
			matrix[1][2] = toCoord[0][1] - fromCoord[0][1];
			break;
		}
		case 1: {
			final double angle = Math.atan2(fromCoord[2][0] - fromCoord[1][0],
				fromCoord[2][1] - fromCoord[1][1]) - Math.atan2(toCoord[2][0] - toCoord[1][0],
				toCoord[2][1] - toCoord[1][1]);
			final double c = Math.cos(angle);
			final double s = Math.sin(angle);
			matrix[0][0] = c;
			matrix[0][1] = -s;
			matrix[0][2] = toCoord[0][0] - c * fromCoord[0][0] + s * fromCoord[0][1];
			matrix[1][0] = s;
			matrix[1][1] = c;
			matrix[1][2] = toCoord[0][1] - s * fromCoord[0][0] - c * fromCoord[0][1];
			break;
		}
		case 2: {
			double[][] a = new double[3][3];
			double[] v = new double[3];
			a[0][0] = fromCoord[0][0];
			a[0][1] = fromCoord[0][1];
			a[0][2] = 1.0;
			a[1][0] = fromCoord[1][0];
			a[1][1] = fromCoord[1][1];
			a[1][2] = 1.0;
			a[2][0] = fromCoord[0][1] - fromCoord[1][1] + fromCoord[1][0];
			a[2][1] = fromCoord[1][0] + fromCoord[1][1] - fromCoord[0][0];
			a[2][2] = 1.0;
			invertGauss(a);
			v[0] = toCoord[0][0];
			v[1] = toCoord[1][0];
			v[2] = toCoord[0][1] - toCoord[1][1] + toCoord[1][0];
			for (int i = 0; (i < 3); i++) {
				matrix[0][i] = 0.0;
				for (int j = 0; (j < 3); j++) {
					matrix[0][i] += a[i][j] * v[j];
				}
			}
			v[0] = toCoord[0][1];
			v[1] = toCoord[1][1];
			v[2] = toCoord[1][0] + toCoord[1][1] - toCoord[0][0];
			for (int i = 0; (i < 3); i++) {
				matrix[1][i] = 0.0;
				for (int j = 0; (j < 3); j++) {
					matrix[1][i] += a[i][j] * v[j];
				}
			}
			break;
		}
		case 3: {
			double[][] a = new double[3][3];
			double[] v = new double[3];
			a[0][0] = fromCoord[0][0];
			a[0][1] = fromCoord[0][1];
			a[0][2] = 1.0;
			a[1][0] = fromCoord[1][0];
			a[1][1] = fromCoord[1][1];
			a[1][2] = 1.0;
			a[2][0] = fromCoord[2][0];
			a[2][1] = fromCoord[2][1];
			a[2][2] = 1.0;
			invertGauss(a);
			v[0] = toCoord[0][0];
			v[1] = toCoord[1][0];
			v[2] = toCoord[2][0];
			for (int i = 0; (i < 3); i++) {
				matrix[0][i] = 0.0;
				for (int j = 0; (j < 3); j++) {
					matrix[0][i] += a[i][j] * v[j];
				}
			}
			v[0] = toCoord[0][1];
			v[1] = toCoord[1][1];
			v[2] = toCoord[2][1];
			for (int i = 0; (i < 3); i++) {
				matrix[1][i] = 0.0;
				for (int j = 0; (j < 3); j++) {
					matrix[1][i] += a[i][j] * v[j];
				}
			}
			break;
		}
		default: {
			IJ.error("Unexpected transformation");
		}
	}
	matrix[2][0] = 0.0;
	matrix[2][1] = 0.0;
	matrix[2][2] = 1.0;
	return(matrix);
} /* end getTransformationMatrix */

/*------------------------------------------------------------------*/
private void invertGauss (
	final double[][] matrix
) {
	final int n = matrix.length;
	final double[][] inverse = new double[n][n];
	for (int i = 0; (i < n); i++) {
		double max = matrix[i][0];
		double absMax = Math.abs(max);
		for (int j = 0; (j < n); j++) {
			inverse[i][j] = 0.0;
			if (absMax < Math.abs(matrix[i][j])) {
				max = matrix[i][j];
				absMax = Math.abs(max);
			}
		}
		inverse[i][i] = 1.0 / max;
		for (int j = 0; (j < n); j++) {
			matrix[i][j] /= max;
		}
	}
	for (int j = 0; (j < n); j++) {
		double max = matrix[j][j];
		double absMax = Math.abs(max);
		int k = j;
		for (int i = j + 1; (i < n); i++) {
			if (absMax < Math.abs(matrix[i][j])) {
				max = matrix[i][j];
				absMax = Math.abs(max);
				k = i;
			}
		}
		if (k != j) {
			final double[] partialLine = new double[n - j];
			final double[] fullLine = new double[n];
			System.arraycopy(matrix[j], j, partialLine, 0, n - j);
			System.arraycopy(matrix[k], j, matrix[j], j, n - j);
			System.arraycopy(partialLine, 0, matrix[k], j, n - j);
			System.arraycopy(inverse[j], 0, fullLine, 0, n);
			System.arraycopy(inverse[k], 0, inverse[j], 0, n);
			System.arraycopy(fullLine, 0, inverse[k], 0, n);
		}
		for (k = 0; (k <= j); k++) {
			inverse[j][k] /= max;
		}
		for (k = j + 1; (k < n); k++) {
			matrix[j][k] /= max;
			inverse[j][k] /= max;
		}
		for (int i = j + 1; (i < n); i++) {
			for (k = 0; (k <= j); k++) {
				inverse[i][k] -= matrix[i][j] * inverse[j][k];
			}
			for (k = j + 1; (k < n); k++) {
				matrix[i][k] -= matrix[i][j] * matrix[j][k];
				inverse[i][k] -= matrix[i][j] * inverse[j][k];
			}
		}
	}
	for (int j = n - 1; (1 <= j); j--) {
		for (int i = j - 1; (0 <= i); i--) {
			for (int k = 0; (k <= j); k++) {
				inverse[i][k] -= matrix[i][j] * inverse[j][k];
			}
			for (int k = j + 1; (k < n); k++) {
				matrix[i][k] -= matrix[i][j] * matrix[j][k];
				inverse[i][k] -= matrix[i][j] * inverse[j][k];
			}
		}
	}
	for (int i = 0; (i < n); i++) {
		System.arraycopy(inverse[i], 0, matrix[i], 0, n);
	}
} /* end invertGauss */

/*------------------------------------------------------------------*/


/*------------------------------------------------------------------*/
private ImagePlus registerSlice (
	ImagePlus source,
	ImagePlus target,
	ImagePlus imp,
	final int width,
	final int height,
	final int transformation,
	final double[][] globalTransform,
	final double[][] anchorPoints,
	final double[] colorWeights,
	int s
) {
	imp.setSlice(s);
	try {
		Object turboReg = null;
		Method method = null;
		double[][] sourcePoints = null;
		double[][] targetPoints = null;
		double[][] localTransform = null;
		switch (imp.getType()) {
			case ImagePlus.GRAY8: {
				source = new ImagePlus("StackRegSource", new ByteProcessor(
					width, height, (byte[])imp.getProcessor().getPixels(),
					imp.getProcessor().getColorModel()));
				break;
			}
			case ImagePlus.GRAY16: {
				source = new ImagePlus("StackRegSource", new ShortProcessor(
					width, height, (short[])imp.getProcessor().getPixels(),
					imp.getProcessor().getColorModel()));
				break;
			}
			case ImagePlus.GRAY32: {
				source = new ImagePlus("StackRegSource", new FloatProcessor(
					width, height, (float[])imp.getProcessor().getPixels(),
					imp.getProcessor().getColorModel()));
				break;
			}
			default: {
				IJ.error("Unexpected image type");
				return(null);
			}
		}
		final FileSaver sourceFile = new FileSaver(source);
		final String sourcePathAndFileName = IJ.getDirectory("temp") + source.getTitle();
		sourceFile.saveAsTiff(sourcePathAndFileName);
		final FileSaver targetFile = new FileSaver(target);
		final String targetPathAndFileName = IJ.getDirectory("temp") + target.getTitle();
		targetFile.saveAsTiff(targetPathAndFileName);
		if (loadPathAndFilename==""){//if we've specified a transformation to load, we needen't bother with aligning them again
			
		}else{
			sourcePoints=new double[3][2];
			targetPoints=new double[3][2];
			int test= loadTransform(2, sourcePoints, targetPoints);
			if (test != -1 && test != s){
                if (!twoStackAlign && !loadSingleMatrix && !fairlyWarned){
                    IJ.error ("We've found some strangeness: the current transformation file index ("+test+") \n"+
                                "and image index ("+s+") don't line up, which this type of alignment needs. \n"+
                                "We'll proceed for now, but it may not work.");
                    fairlyWarned=true;
                }
                s=test;
                imp.setSlice(s);
			}
			transformNumber++;
		}
		localTransform = getTransformationMatrix(targetPoints, sourcePoints,
			transformation);
		double[][] rescued = {
			{globalTransform[0][0], globalTransform[0][1], globalTransform[0][2]},
			{globalTransform[1][0], globalTransform[1][1], globalTransform[1][2]},
			{globalTransform[2][0], globalTransform[2][1], globalTransform[2][2]}
		};
		for (int i = 0; (i < 3); i++) {
			for (int j = 0; (j < 3); j++) {
				globalTransform[i][j] = 0.0;
				for (int k = 0; (k < 3); k++) {
					globalTransform[i][j] += localTransform[i][k] * rescued[k][j];
				}
			}
		}
		switch (imp.getType()) {
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32: {
				switch (transformation) {
					case 0: {
						sourcePoints = new double[1][3];
						for (int i = 0; (i < 3); i++) {
							sourcePoints[0][i] = 0.0;
							for (int j = 0; (j < 3); j++) {
								sourcePoints[0][i] += globalTransform[i][j]
									* anchorPoints[0][j];
							}
						}
						turboReg = IJ.runPlugIn("TurboReg_", "-transform"
							+ " -file " + sourcePathAndFileName
							+ " " + width + " " + height
							+ " -translation"
							+ " " + sourcePoints[0][0] + " " + sourcePoints[0][1]
							+ " " + (width / 2) + " " + (height / 2)
							+ " -hideOutput"
						);
						break;
					}
					case 1: {
						sourcePoints = new double[3][3];
						for (int i = 0; (i < 3); i++) {
							sourcePoints[0][i] = 0.0;
							sourcePoints[1][i] = 0.0;
							sourcePoints[2][i] = 0.0;
							for (int j = 0; (j < 3); j++) {
								sourcePoints[0][i] += globalTransform[i][j]
									* anchorPoints[0][j];
								sourcePoints[1][i] += globalTransform[i][j]
									* anchorPoints[1][j];
								sourcePoints[2][i] += globalTransform[i][j]
									* anchorPoints[2][j];
							}
						}
						turboReg = IJ.runPlugIn("TurboReg_", "-transform"
							+ " -file " + sourcePathAndFileName
							+ " " + width + " " + height
							+ " -rigidBody"
							+ " " + sourcePoints[0][0] + " " + sourcePoints[0][1]
							+ " " + (width / 2) + " " + (height / 2)
							+ " " + sourcePoints[1][0] + " " + sourcePoints[1][1]
							+ " " + (width / 2) + " " + (height / 4)
							+ " " + sourcePoints[2][0] + " " + sourcePoints[2][1]
							+ " " + (width / 2) + " " + ((3 * height) / 4)
							+ " -hideOutput"
						);
						break;
					}
					case 2: {
						sourcePoints = new double[2][3];
						for (int i = 0; (i < 3); i++) {
							sourcePoints[0][i] = 0.0;
							sourcePoints[1][i] = 0.0;
							for (int j = 0; (j < 3); j++) {
								sourcePoints[0][i] += globalTransform[i][j]
									* anchorPoints[0][j];
								sourcePoints[1][i] += globalTransform[i][j]
									* anchorPoints[1][j];
							}
						}
						turboReg = IJ.runPlugIn("TurboReg_", "-transform"
							+ " -file " + sourcePathAndFileName
							+ " " + width + " " + height
							+ " -scaledRotation"
							+ " " + sourcePoints[0][0] + " " + sourcePoints[0][1]
							+ " " + (width / 4) + " " + (height / 2)
							+ " " + sourcePoints[1][0] + " " + sourcePoints[1][1]
							+ " " + ((3 * width) / 4) + " " + (height / 2)
							+ " -hideOutput"
						);
						break;
					}
					case 3: {
						sourcePoints = new double[3][3];
						for (int i = 0; (i < 3); i++) {
							sourcePoints[0][i] = 0.0;
							sourcePoints[1][i] = 0.0;
							sourcePoints[2][i] = 0.0;
							for (int j = 0; (j < 3); j++) {
								sourcePoints[0][i] += globalTransform[i][j]
									* anchorPoints[0][j];
								sourcePoints[1][i] += globalTransform[i][j]
									* anchorPoints[1][j];
								sourcePoints[2][i] += globalTransform[i][j]
									* anchorPoints[2][j];
							}
						}
						turboReg = IJ.runPlugIn("TurboReg_", "-transform"
							+ " -file " + sourcePathAndFileName
							+ " " + width + " " + height
							+ " -affine"
							+ " " + sourcePoints[0][0] + " " + sourcePoints[0][1]
							+ " " + (width / 2) + " " + (height / 4)
							+ " " + sourcePoints[1][0] + " " + sourcePoints[1][1]
							+ " " + (width / 4) + " " + ((3 * height) / 4)
							+ " " + sourcePoints[2][0] + " " + sourcePoints[2][1]
							+ " " + ((3 * width) / 4) + " " + ((3 * height) / 4)
							+ " -hideOutput"
						);
						break;
					}
					default: {
						IJ.error("Unexpected transformation");
						return(null);
					}
				}
				if (turboReg == null) {
					throw(new ClassNotFoundException());
				}
				method = turboReg.getClass().getMethod("getTransformedImage", null);
				ImagePlus transformedSource = (ImagePlus)method.invoke(turboReg, null);
				transformedSource.getStack().deleteLastSlice();
				switch (imp.getType()) {
					case ImagePlus.GRAY8: {
						transformedSource.getProcessor().setMinAndMax(0.0, 255.0);
						final ImageConverter converter = new ImageConverter(transformedSource);
						converter.convertToGray8();
						break;
					}
					case ImagePlus.GRAY16: {
						transformedSource.getProcessor().setMinAndMax(0.0, 65535.0);
						final ImageConverter converter = new ImageConverter(transformedSource);
						converter.convertToGray16();
						break;
					}
					case ImagePlus.GRAY32: {
						break;
					}
					default: {
						IJ.error("Unexpected image type");
						return(null);
					}
				}
				imp.setProcessor(null, transformedSource.getProcessor());
				break;
			}
			default: {
				IJ.error("Unexpected image type");
				return(null);
			}
		}
	} catch (NoSuchMethodException e) {
		IJ.error("Unexpected NoSuchMethodException " + e);
		return(null);
	} catch (IllegalAccessException e) {
		IJ.error("Unexpected IllegalAccessException " + e);
		return(null);
	} catch (InvocationTargetException e) {
		IJ.error("Unexpected InvocationTargetException " + e);
		return(null);
	} catch (ClassNotFoundException e) {
		IJ.error("Please download TurboReg_ from\nhttp://bigwww.epfl.ch/thevenaz/turboreg/");
		return(null);
	}
	return(source);
} /* end registerSlice */

/*------------------------------------------------------------------*/
private ImagePlus[] createAdmissibleImageList (
) {
	final int[] windowList = WindowManager.getIDList();
	final Stack stack = new Stack();
	for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) {
		final ImagePlus imp = WindowManager.getImage(windowList[k]);
		if ((imp != null) && ((imp.getType() == imp.GRAY16)
			|| (imp.getType() == imp.GRAY32)
			|| ((imp.getType() == imp.GRAY8) && !imp.getStack().isHSB()))) {
			stack.push(imp);
		}
	}
	final ImagePlus[] admissibleImageList = new ImagePlus[stack.size()];
	int k = 0;
	while (!stack.isEmpty()) {
		admissibleImageList[k++] = (ImagePlus)stack.pop();
	}
    if (k==0 && (windowList != null && windowList.length > 0 )){
        IJ.error("No grayscale images found!  \n\nAre you using a color image?\n"+
                 "If so, try splitting it into grayscale channels,\n"+
                 "then use the best of those to align the stack\n"+
                 "and apply the transformation to the rest.");
    }
	return(admissibleImageList);
} /* end createAdmissibleImageList */

} /* end class StackReg_ */

/*====================================================================
|	stackRegCredits
\===================================================================*/

/********************************************************************/
