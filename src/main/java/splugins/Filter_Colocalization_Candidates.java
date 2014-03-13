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

public class Filter_Colocalization_Candidates implements PlugIn, KeyListener, ImageListener {

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
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='f') 
        {
        	if (myimg.getFrame()<myimg.getNFrames()) myimg.setT(myimg.getFrame()+1);
        	//win.getImagePlus().setSlice(win.getImagePlus().getSlice()+1);
        }
        if (keyChar=='a')
        {
        	if (myimg.getFrame()>1) myimg.setT(myimg.getFrame()-1);
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
        if (keyChar=='s'&&!s_down)
        {
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)/2.0);
        	myimg.updateAndDraw();
        	//s_down=true;
        }
        if (keyChar=='x')
        {
        	double min, max;
        	min=myimg.getDisplayRangeMin();
        	max=myimg.getDisplayRangeMax();
        	myimg.setDisplayRange(min, min+(max-min)*2.0);
        	myimg.updateAndDraw();
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
            win.removeKeyListener(this);
        if (canvas!=null)
            canvas.removeKeyListener(this);
        //ImagePlus.removeImageListener(this);
	}
	
	public void imageOpened(ImagePlus imp) {}
    public void imageUpdated(ImagePlus imp) {}

}
