import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import java.io.*;
import java.lang.Math;

public class Cell_Distance implements PlugIn {

/*
//NEW SECTION BEGINS HERE
	float[] x;
	float[] y;
	ij.getSelectionCoordinates(x, y);
	for (i=0; i<x.length; i++){
		IJ.print(x[i]+","+y[i]);
	}
	ij.selectWindow("Log");
	ij.saveAs("txt","BoundaryFile");
	ij.//close();
//NEW SECTION ENDS HERE
*/
	
	float[] CellX;
	float[] CellY;
	float[] BoundX;
	float[] BoundY;
	int numCells = 0;
	int numBounds = 0;
	Double[] Distance;
	Double[] nearBoundX;
	Double[] nearBoundY;
	
	public void run(String arg){
//	public static void main(String[] args) throws Exception{
		



		//Open the cells File
		File Cells = new File("CellsFile.txt");
		if (!Cells.exists() || !Cells.canRead()){
			//System.out.println("Cannot read CellsFile. It did not work.");
			IJ.showMessage("Cannot read CellsFile. It did not work.");
		}
		
		// Open the Boundary File
		File Boundary = new File("BoundaryFile.txt");
		if (!Boundary.exists() || !Boundary.canRead()){
			//System.out.println("Cannot read BoundaryFile. It did not work.");
			IJ.showMessage("Cannot read BoundaryFile. It did not work.");
		}
		
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
				m=m+1;
			}
		}
		//catch (FileNotFoundException e){
		catch (IOException e){
			//System.out.println("File not found.");
			IJ.showMessage("File not found.");
		}

		//Count lines in the boundary file
		try{
			FileReader fr = new FileReader (Boundary);
			BufferedReader BoundaryIn = new BufferedReader(fr);
			while ((BoundaryIn.readLine()) != null){
				numBounds = numBounds +1;
			}
			fr.close();
			BoundaryIn.close();
		}
		//catch (FileNotFoundException e){
		catch (IOException e){
			//System.out.println("File not found.");
			IJ.showMessage("File not found.");
		}

		//Read the Boundary File
		try{
			FileReader fr = new FileReader (Boundary);
			BufferedReader BoundaryIn = new BufferedReader(fr);
			int n=0;
			String[] BoundLine = new String[numBounds];			
			while (n < numBounds){
				BoundLine[n] = BoundaryIn.readLine();
				//System.out.println(BoundLine[n]);
				n = n+1;
			}	
			fr.close();
			BoundaryIn.close();
				
				//Get x coord and y coord from the CellLine string
				int m = 0;
				BoundX=new float[numBounds];
				BoundY=new float[numBounds];
				while(m < numCells){
					String[] BoundXY = BoundLine[m].split(",");
					BoundX[m] = Float.parseFloat(BoundXY[0]);
					BoundY[m] = Float.parseFloat(BoundXY[1]);
					m=m+1;
				}
		}
		//catch (FileNotFoundException e){
		catch (IOException e){
			//System.out.println("File not found.");
			IJ.showMessage("File not found.");
		}
		
		//Now we have 4 arrays... X values for Bound / Y values for Bound / X values for Cells / Y Values for Cells
		//Calculate the smallest distance between each and save that to a file
		int n = 0;
		int m = 0;
		Distance = new Double[numCells];
		nearBoundX = new Double[numCells];
		nearBoundY = new Double[numCells];
		while(n < numCells){
			while(m < numBounds){
				float x = CellX[n] - BoundX[m];
				float y = CellY[n] - BoundY[m];
				Double Dist = Math.hypot(x,y);
				if (Distance[n] == null){
					Distance[n] = Dist;
					nearBoundX[n] = (double)BoundX[m];
					nearBoundY[n] = (double)BoundY[m];
				}else{
					if (Dist < Distance[n]){
						Distance[n] = Dist;
						nearBoundX[n] = (double)BoundX[m];
						nearBoundY[n] = (double)BoundY[m];
					}
				}
				//System.out.println("Distance " + n + " = " +Distance[n]);
				m=m+1;
			}
			m=0;
			n=n+1;
		}
		
		try{
			File Dist1 = new File("DistanceFile.csv");		//Create new Distance File
			FileWriter Dist = new FileWriter (Dist1);		//Open writer stream
		
			Dist.write("X Position,Y Position,Distance,nearBoundX,nearBoundY"+"\n");
			n = 0;
			while(n < numCells){
				//System.out.println(CellX[n] + "," + CellY[n] + "," + Distance[n] + "," + nearBoundX[n] + "," + nearBoundY[n] + "\n");
				Dist.write(CellX[n] + "," + CellY[n] + "," + Distance[n] + "," + nearBoundX[n] + "," + nearBoundY[n] + "\n");
				n++;
			}
			IJ.showMessage("File was written.");
			Dist.close();
		}catch(IOException e){
			//System.out.println("Cannot write to 'DistanceFile.csv'.");
			IJ.showMessage("Cannot write to 'DistanceFile.csv'.");
		}
		
		return;
	}

//}
}
