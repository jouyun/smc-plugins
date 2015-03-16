package splugins;

import ij.IJ;
import ij.measure.ResultsTable;

import java.util.ArrayList;

public class get_scanner_candidates {
	
	ArrayList <ArrayList <double []>> auto_focus_points;
	ArrayList <ArrayList <double []>> image_points;
	
	public void load_data(double x_start, double y_start, double x_shift, double y_shift)
	{
		auto_focus_points=new ArrayList <ArrayList <double[]>>();
		image_points=new ArrayList <ArrayList <double[]>>();
		ResultsTable rslt;
		rslt=ResultsTable.getResultsTable();
		int i=0;
		int tocollect=rslt.size();
		while (i<tocollect)
		{
			int next_neg=i;
			double x=rslt.getValueAsDouble(rslt.getColumnIndex("X"), next_neg);
			double y=rslt.getValueAsDouble(rslt.getColumnIndex("Y"), next_neg);
			while (x!=-1)
			{
				next_neg++;
				x=rslt.getValueAsDouble(rslt.getColumnIndex("X"), next_neg);
				y=rslt.getValueAsDouble(rslt.getColumnIndex("Y"), next_neg);
			}
			ArrayList<double []> current_autos=new ArrayList<double []>();			
			for (int j=i; j<next_neg; j++)
			{
				x=rslt.getValueAsDouble(rslt.getColumnIndex("X"), j);
				y=rslt.getValueAsDouble(rslt.getColumnIndex("Y"), j);
				double[] new_pos=new double [2];
				new_pos[0]=x_start+x+x_shift;
				new_pos[1]=y_start+y+y_shift;				
				current_autos.add(new_pos);
			}
			auto_focus_points.add(current_autos);
			
			i=next_neg+1;	
			
			//Find imaging positions, add them to the queue
			int next_zer=i;
			x=rslt.getValueAsDouble(rslt.getColumnIndex("X"), next_zer);
			y=rslt.getValueAsDouble(rslt.getColumnIndex("Y"), next_zer);
			ArrayList<double []> current_points=new ArrayList<double []>();
			while (x!=0&&next_zer<tocollect)
			{
				double [] new_pos=new double[2];
				new_pos[0]=x_start+x+x_shift;
				new_pos[1]=y_start+y+y_shift;
				current_points.add(new_pos);
				next_zer++;
				x=rslt.getValueAsDouble(rslt.getColumnIndex("X"), next_zer);
				y=rslt.getValueAsDouble(rslt.getColumnIndex("Y"), next_zer);
			}
			image_points.add(current_points);
			i=next_zer+1;
		}
	}
	public double [][] get_auto_array(int index)
	{
		double [][] rtnval=new double[auto_focus_points.get(index).size()][2];
		ArrayList <double []> current=auto_focus_points.get(index);
		for (int i=0; i<auto_focus_points.get(index).size(); i++)
		{
			rtnval[i][0]=current.get(i)[0];
			rtnval[i][1]=current.get(i)[1];			
		}
		return rtnval;
	}
	public double [][] get_image_array(int index)
	{
		double [][] rtnval=new double[image_points.get(index).size()][2];
		ArrayList <double []> current=image_points.get(index);
		for (int i=0; i<image_points.get(index).size(); i++)
		{
			rtnval[i][0]=current.get(i)[0];
			rtnval[i][1]=current.get(i)[1];			
		}
		return rtnval;
	}
	public int get_size_auto()
	{
		return auto_focus_points.size();
	}
	public int get_size_image()
	{
		return image_points.size();
	}
}
