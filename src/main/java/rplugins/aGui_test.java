//To create a new GUI, Rename this file "new_filename", then find and replace all instences of
//"aGui_test" with "new_filename".

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class aGui_test 	extends JPanel implements PlugIn , ActionListener{
    static private final String newline = "\n";
    JButton clearButton, SaveButton, processButton;
    JTextField rangeBeginText, rangeEndText;
    JTextArea log;
    JFileChooser fc;
    String CellsFilePath, FidFilePath, Directory, SaveName, NameSub;
    Boolean clearTextFlag = true;
    int count = 0;
    String[] logListBegin = new String[100];
    String[] logListEnd = new String[100];
    File[] files;
   // CalcDistance CalcDist = new CalcDistance();
    
    public aGui_test() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(15,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
	log.setLineWrap(true);
	log.setWrapStyleWord(true);
	log.setText("Instructions:    In the text boxes above, enter a range of frames to be deleted from each image file. Click ''Save Range'' to add the range to the log. Continue adding frame ranges to be deleted and, when done, click ''Process''. A file chooser dialogue will pop up. Select the folder of images to be processed. If a mistake is made, click the ''Clear Log'' button and re-enter frame ranges."+newline+newline+"**IMPORTANT**"+newline+"Ranges must be entered in decending order. Example:"+newline+"97 to 99"+newline+"73 to 76"+newline+"34 to 38");
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the clear button.  
        clearButton = new JButton("Clear Log");
        clearButton.addActionListener(this);

        //Create the save range button.  
        SaveButton = new JButton("Save Range");
        SaveButton.addActionListener(this);

      //Create the process button.  
        processButton = new JButton("Process");
        processButton.addActionListener(this);

	//Create new JTextField
	rangeBeginText = new JTextField("Begin Range",7);
	rangeEndText = new JTextField("End Range",7);
        
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
 	buttonPanel.add(rangeBeginText);
	buttonPanel.add(rangeEndText);
	buttonPanel.add(SaveButton);
        	buttonPanel.add(clearButton);
        	buttonPanel.add(processButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle clear button action.
        if (e.getSource() == clearButton) {

   	log.setText("");
	count = 0;
	for(int i=0;i<100;i++){
		logListBegin[i] = null;
		logListEnd[i] = null;
	}
            log.setCaretPosition(log.getDocument().getLength());

        //Handle save button action.
        } else if (e.getSource() == SaveButton) {
	if(clearTextFlag){
		log.setText("");
		clearTextFlag = false;
	}
	
	logListBegin[count]=rangeBeginText.getText();
	logListEnd[count]=rangeEndText.getText();
	
	count++;
            log.setCaretPosition(log.getDocument().getLength());
	log.append(logListBegin[count-1]+"  to  "+logListEnd[count-1]+newline);
	rangeBeginText.setText("");
	rangeEndText.setText("");
            
        //Handle process button action
        }else if (e.getSource() == processButton) {
	
        	try{
           		 int returnVal = fc.showOpenDialog(aGui_test.this);
        		    if (returnVal == JFileChooser.APPROVE_OPTION) {
        	      		  File dir= fc.getSelectedFile();
			log.append("Processing and saving:"+newline);
    			files = dir.listFiles();
           		    } else {
              		  log.append("Open command cancelled by user." + newline);
           		    }
       	}catch(Exception a){
        		log.append("An Exception has occurred111. " + newline);
        		//return;
        	}
			deleteSlices();
			clearTextFlag = true;
			count = 0;
			for(int i=0;i<100;i++){
				logListBegin[i] = null;
				logListEnd[i] = null;
			}
			log.append(newline + "Enter new set of ranges to continue."+newline);
        }
 
    }


//Function to delete slices
  public void deleteSlices(){
	for(File f : files){
		String current =  f.getPath();
		if (!f.getPath().endsWith("/") & f.getPath().endsWith(".tif")){
			ij.IJ.open(f.getPath());
			ImagePlus imp = WindowManager.getCurrentImage();
			ImageStack stack = imp.getImageStack();
			for(int j=0; j<count; j++){
				int first = Integer.parseInt(logListBegin[j]);
				int last = Integer.parseInt(logListEnd[j]);
				int count1 = 0;
				for (int i=first; i<=last; i++) {
					if ((i-count1)>stack.getSize())
						break;
					stack.deleteSlice(i-count1);
					count1++;
				}
			}
			String name = f.getName();
			String direct = f.getParent();
			log.append(direct + "\\ds_"+ name+ newline);
			IJ.save(direct + "\\ds_"+ name);
			imp.close();
		}
	}	
  }




    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("New Title");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //Add content to the window.
        frame.add(new aGui_test());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void mainrun() {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
    public void run(String arg) {
		//IJ.showMessage("My_Plugin","Hello world!");
		aGui_test.mainrun();
	}
}
