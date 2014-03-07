package rplugins;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import java.util.*;
import java.lang.*;

public class MSdots_CtS_auto_3colorMix implements PlugIn {
	//constants for random number generator:
	final int IA = 16807;
	final int IM = 2147483647;
	final double AM = (1.0/(double)IM);
	final int IQ = 127773;
	final int IR = 2836;
	final int MASK = 123459876;
	int seed;

	public void run(String arg) {
		Thread thisThread = Thread.currentThread();

		GenericDialog gd = new GenericDialog("Options");
		seed=1;
		gd.addNumericField("Seed",seed,0);
		boolean randomize=false;
		gd.addCheckbox("randomize seed",randomize);
		int dwidth=13;
		gd.addNumericField("Dot width",dwidth,0);
		int dheight=5;
		gd.addNumericField("Dot height",dheight,0);
		int ndots=550;
		gd.addNumericField("Number of Dots",ndots,0);
		int width=70;
		gd.addNumericField("Image width",width,0);
		int height=100;
		gd.addNumericField("Image height",height,0);
		gd.showDialog();
		if(gd.wasCanceled()){return;}
		seed=(int)gd.getNextNumber();
		randomize=gd.getNextBoolean();
		dwidth=(int)gd.getNextNumber();
		dheight=(int)gd.getNextNumber();
		ndots=(int)gd.getNextNumber();
		width=(int)gd.getNextNumber();
		height=(int)gd.getNextNumber();
		if(randomize){
			seed=(int)((double)System.currentTimeMillis()/1000.0);
		}
		float[] pixels1=new float[width*height];
		float[] pixels2=new float[width*height];
		float[] pixels3=new float[width*height];
		float[] pixels4=new float[width*height];

		float[] storepix=new float[width*height];
		int x;
		int y;
		int center;
		int loop = 0;
		int cellpix = 0;
		int halfw = (dwidth / 2);
		int halfh = (dheight / 2);
			//IJ.log("halfw =   "+halfw + "   halfh =    " + halfh);

///////////////New section for loop

int numR =0;
int numG =0;
int numB =0;
int numY =0;
int Rcell = 0;
int Gcell = 0;
int Bcell = 0;
int Ycell=0;

while(numR<10){
	int percR = (numR * 10) +10;
	numG = 0;
	numB = 0;
	numY=0;
	while(numG<10){
		int percG = (numG * 10) +10;
		numB = 0;
		numY=0;
		while(numB<10){	
			int percB = (numB * 10) +10;
			numY = 0;
//			while(numY<5){	
//				int percY = (numY * 10) +10;

			//keep duplicates, but put 3 loops above (one for each color).
			int duplicates= 0;
			while(duplicates<10){  //back to 10
				int countR = 0;
				int countG = 0;
				int countB = 0;
				int countY = 0;
				int numCells = 0;
///////////////End new section for loop

		for(int i=0;i<ndots;i++){
			if(numR==percR && numG==percG && numB==percB){	break;	}
			int m = 0;
			search:
			do{
				x=(int)((double)(width-1)*ran0());
				y=(int)((double)(height-1)*ran0());
				center = x + halfw+(y+halfh)*width;
				if(center > (width-1)*(height-1)){center = -1;}
				m++;
				if(m > 1000 &&  x < width - dwidth){	break search;	};
			}while(center == -1 || storepix[center] > 0.0f   ||    x > width - dwidth);

			loop +=1;
			
			//Use "countR" variable to track how many red cells have been placed
			//Use "numCells" variable to count the TOTAL cells that have been placed
			//Use "Rcell" variable to know when to increment "numCells" since a cell can be more than one color

			int num1 = (int)(10*ran0());
				if(num1<=numR && countR <= percR){
					countR++;
				}
			int num2 = (int)(10*ran0());
				if(num2<=numG && countG <= percG){
					countG++;
				}
			int num3 = (int)(10*ran0());
				if(num3<=numB && countB <= percB){
					countB++;
				}
//			int num4 = (int)(10*ran0());
//				if(num4<=numY && countY <= percY){
//					countY++;
//				}

			Rcell = 0;	
			Gcell = 0;
			Bcell = 0;
			Ycell=0;
			
			for(int j=0;j<dwidth;j++){
				for(int k=0;k<dheight;k++){
					
					int dumval=x+y*width+j+k*width;
					if(dumval<width*height){
						cellpix += 1;
						storepix[dumval] = 1.0f;

							//NOTE:::> Remember to change "num" above if changing the color distrubution
						
						
						//NEW RANDOM COMBINATIONS WITH 3 LOOPS (ONE FOR EACH COLOR)
						if(num1<= numR && countR <= percR){
							pixels1[dumval]=1.0f;
							if(j==0&&k==0){
								numCells++;
								Rcell=1;
							}
						}
						if(num2<=numG && countG <=percG){
							pixels2[dumval]=1.0f;
							if(j==0&&k==0&&Rcell==0){
								Gcell = 1;
								numCells++;
							}
						}
						if(num3<=numB && countB <= percB){
							pixels3[dumval]=1.0f;
							if(j==0&&k==0&&Rcell==0&&Gcell==0){
								Bcell = 1;
								numCells++;
							}
						}
//						if(num4<=numY && countY <= percY){
//							pixels4[dumval]=1.0f;
//							if(j==0&&k==0&&Rcell==0 && Gcell==0 && Bcell==0){
//								Ycell = 1;
//								numCells++;
//							}
//						}

						////////////////////////////////////////////////////
					}
				}
			}
		}
		
		//IJ.log("loop =  "+loop);
		//IJ.log("cellpix =    "+cellpix);

		FloatProcessor fp1 = new FloatProcessor(width,height,pixels1,null);
		ImagePlus imp1 = new ImagePlus("Random Dots 1",fp1);
		imp1.show();

		FloatProcessor fp2 = new FloatProcessor(width,height,pixels2,null);
		ImagePlus imp2 = new ImagePlus("Random Dots 2",fp2);
		imp2.show();

		FloatProcessor fp3 = new FloatProcessor(width,height,pixels3,null);
		ImagePlus imp3 = new ImagePlus("Random Dots 3",fp3);
		imp3.show();

//		FloatProcessor fp4 = new FloatProcessor(width,height,pixels4,null);
//		ImagePlus imp4 = new ImagePlus("Random Dots 4",fp4);
//		imp4.show();

///////////New section for loop
////////////////////Need to make stack, save, and close image

IJ.run("MSsim StackSave v2col");

try{thisThread.sleep(101L);
}catch(InterruptedException e){}

int Rcount = countR - 1;
int Gcount = countG-1;
int Bcount = countB-1;
//int Ycount = countY-1;
int CellsNum = numCells;
ImagePlus imp5 = WindowManager.getCurrentImage();
String title = "R_"+Rcount+"_G_"+Gcount+"_B_"+Bcount+"_numCells_"+CellsNum+"__dup_"+duplicates;
imp5.setTitle(title);

String fname = "C:\\Sim\\"+title+".tif";
new FileSaver(imp5).saveAsTiffStack(fname);
IJ.run("MSmacroRLA sim v3col");

imp5.close();

		Arrays.fill(pixels1,0.0f);
		Arrays.fill(pixels2,0.0f);
		Arrays.fill(pixels3,0.0f);
		Arrays.fill(pixels4,0.0f);
		Arrays.fill(storepix,0.0f);

	duplicates+=1;
	}
//	numY++;
//	}
	numB++;
	}
	numG++;
	}
	numR++;
	}
//////////End new section for loop
}
	public double ran0()   //Gives a random number between 0 and 1
	{
	//minimal linear congruential random number generator from numerical recipes in c
		int k;
		double ans;
		seed ^= MASK;
		k=seed/IQ;
		seed=IA*(seed-k*IQ)-IR*k;
		if(seed < 0){seed += IM;}
		ans=AM*seed;
		seed ^= MASK;
		return ans;
	}

}
