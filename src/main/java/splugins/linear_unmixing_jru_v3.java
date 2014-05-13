package splugins;
/*******************************************************************************

 * Copyright (c) 2012 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
import ij.*;

import org.apache.commons.math.*;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DecompositionSolver;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
//import org.apache.commons.math3.*;


import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

import ij.process.*;
import ij.gui.*;

import java.awt.*;

import ij.plugin.*;

import java.io.*;

import jguis.*;
import jalgs.*;
import jalgs.jfit.*;

public class linear_unmixing_jru_v3 implements PlugIn {
	int species,mainoptionsindex,totspecies,maxlength;
	boolean subback;
	String[] speciesnames;
	float[][] speciesspectra;
	float[] backgroundspectrum;
	int[] speciesindices;

	public void run(String arg) {
		          
		//this plugin performs linear unmixing and in so doing finds the intensity of multiple components in frequency resolved image
		//the image could be frequency resolved in color or in temporal fourier frequency (lifetime or photoactivation)
		//this version doesn't use the database and allows for selection of the channel region for unmixing
		//include the background as a spectrum if you would like
		ImagePlus[] imps=jutils.selectImages(false,2,new String[]{"Spectral_Image","Spectra_Plot"});
		if(imps==null) return;
		GenericDialog gd2=new GenericDialog("More Options");
		gd2.addCheckbox("Output_Residuals?",false);
		gd2.addCheckbox("Output_chi^2?",false);
		gd2.addCheckbox("Truncate_Negative_Values",true);
		gd2.addCheckbox("Weights",false);
		gd2.addCheckbox("Normalize spectra",true);
		gd2.addNumericField("Start_ch",1,0);
		gd2.addNumericField("End_ch",imps[0].getNChannels(),0);
		gd2.showDialog(); if(gd2.wasCanceled()){return;}
		boolean outres=gd2.getNextBoolean();
		boolean outc2=gd2.getNextBoolean();
		boolean truncate=gd2.getNextBoolean();
		boolean weight=gd2.getNextBoolean();
		boolean normalize=gd2.getNextBoolean();
		int startch=(int)gd2.getNextNumber()-1;
		int endch=(int)gd2.getNextNumber()-1;
		Object[] outimages=exec(imps[0],imps[1].getWindow(),startch,endch,outres,outc2,truncate, weight, normalize);
		for(int i=0;i<outimages.length;i++) ((ImagePlus)outimages[i]).show();
	}

	public Object[] exec(ImagePlus input,ImageWindow plot,int startch,int endch,boolean outresid,boolean outc2,boolean truncneg, boolean weight, boolean normalize){
		float[][] spectra=(float[][])jutils.runPW4VoidMethod(plot,"getYValues");
		if (normalize) for (int i=0; i<spectra.length; i++) Multi_Spectral_Profiler.normalize(spectra[i]);
		//Do decomposition, see http://commons.apache.org/proper/commons-math/userguide/linear.html
		double [][] dspectra=new double[spectra[0].length][spectra.length];
		for (int i=0; i<spectra.length; i++){
			for (int j=0; j<spectra[0].length; j++)
			{
				dspectra[j][i]=spectra[i][j];
			}
		}
		RealMatrix coefficients = new Array2DRowRealMatrix(dspectra, false);
		DecompositionSolver solver = new SingularValueDecompositionImpl(coefficients).getSolver();
		
		int frames=input.getNFrames();
		int slices=input.getNSlices();
		int channels=input.getNChannels();
		int width=input.getWidth(); int height=input.getHeight();
		if(startch<0) startch=0;
		if(endch>=channels) endch=channels-1;
		int fitch=endch-startch+1;
		ImageStack stack=input.getStack();
		//start by creating the linear least squares object
		linleastsquares lls=new linleastsquares(spectra,false,startch,endch);
		//now go through pixel by pixel and do the unmixing
		ImageStack outstack=new ImageStack(width,height);
		ImageStack residstack=new ImageStack(width,height);
		ImageStack c2stack=new ImageStack(width,height);
		int counter=0;
		for(int i=0;i<frames;i++){
			for(int j=0;j<slices;j++){
				Object[] cseries=jutils.get3DCSeries(stack,j,i,frames,slices,channels);
				float[][] contr=new float[spectra.length][width*height];
				float[][] resid=null;
				if(outresid) resid=new float[channels][width*height];
				float[] c2=null;
				if(outc2) c2=new float[width*height];
				for(int k=0;k<width*height;k++){
					float[] col=algutils.convert_arr_float(algutils.get_stack_col(cseries,width,height,k,cseries.length));
					double [] contributions=new double [spectra.length];
					if (!weight)
					{
						//Do matrix multiplication using decomposed results in solver, see above
						double [] dcol=new double[col.length];
						for (int c=0; c<col.length; c++) dcol[c]=col[c];
						RealVector constants = new ArrayRealVector(dcol, false);
						RealVector solution = solver.solve(constants);
						for (int c=0; c<solution.getDimension(); c++) contributions[c]=solution.getEntry(c);
					}
					else
					{
					
						float [] weights=new float[col.length];
						float sum=0.0f;
						for (int c=0; c<col.length; c++) 
						{
							//weights[c]=(float)Math.sqrt(col[c]);
							//weights[c]=1.0f/(1.0f+col[c]);  //CLOSE!
							//weights[c]=1.0f/(1.0f+(float)Math.sqrt(col[c]));  //Closer!
							weights[c]=1.0f/(1.0f+(float)Math.sqrt(Math.sqrt(col[c])));  //Closer!
						//	weights[c]=weights[c]*weights[c];
							sum+=weights[c];
						}
						for (int c=0; c<col.length; c++) weights[c]=weights[c]/sum;
						contributions=lls.fitdata(col,weights);
					}
					
					for(int l=0;l<contributions.length;l++){
						if(truncneg && contributions[l]<0.0f) contributions[l]=0.0f;
						contr[l][k]=(float)contributions[l];
					}
					if(outresid){
						float[] residcol=lls.get_fresid(contributions,col,null);
						for(int l=0;l<channels;l++){resid[l][k]=residcol[l];}
					}
					if(outc2){
						c2[k]=(float)lls.get_c2(contributions,col,null);
					}
				}
				for(int k=0;k<spectra.length;k++) outstack.addSlice("",contr[k]);
				if(outresid) for(int k=0;k<channels;k++) residstack.addSlice("",resid[k]);
				if(outc2) c2stack.addSlice("",c2);
				counter++;
				IJ.showProgress(counter,j+i*slices);
			}
		}
		ImagePlus outimp=new ImagePlus("Unmixed Stack",outstack);
		outimp.copyScale(input);
		outimp.setOpenAsHyperStack(true);
		outimp.setDimensions(spectra.length,slices,frames);
		int nout=1;
		if(outresid) nout++;
		if(outc2) nout++;
		Object[] output=new Object[nout];
		int outcounter=0;
		output[0]=new CompositeImage(outimp,CompositeImage.COLOR); outcounter++;
		if(outresid){
			ImagePlus residimp=new ImagePlus("Residuals Stack",residstack);
			residimp.copyScale(input);
			residimp.setOpenAsHyperStack(true);
			residimp.setDimensions(channels,slices,frames);
			output[outcounter]=new CompositeImage(residimp,CompositeImage.GRAYSCALE);
			outcounter++;
		}
		if(outc2){
			ImagePlus c2imp=new ImagePlus("chi^2",c2stack);
			c2imp.copyScale(input);
			c2imp.setOpenAsHyperStack(true);
			c2imp.setDimensions(1,slices,frames);
			output[outcounter]=c2imp;
		}
		return output;
	}
	

}
