package splugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class open_URL implements PlugIn {

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		GenericDialog gd =new GenericDialog("Choose URL");
		gd.addStringField("URL" , "");
		gd.showDialog();
		String urls=gd.getNextString();
		try {
			URL url=new URL(urls);
			URLConnection urlc=url.openConnection();
			BufferedReader br=new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
