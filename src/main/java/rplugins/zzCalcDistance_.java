

public class CalcDistance_ implements PlugIn {

	public void run(String arg) {
		IJ.showMessage("CalcDistance_","Hello world!");
	}

}









package Chooser;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;


public class CalcDistance_ implements Plugin {

	
	float[] CellX;
	float[] CellY;
	float[] FidX;
	float[] FidY;
	int numCells = 0;
	int numFid = 0;
	Double[] Distance;
	Double[] nearFidX;
	Double[] nearFidY;

	public CalcDistance(){
		
	}
	
	public void doCalcDist(String CellsFile, String FidFile, String Dir, String SaveName) throws Exception{

        
		//Open the cells File
		File Cells = new File(CellsFile);
		if (!Cells.exists() || !Cells.canRead()){
			System.out.println("Cannot read CellsFile. It did not work.");
		}

		
		// Open the Route File
		File Fid = new File(FidFile);
		if (!Fid.exists() || !Fid.canRead()){
			System.out.println("Cannot read FidFile. It did not work.");
		}
		
		numCells = 0;
		//Read the Cells File
		try{
			FileReader fr1 = new FileReader (Cells);		//Open CellsFile to see how many cells there are
			BufferedReader in = new BufferedReader(fr1);			
			
			while ((in.readLine()) != null){
				numCells = numCells+1;
			}
			fr1.close();
			in.close();										//Close the Cells file
			
			String[] CellLine = new String[numCells];			//Create a new array for the Cells now that we know the number of cells to put into the array
			
			FileReader fr2 = new FileReader (Cells);		//Open the Cells file again and dump all data into the new array
			BufferedReader CellsIn = new BufferedReader(fr2);
			int n=0;  //initialize counter: first line is line 0
			while (n<numCells){
				CellLine[n] = CellsIn.readLine();
				//System.out.println(CellLine[n]);			
				n=n+1;
			}
			fr2.close();
			CellsIn.close();
			
			//Get x coord and y coord from the CellLine string
			int m = 0;
			CellX=new float[numCells];
			CellY=new float[numCells];
			while(m < numCells){
				String[] CellXY = CellLine[m].split(",");				
				CellX[m] = Float.parseFloat(CellXY[0]);
				CellY[m] = Float.parseFloat(CellXY[1]);
				System.out.println("Cell ("+CellX[m]+", "+CellY[m]+")");
				m=m+1;
			}
			System.out.println("point0");
		}
		catch (Exception e){
			System.out.println("Exception Occurred.");
		}

		numFid = 0;
		//Count lines in the Fiducial file
		try{
			System.out.println("point1");
			FileReader fr = new FileReader (Fid);
			BufferedReader FidIn = new BufferedReader(fr);
			System.out.println("point2");
			while ((FidIn.readLine()) != null){
				numFid = numFid +1;
			}
			fr.close();
			FidIn.close();
			System.out.println("point3");
		}
		catch (Exception e){
			System.out.println("File not found.");
		}

		//Read the Fiducial File
		try{
			FileReader fr = new FileReader (Fid);
			BufferedReader FidIn = new BufferedReader(fr);
			int n=0;
			String[] BoundLine = new String[numFid];			
			while (n < numFid){
				BoundLine[n] = FidIn.readLine();
				//System.out.println(BoundLine[n]);
				n = n+1;
			}	
			fr.close();
			FidIn.close();
			
				//Get x coord and y coord from the CellLine string
				int m = 0;
				FidX=new float[numFid];
				FidY=new float[numFid];
				while(m < numFid){		   //ERROR -- Changed from numCells to numFid here
					String[] FidXY = BoundLine[m].split(",");
					FidX[m] = Float.parseFloat(FidXY[0]);
					FidY[m] = Float.parseFloat(FidXY[1]);
					System.out.println("Bound ("+FidX[m]+", "+FidY[m]+")");
					m=m+1;					
				}
		}
		catch (Exception e){
			System.out.println("Exception Occurred.");
		}
		
		//Now we have 4 arrays... X values for Bound / Y values for Bound / X values for Cells / Y Values for Cells
		//Calculate the smallest distance between each and save that to a file
		int n = 0;
		int m = 0;
		Distance = new Double[numCells];
		nearFidX = new Double[numCells];
		nearFidY = new Double[numCells];
		while(n < numCells){
			while(m < numFid){
				float x = CellX[n] - FidX[m];
				float y = CellY[n] - FidY[m];
				Double Dist = Math.hypot(x,y);
				if (Distance[n] == null){
					//Was used to determine whether cell is left or right of Fiducial Mark
					/*
					if(x <= 0){
						Distance[n] = -Dist;
					}else{
						Distance[n] = Dist;
					}
					*/
					Distance[n] = Dist;
					nearFidX[n] = (double)FidX[m];
					nearFidY[n] = (double)FidY[m];
					System.out.println("Distance = " + Distance[n]);
					System.out.println("NearFid = (" + nearFidX[n] + ", " + nearFidY[n] + ")");
				}else{
					if (Dist < Math.abs(Distance[n])){
						//Was used to determine whether cell is left or right of Fiducial Mark
						/*
						if(x <= 0){
							Distance[n] = -Dist;
						}else{
							Distance[n] = Dist;
						}
						*/
						Distance[n] = Dist;
						nearFidX[n] = (double)FidX[m];
						nearFidY[n] = (double)FidY[m];
						System.out.println("Distance = " + Distance[n]);
						System.out.println("NearFid = (" + nearFidX[n] + ", " + nearFidY[n] + ")");
					}
				}
				//System.out.println("Distance " + n + " = " +Distance[n]);
				m=m+1;
			}
			m=0;
			n=n+1;
		}
		
		try{
			
			String SavePath = Dir + "\\DistCalc_" + SaveName + ".csv";
			System.out.println("Try to write:   " + SavePath);
			File Dist1 = new File(SavePath);		//Create new Distance File
			FileWriter Dist = new FileWriter (Dist1);		//Open writer stream
		
			Dist.write("X Position,Y Position,Distance,nearFidX,nearFidY"+"\n");
			n = 0;
			while(n < numCells){
				System.out.println(CellX[n] + "," + CellY[n] + "," + Distance[n] + "," + nearFidX[n] + "," + nearFidY[n] + "\n");
				Dist.write(CellX[n] + "," + CellY[n] + "," + Distance[n] + "," + nearFidX[n] + "," + nearFidY[n] + "\n");
				n++;
			}
			Dist.close();
		}catch(Exception e){
			System.out.println("Cannot write to File... Or other Exception Occurred");
		}
		
		//return;
	}

}
