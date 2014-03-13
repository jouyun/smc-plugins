package splugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Filter_Colocalization_Candidates implements PlugIn, KeyListener {

	ImageWindow win;
    ImageCanvas canvas;
    ImagePlus myimg;
    int counts;
    boolean [] hit_list;
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        int flags = e.getModifiers();
        //IJ.log("keyPressed: keyCode=" + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        //IJ.log("char: "+keyChar);
        if (keyChar=='f') 
        {
        	win.getImagePlus().setSlice(win.getImagePlus().getSlice()+1);
        }
        if (keyChar=='a')
        {
        	win.getImagePlus().setSlice(win.getImagePlus().getSlice()-1);
        }
        if (keyChar=='s')
        {
        	counts++;
        	win.getImagePlus().getProcessor().invert();
        	win.getImagePlus().updateAndDraw();
        	hit_list[myimg.getFrame()]=true;
        }
        if (keyChar=='d')
        {
        	counts--;
        	win.getImagePlus().getProcessor().invert();
        	win.getImagePlus().updateAndDraw();
        	hit_list[myimg.getFrame()]=false;
        }
        if (keyChar=='q')
        {
        	int tmp=0;
        	for (int i=0; i<hit_list.length; i++)
        	{
        		if (hit_list[i]) tmp++;
        	}
        	IJ.log("Counts:  " + tmp);
        	cleanup();
        }

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

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
        IJ.log("addKeyListener");
        counts=0;
        hit_list=new boolean[myimg.getNFrames()];

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
        IJ.log("removeKeyListener");
	}

}
