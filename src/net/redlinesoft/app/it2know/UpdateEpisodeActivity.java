package net.redlinesoft.app.it2know;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class UpdateEpisodeActivity extends Activity {
	
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;

	private ProgressDialog mProgressDialog;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_episode);
        Log.d("ACTIVITY","load update episode activity");
        startDownload();        
        parseContent(); 
        finish();
    }
   
	private void parseContent() {
    	final DatabaseHandler myDb = new DatabaseHandler(this);
		// TODO Auto-generated method stub
    	try {
			File fXmlFile = new File(Environment.getExternalStorageDirectory().getPath() + "/playlist.xspf");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			Log.d("XML", doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("track");

			Log.d("XML", String.valueOf(nList.getLength()));

			Integer epidId = 0;
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					Log.d("XML",getTagValue("title", eElement));
					Log.d("XML",getTagValue("location",eElement));
					String strLocation = getTagValue("location", eElement);
					String[] strEpisodeID = strLocation.split("/");
					Integer itemId = Integer.parseInt(strEpisodeID[0]);
					String itemTitle = getTagValue("title",eElement);

					if (itemId >= epidId) {
						epidId++;
						myDb.InsertEpisodeItem(epidId,getString(R.string.text_episode) + " " + epidId);
					}
					myDb.InsertItem((temp + 1), epidId,itemTitle, strLocation);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("XML", "Error Parsing");
		}
		
	}

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue().trim();
	}
 
	private void startDownload() {
		// editText1
		String URLDownload =null;
		Random randomGenerator = new Random();
		URLDownload = getString(R.string.playlist_url) + "?" + String.valueOf(randomGenerator.nextInt()) ;
		new DownloadFileAsync().execute(URLDownload);
	}
    
    class DownloadFileAsync extends AsyncTask<String, String, String> {

		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		@Override
		protected String doInBackground(String... aurl) {
			int count;

			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d("DOWNLOAD", "Lenght of file: " + lenghtOfFile);

				InputStream input = new BufferedInputStream(url.openStream());

				// Get File Name from URL
				// String fileName = URLDownload.substring(
				// URLDownload.lastIndexOf('/')+1, URLDownload.length() );

				OutputStream output = new FileOutputStream(Environment
						.getExternalStorageDirectory().getPath()
						+ "/playlist.xspf");

				Log.d("FILE", Environment.getExternalStorageDirectory()
						.getPath() + "/playlist.xspf");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
				
			} catch (Exception e) {
				Log.d("DOWNLOAD","Error download file");
			}
	
			return null;

		}
		
		 


		protected void onProgressUpdate(String... progress) {
			Log.d("DOWNLOAD", progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("กำลังดาวน์โหลด...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_update_episode, menu);
        return true;
    }
}
