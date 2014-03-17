package splugins;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.io.FileInfo;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Filter_Colocalization_Candidates implements PlugIn, KeyListener, ImageListener, MouseWheelListener {

	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int counts;
    int Nframes;
    int Nchannels;
    int Nslices;
    int height;
    int width;
    boolean [] hit_list;
    boolean s_down;
    boolean just_went;
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='e') 
        {
        	if (myimg.getFrame()<myimg.getNFrames()) myimg.setT(myimg.getFrame()+1);
        	/*float pix[]=(float [])myimg.getProcessor().getPixels();
        	float pix2[]=(float [])myimg.getStack().getProcessor(myimg.getSlice()+1).getPixels();
        	float max=0, min=1000000;
        	float max2=0, min2=1000000;
        	for (int i=0; i<pix.length; i++)
        	{
        		if (pix[i]>max) max=pix[i];
        		if (pix[i]<min) min=pix[i];
        		if (pix2[i]>max2) max2=pix2[i];
        		if (pix2[i]<min2) min2=pix2[i];
        	}
        	myimg.setC(2);
        	myimg.setDisplayRange(min2, min2+(max2-min2)/.00000000001);
        	myimg.setC(1);
        	myimg.setDisplayRange(min, min+(max-min)/.5);
        	myimg.updateAndDraw();*/
        	
        	//win.getImagePlus().setSlice(win.getImagePlus().getSlice()+1);
        }
        if (keyChar=='r')
        {
        	if (myimg.getFrame()>1) myimg.setT(myimg.getFrame()-1);
        	/*float pix[]=(float [])myimg.getProcessor().getPixels();
        	float pix2[]=(float [])myimg.getStack().getProcessor(myimg.getSlice()+1).getPixels();
        	float max=0, min=1000000;
        	float max2=0, min2=1000000;
        	for (int i=0; i<pix.length; i++)
        	{
        		if (pix[i]>max) max=pix[i];
        		if (pix[i]<min) min=pix[i];
        		if (pix2[i]>max2) max2=pix2[i];
        		if (pix2[i]<min2) min2=pix2[i];
        	}
        	myimg.setC(2);
        	myimg.setDisplayRange(min2, min2+(max2-min2)/1.0);
        	myimg.setC(1);
        	myimg.setDisplayRange(min, min+(max-min)/1.0);
        	myimg.updateAndDraw();*/
        	//win.getImagePlus().setSlice(win.getImagePlus().getSlice()-1);
        }
        if (keyChar=='p')
        {
        	//win.getImagePlus().getProcessor().invert();
        	//win.getImagePlus().updateAndDraw();
        	/*for (int i=0; i<Nslices; i++)
        	{
        		myimg.getStack().getProcessor(1+i*Nchannels+(myimg.getT()-1)*Nchannels*Nslices+1).invert();
        	}
        	
        	myimg.updateAndDraw();
        	hit_list[myimg.getFrame()]=true;*/
        }
        if (keyChar=='d')
        {
        	//win.getImagePlus().getProcessor().invert();
        	//win.getImagePlus().updateAndDraw();
        	for (int i=0; i<Nslices; i++)
        	{
        		myimg.getStack().getProcessor(1+i*Nchannels+(myimg.getT()-1)*Nchannels*Nslices+1).invert();
        	}
        	myimg.updateAndDraw();
        	hit_list[myimg.getFrame()-1]=!hit_list[myimg.getFrame()-1];
        }
        if (keyChar=='f'&&!s_down)
        {
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)/1.5);
        	myimg.updateAndDraw();
        	//s_down=true;
        }
        if (keyChar=='a')
        {
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)*1.5);
        	myimg.updateAndDraw();
        }
        if (keyChar=='e'&&!s_down)
        {
        	myimg.setC(2);
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)/2.0);
        	myimg.updateAndDraw();
        	myimg.setC(1);
        	//s_down=true;
        }
        if (keyChar=='c')
        {
        	myimg.setC(2);
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)*2.0);
        	myimg.updateAndDraw();
        	myimg.setC(1);
        }
        if (keyChar=='q')
        {
        	int tmp=0;
        	for (int i=0; i<hit_list.length; i++)
        	{
        		if (hit_list[i]) tmp++;
        	}
        	IJ.log("" + tmp);
        	cleanup();
        	
        	FileInfo info=myimg.getOriginalFileInfo();
        	String title=myimg.getTitle();
        	String sub=title.substring(title.indexOf("_")+1, title.indexOf("_", title.indexOf("_")+1));
        	
        	ResultsTable the_table=ResultsTable.getResultsTable();
        	the_table.incrementCounter();
        	the_table.addValue("Directory",info.directory );
        	the_table.addValue("File",  info.fileName);
			the_table.addValue("Blobs", Integer.parseInt(sub, 10));
			the_table.addValue("Colocalized", tmp);
			the_table.show("Results");
			
			
			
        }

	}
	public void mouseWheelMoved(MouseWheelEvent e) {
		myimg.setC(1);
			if (just_went)
			{
				just_went=false;
				return;
			}
			just_went=true;
	       String message;
	       
	       int notches = e.getWheelRotation();
	       if (notches < 0) {
	    	   
	    	   if (myimg.getFrame()>1) myimg.setT(myimg.getFrame()-1);
	    	   
	    	   /*double min, max;
	        	min=myimg.getDisplayRangeMin();
	        	max=myimg.getDisplayRangeMax();
	        	myimg.setDisplayRange(min, min+(max-min)/1.5);
	        	myimg.updateAndDraw();*/
	        	
	       } else {
	    	   if (myimg.getFrame()<myimg.getNFrames()) myimg.setT(myimg.getFrame()+1);
	    	   /*
	    	   double min, max;
	        	min=myimg.getDisplayRangeMin();
	        	max=myimg.getDisplayRangeMax();
	        	myimg.setDisplayRange(min, min+(max-min)*1.5);
	        	myimg.updateAndDraw();*/
	       }
	       
	    }

	@Override
	public void keyReleased(KeyEvent e) {
		/*int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='s') 
        {
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)*2.0);
        	myimg.updateAndDraw();
        	s_down=false;
        }*/

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(String arg0) {
		myimg = IJ.getImage();
        win = myimg.getWindow();
        canvas = win.getCanvas();
        win.removeKeyListener(IJ.getInstance());
        canvas.removeKeyListener(IJ.getInstance());
        win.addKeyListener(this);
        canvas.addKeyListener(this);
        win.addMouseWheelListener(this);
        canvas.addMouseWheelListener(this);
        //ImagePlus.addImageListener(this);
        counts=0;
        hit_list=new boolean[myimg.getNFrames()];
        
        width=myimg.getWidth();
        height=myimg.getHeight();
        Nslices=myimg.getNSlices();
        Nframes=myimg.getNFrames();
        Nchannels=myimg.getNChannels();

	}
	
	public void imageClosed(ImagePlus imp) {
        cleanup();
    }
	
	public void cleanup()
	{
		if (win!=null)
		{
			win.removeKeyListener(this);
			win.removeMouseWheelListener(this);
		}
		
        if (canvas!=null)
        {
        	canvas.removeKeyListener(this);
        	canvas.removeMouseWheelListener(this);
        }
        
        
        //ImagePlus.removeImageListener(this);
	}
	
	public void imageOpened(ImagePlus imp) {}
    public void imageUpdated(ImagePlus imp) {}

}
