package splugins;

import java.awt.event.KeyEvent;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.Zoom;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import ij.ImageListener;

public class Classify_Individual_Files_Others implements PlugIn, KeyListener, ImageListener {

	ResultsTable rslt;
	int current_idx;
	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int counts;
	@Override
	public void run(String arg) {
		
		rslt=ResultsTable.getResultsTable();
		current_idx = -1;
		go_to_next_other();
        go_to();
	}
	public void go_to_next_other()
	{
		rslt=ResultsTable.getResultsTable();
		
        for (int i=current_idx+1; i<rslt.getCounter(); i++)
        {
        	String tmp = rslt.getStringValue(0, i);
        	IJ.log(tmp);
        	if (tmp.contains("Other"))
        	{
        		current_idx=i;
        		break;
        	}
        }
        IJ.log("Position: "+current_idx);
	}
	public void go_to_previous_other()
	{
		rslt=ResultsTable.getResultsTable();
		
        for (int i=current_idx-1; i>-1; i--)
        {
        	String tmp = rslt.getStringValue(0, i);
        	if (tmp.contains("Other"))
        	{
        		current_idx=i;
        		break;
        	}
        }
	}
	public void go_to_close()
	{
		IJ.log(rslt.getStringValue("Type", current_idx-1));
		myimg.close();
		go_to();
	}
	public void go_to()
	{
		//rslt.updateResults();
		rslt.show("Results");
		String filename= rslt.getStringValue("File", current_idx);
    	//IJ.open("S:\\micro\\asa\\stn\\mel\\20-15_Irradiation_Datasets\\20210721_MER_IMARE-106746_20_15_1A_control\\20-15-1A_after_crash\\cjw\\DeepLearn\\Classifier\\Test\\0000_control.tif");
		IJ.open(filename);
    	myimg = IJ.getImage();
    	myimg.setTitle("Nucleus");
        win = myimg.getWindow();
        canvas = win.getCanvas();
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addKeyListener(this);
        canvas.addKeyListener(this);
        ImagePlus.addImageListener(this);
        int nSlices = myimg.getNSlices();
        IJ.run(myimg, "Grays", "");
        IJ.run("In [+]", "");
        IJ.run("In [+]", "");
        IJ.run("In [+]", "");
        IJ.run("In [+]", "");
        //IJ.run("Channels Tool...");
        myimg.setDisplayMode(IJ.COMPOSITE);
        myimg.setActiveChannels("10");
        myimg.setSlice((int)(Math.floor(nSlices/2)));
        myimg.updateAndDraw();
        myimg.show();
        myimg.setPosition(0, (int)(Math.floor(nSlices/2)), 0);
				
	}
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='q') 
        {
        	
        }

        if (keyChar=='c')
        {
        	rslt.setValue(0, current_idx, "Chromatoid");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='p')
        {
        	rslt.setValue(0, current_idx, "Proximal");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='i')
        {
        	rslt.setValue(0, current_idx, "Distal");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='r')
        {
        	rslt.setValue(0, current_idx, "Progenitor");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='z')
        {
        	rslt.setValue(0, current_idx, "ChromatoidDoublet");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='s')
        {
        	rslt.setValue(0, current_idx, "Secretory");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='e')
        {
        	rslt.setValue(0, current_idx, "Epithelial");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='j')
        {
        	rslt.setValue(0, current_idx, "Junk");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='d')
        {
        	rslt.setValue(0, current_idx, "Doublet");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='g')
        {
        	rslt.setValue(0, current_idx, "Gut");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='n')
        {
        	rslt.setValue(0, current_idx, "Neuronal");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='m')
        {
        	rslt.setValue(0, current_idx, "Muscle");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }
        if (keyChar=='o')
        {
        	rslt.setValue(0, current_idx, "Unknown");
        	//current_idx=current_idx+1;
        	go_to_next_other();
        	go_to_close();
        }        
        if (keyChar=='b')
        {
        	current_idx=current_idx-1;
        	//go_to_previous_other();
        	go_to_close();
        }


	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void imageOpened(ImagePlus imp) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void imageClosed(ImagePlus imp) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void imageUpdated(ImagePlus imp) {
		// TODO Auto-generated method stub
		
	}

}
