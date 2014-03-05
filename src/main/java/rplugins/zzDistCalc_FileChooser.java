/*
public class DistCalc_FileChooser implements PlugIn {

	public void run(String arg) {
		IJ.showMessage("DistCalc_FileChooser","Hello world!");
	}

}

*/

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
import javax.swing.filechooser.*;

public class DistCalc_FileChooser 	extends JPanel
							implements ActionListener, PlugIn{
    static private final String newline = "\n";
    JButton openCellsButton, opeFidButton, processButton;
    JTextArea log;
    JFileChooser fc;
    String CellsFilePath, FidFilePath, Directory, SaveName, NameSub;
    CalcDistance CalcDist = new CalcDistance();
    
    public DistCalc_FileChooser() {
        super(new BorderLayout());

        //Create the log first, because the action listeners
        //need to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        //Create a file chooser
        fc = new JFileChooser();

        //Uncomment one of the following lines to try a different
        //file selection mode.  The first allows just directories
        //to be selected (and, at least in the Java look and feel,
        //shown).  The second allows both files and directories
        //to be selected.  If you leave these lines commented out,
        //then the default mode (FILES_ONLY) will be used.
        //
        //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        //Create the open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        openCellsButton = new JButton("Open Cells File");
        openCellsButton.addActionListener(this);

        //Create the next open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        opeFidButton = new JButton("Open Fiducial File");
        opeFidButton.addActionListener(this);

      //Create the next open button.  We use the image from the JLF
        //Graphics Repository (but we extracted it from the jar).
        processButton = new JButton("Process");
        processButton.addActionListener(this);
        
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openCellsButton);
        buttonPanel.add(opeFidButton);
        buttonPanel.add(processButton);

        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open1 button action.
        if (e.getSource() == openCellsButton) {
            int returnVal = fc.showOpenDialog(DistCalc_FileChooser.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would open the file.
                log.append("Cells File: " + file.getPath() + newline);
                CellsFilePath = file.getPath();
                log.append("Parent Dir: " + file.getParentFile() + newline);
                Directory = file.getParent();
                SaveName = file.getName();
                int subLength = SaveName.length() - 4;
                NameSub = SaveName.substring(0,subLength);
                //log.append("File Name SUB: " + NameSub + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

        //Handle open2 button action.
        } else if (e.getSource() == opeFidButton) {
            int returnVal = fc.showSaveDialog(DistCalc_FileChooser.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                //This is where a real application would save the file.
                log.append("Fiducial File: " + file.getPath() + "." + newline);
                FidFilePath = file.getPath();
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
            
        //Handle process button action
        }else if (e.getSource() == processButton) {
            //call the CalcDistance Plugin to process the files.
        	try{
        		CalcDist.doCalcDist(CellsFilePath, FidFilePath, Directory, NameSub);
        		log.append("---------------------------------" + newline
        				+"A file will be saved to the following path:" + "\n"
        				+Directory + "\\DistCalc_" + NameSub + ".csv" + "\n"
        				+"---------------------------------" + newline);
        	}
        	catch(Exception a){
        		//return;
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
        JFrame frame = new JFrame("Distance to Fiducials");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new DistCalc_FileChooser());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
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
}
