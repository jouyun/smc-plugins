package splugins;

/*====================================================================	
| Version: March 2, 2005
\===================================================================*/

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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.IndexColorModel;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.FileWriter;

/*====================================================================
|	StackReg_
\===================================================================*/

/********************************************************************/
public class multichannel_stackreg
	implements
		PlugIn

{ /* begin class StackReg_ */

/*....................................................................
	Private global variables
....................................................................*/
private static final double TINY = (double)Float.intBitsToFloat((int)0x33FFFFFF);

/*....................................................................
	Public methods
....................................................................*/

//ImageJ hyperstacks are always in CZT order


/********************************************************************/
public void run (
	final String arg
) {
	Runtime.getRuntime().gc();
	final ImagePlus imp = WindowManager.getCurrentImage();
	if (imp == null) {
		IJ.error("No image available");
		return;
	}
	if (imp.getStack().isRGB() || imp.getStack().isHSB()) {
		IJ.error("Unable to process either RGB or HSB stacks");
		return;
	}
	GenericDialog gd = new GenericDialog("StackReg");
	final String[] transformationItem = {
		"Translation",
		"Rigid Body",
		"Scaled Rotation",
		"Affine"
	};
	gd.addChoice("Transformation:", transformationItem, "Rigid Body");
	gd.addCheckbox("Credits", false);
	gd.addStringField("Temp space", IJ.getDirectory("temp"));
	gd.showDialog();
	if (gd.wasCanceled()) {
		return;
	}
	final int transformation = gd.getNextChoiceIndex();
	if (gd.getNextBoolean()) {
		final stackRegCredits dialog = new stackRegCredits(IJ.getInstance());
		GUI.center(dialog);
		dialog.setVisible(true);
		return;
	}
	final String temp_space=gd.getNextString();
	final int width = imp.getWidth();
	final int height = imp.getHeight();
	final int targetSlice = imp.getCurrentSlice();
	final int[] dimensions=imp.getDimensions();
	
	if (dimensions[3]>1&&dimensions[4]>1)
	{
		IJ.error("Cannot handle both time series and z-stack");
		return;
	}
	int nSlices, nChannels, targetChannel;
	nChannels=dimensions[2];
	if (dimensions[3]>1) nSlices=dimensions[3];
	else
	{
		if (dimensions[4]>1) nSlices=dimensions[4];
		else 
		{
			IJ.error("Need a non-channel only stack");
			return;
		}
	}
	targetChannel=(targetSlice-1)%nChannels;
	double[][] globalTransform = {
		{1.0, 0.0, 0.0},
		{0.0, 1.0, 0.0},
		{0.0, 0.0, 1.0}
	};
	//Setup output temp file for adjustments
	FileWriter outputStream=null;

	String sss;
	sss=temp_space;
	try{
		outputStream=new FileWriter(temp_space+"tmp.txt");
	}
	catch (IOException a){return;}

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
			return;
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
			return;
		}
	}
	for (int s = targetSlice - nChannels; (0 < s); s-=nChannels) {
		//Do the registration and replace image
		source = registerSlice(source, target, imp, width, height,
			transformation, globalTransform, anchorPoints, colorWeights, s, temp_space);
		//Now do it for all of the other channels using the same globalTransform
		for (int m=0; m<nChannels; m++)
		{
			if (m!=targetChannel)
			{
				applyregisterSlice(imp, width, height,
						transformation, globalTransform, anchorPoints, colorWeights, s-targetChannel+m, temp_space);
			}
		}
				
		//Log the globaltranform for reconstruction later
		String towrite;
		towrite="";
		for (int m=0; m<3; m++)
		{
			for (int n=0; n<3; n++)
			{
				towrite=towrite+Double.toString(globalTransform[m][n])+"\n";
			}
		}
		try {
			outputStream.write(towrite);
		}
		catch (IOException t){}

		if (source == null) {
			imp.setSlice(targetSlice);
			return;
		}
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
		switch (imp.getType()) {
			case ImagePlus.COLOR_256:
			case ImagePlus.COLOR_RGB: {
				target = getGray32("StackRegTarget", imp, colorWeights);
				break;
			}
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32: {
				target.getProcessor().copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
				break;
			}
			default: {
				IJ.error("Unexpected image type");
				return;
			}
		}
	}
	for (int s = targetSlice + nChannels; (s <= imp.getStackSize()); s+=nChannels) {
		source = registerSlice(source, target, imp, width, height,
			transformation, globalTransform, anchorPoints, colorWeights, s, temp_space);
		for (int m=0; m<nChannels; m++)
		{
			if (m!=targetChannel)
			{
				applyregisterSlice(imp, width, height,
						transformation, globalTransform, anchorPoints, colorWeights, s-targetChannel+m, temp_space);
			}
		}
		String towrite;
		towrite="";
		for (int m=0; m<3; m++)
		{
			for (int n=0; n<3; n++)
			{
				towrite=towrite+Double.toString(globalTransform[m][n])+"\n";
			}
		}
		try {
			outputStream.write(towrite);
		}
		catch (IOException t){}
		if (source == null) {
			imp.setSlice(targetSlice);
			return;
		}
	}
	imp.setSlice(targetSlice);
	imp.updateAndDraw();
	
	if (outputStream!=null) {
		try {
			outputStream.close();
		}
		catch (IOException b){}
		}

} /* end run */


/*------------------------------------------------------------------*/
private ImagePlus getGray32 (
	final String title,
	final ImagePlus imp,
	final double[] colorWeights
) {
	final int length = imp.getWidth() * imp.getHeight();
	final ImagePlus gray32 = new ImagePlus(title,
		new FloatProcessor(imp.getWidth(), imp.getHeight()));
	final float[] gray = (float[])gray32.getProcessor().getPixels();
	double r;
	double g;
	double b;
	if (imp.getProcessor().getPixels() instanceof byte[]) {
		final byte[] pixels = (byte[])imp.getProcessor().getPixels();
		final IndexColorModel icm = (IndexColorModel)imp.getProcessor().getColorModel();
		final int mapSize = icm.getMapSize();
		final byte[] reds = new byte[mapSize];
		final byte[] greens = new byte[mapSize];
		final byte[] blues = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		int index;
		for (int k = 0; (k < length); k++) {
			index = (int)(pixels[k] & 0xFF);
			r = (double)(reds[index] & 0xFF);
			g = (double)(greens[index] & 0xFF);
			b = (double)(blues[index] & 0xFF);
			gray[k] = (float)(colorWeights[0] * r + colorWeights[1] * g + colorWeights[2] * b);
		}
	}
	else if (imp.getProcessor().getPixels() instanceof int[]) {
		final int[] pixels = (int[])imp.getProcessor().getPixels();
		for (int k = 0; (k < length); k++) {
			r = (double)((pixels[k] & 0x00FF0000) >>> 16);
			g = (double)((pixels[k] & 0x0000FF00) >>> 8);
			b = (double)(pixels[k] & 0x000000FF);
			gray[k] = (float)(colorWeights[0] * r + colorWeights[1] * g + colorWeights[2] * b);
		}
	}
	return(gray32);
} /* getGray32 */


/*------------------------------------------------------------------*/
private double[] getLuminanceFromCCIR601 (
) {
	double[] weights = {0.299, 0.587, 0.114};
	return(weights);
} /* getLuminanceFromCCIR601 */

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
private ImagePlus registerSlice (
	ImagePlus source,
	final ImagePlus target,
	final ImagePlus imp,
	final int width,
	final int height,
	final int transformation,
	final double[][] globalTransform,
	final double[][] anchorPoints,
	final double[] colorWeights,
	final int s,
	final String temp_path
) {
	imp.setSlice(s);
	try {
		Object turboReg = null;
		Method method = null;
		double[][] sourcePoints = null;
		double[][] targetPoints = null;
		double[][] localTransform = null;
		switch (imp.getType()) {
			case ImagePlus.COLOR_256:
			case ImagePlus.COLOR_RGB: {
				source = getGray32("StackRegSource", imp, colorWeights);
				break;
			}
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
		final String sourcePathAndFileName = temp_path + source.getTitle();
		sourceFile.saveAsTiff(sourcePathAndFileName);
		final FileSaver targetFile = new FileSaver(target);
		final String targetPathAndFileName = temp_path + target.getTitle();
		targetFile.saveAsTiff(targetPathAndFileName);
		switch (transformation) {
			case 0: {
				turboReg = IJ.runPlugIn("TurboReg_", "-align"
					+ " -file " + sourcePathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -file " + targetPathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -translation"
					+ " " + (width / 2) + " " + (height / 2)
					+ " " + (width / 2) + " " + (height / 2)
					+ " -hideOutput"
				);
				break;
			}
			case 1: {
				turboReg = IJ.runPlugIn("TurboReg_", "-align"
					+ " -file " + sourcePathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -file " + targetPathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -rigidBody"
					+ " " + (width / 2) + " " + (height / 2)
					+ " " + (width / 2) + " " + (height / 2)
					+ " " + (width / 2) + " " + (height / 4)
					+ " " + (width / 2) + " " + (height / 4)
					+ " " + (width / 2) + " " + ((3 * height) / 4)
					+ " " + (width / 2) + " " + ((3 * height) / 4)
					+ " -hideOutput"
				);
				break;
			}
			case 2: {
				turboReg = IJ.runPlugIn("TurboReg_", "-align"
					+ " -file " + sourcePathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -file " + targetPathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -scaledRotation"
					+ " " + (width / 4) + " " + (height / 2)
					+ " " + (width / 4) + " " + (height / 2)
					+ " " + ((3 * width) / 4) + " " + (height / 2)
					+ " " + ((3 * width) / 4) + " " + (height / 2)
					+ " -hideOutput"
				);
				break;
			}
			case 3: {
				turboReg = IJ.runPlugIn("TurboReg_", "-align"
					+ " -file " + sourcePathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -file " + targetPathAndFileName
					+ " 0 0 " + (width - 1) + " " + (height - 1)
					+ " -affine"
					+ " " + (width / 2) + " " + (height / 4)
					+ " " + (width / 2) + " " + (height / 4)
					+ " " + (width / 4) + " " + ((3 * height) / 4)
					+ " " + (width / 4) + " " + ((3 * height) / 4)
					+ " " + ((3 * width) / 4) + " " + ((3 * height) / 4)
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
		target.setProcessor(null, source.getProcessor());
		method = turboReg.getClass().getMethod("getSourcePoints", null);
		sourcePoints = ((double[][])method.invoke(turboReg, null));
		method = turboReg.getClass().getMethod("getTargetPoints", null);
		targetPoints = ((double[][])method.invoke(turboReg, null));
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

private ImagePlus applyregisterSlice (
		final ImagePlus imp,
		final int width,
		final int height,
		final int transformation,
		final double[][] globalTransform,
		final double[][] anchorPoints,
		final double[] colorWeights,
		final int s,
		final String temp_path
	) {
		imp.setSlice(s);
		ImagePlus source;
		try {
			Object turboReg = null;
			Method method = null;
			double[][] sourcePoints = null;
			double[][] targetPoints = null;
			double[][] localTransform = null;
			switch (imp.getType()) {
				case ImagePlus.COLOR_256:
				case ImagePlus.COLOR_RGB: {
					source = getGray32("StackRegSource", imp, colorWeights);
					break;
				}
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
			final String sourcePathAndFileName = temp_path + source.getTitle();
			sourceFile.saveAsTiff(sourcePathAndFileName);
			//final FileSaver targetFile = new FileSaver(target);
			//final String targetPathAndFileName = IJ.getDirectory("temp") + target.getTitle();
			//targetFile.saveAsTiff(targetPathAndFileName);

			
			
			
			
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

} /* end class StackReg_ */



/*====================================================================
|	stackRegCredits
\===================================================================*/

/********************************************************************/
class stackRegCredits
	extends
		Dialog

{ /* begin class stackRegCredits */

/*....................................................................
	Public methods
....................................................................*/

/********************************************************************/
public Insets getInsets (
) {
	return(new Insets(0, 20, 20, 20));
} /* end getInsets */

/********************************************************************/
public stackRegCredits (
	final Frame parentWindow
) {
	super(parentWindow, "StackReg", true);
	setLayout(new BorderLayout(0, 20));
	final Label separation = new Label("");
	final Panel buttonPanel = new Panel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	final Button doneButton = new Button("Done");
	doneButton.addActionListener(
		new ActionListener (
		) {
			public void actionPerformed (
				final ActionEvent ae
			) {
				if (ae.getActionCommand().equals("Done")) {
					dispose();
				}
			}
		}
	);
	buttonPanel.add(doneButton);
	final TextArea text = new TextArea(25, 56);
	text.setEditable(false);
	text.append("\n");
	text.append(" This work is based on the following paper:\n");
	text.append("\n");
	text.append(" P. Th" + (char)233 + "venaz, U.E. Ruttimann, M. Unser\n");
	text.append(" A Pyramid Approach to Subpixel Registration Based on Intensity\n");
	text.append(" IEEE Transactions on Image Processing\n");
	text.append(" vol. 7, no. 1, pp. 27-41, January 1998.\n");
	text.append("\n");
	text.append(" This paper is available on-line at\n");
	text.append(" http://bigwww.epfl.ch/publications/thevenaz9801.html\n");
	text.append("\n");
	text.append(" Other relevant on-line publications are available at\n");
	text.append(" http://bigwww.epfl.ch/publications/\n");
	text.append("\n");
	text.append(" Additional help available at\n");
	text.append(" http://bigwww.epfl.ch/thevenaz/stackreg/\n");
	text.append("\n");
	text.append(" Ancillary TurboReg_ plugin available at\n");
	text.append(" http://bigwww.epfl.ch/thevenaz/turboreg/\n");
	text.append("\n");
	text.append(" You'll be free to use this software for research purposes, but\n");
	text.append(" you should not redistribute it without our consent. In addition,\n");
	text.append(" we expect you to include a citation or acknowledgment whenever\n");
	text.append(" you present or publish results that are based on it.\n");
	add("North", separation);
	add("Center", text);
	add("South", buttonPanel);
	pack();
} /* end stackRegCredits */

} /* end class stackRegCredits */


