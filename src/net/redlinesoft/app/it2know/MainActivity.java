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
import com.google.ads.*;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity implements
		AdapterView.OnItemSelectedListener {

	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;
	private AdView adView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.d("APP", "load main activity");

		// load admob
		// Create the adView
		adView = new AdView(this, AdSize.BANNER, "a15082bc5732b54");
		// Lookup your LinearLayout assuming it’s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());

		// initial database handler
		final DatabaseHandler myDb = new DatabaseHandler(this);
		// read database
		//myDb.getWritableDatabase();
		// check espidose exist and update from internet
		checkEpisodeExist();
		// load content
		loadContent();
		
		myDb.close();

	}

	private void startDownload() {
		// editText1
		String URLDownload = null;
		Random randomGenerator = new Random();
		URLDownload = getString(R.string.playlist_url) + "?"
				+ String.valueOf(randomGenerator.nextInt());
		new DownloadFileAsync().execute(URLDownload);
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

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
				Log.d("DOWNLOAD", "Error download file");
			}

			return null;

		}

		protected void onProgressUpdate(String... progress) {
			Log.d("DOWNLOAD", progress[0]);
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

	 
		@SuppressWarnings("deprecation")
		protected void onPostExecute(String unused) {			
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			removeDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

	 
		@SuppressWarnings("deprecation")
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

	}

	private void parseContent() {
		final DatabaseHandler myDb = new DatabaseHandler(this);
		// TODO Auto-generated method stub
		try {
		 
			File fXmlFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/playlist.xspf");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
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
					//Log.d("XML", getTagValue("title", eElement));
					//Log.d("XML", getTagValue("location", eElement));
					String strLocation = getTagValue("location", eElement);
					String[] strEpisodeID = strLocation.split("/");
					Integer itemId = Integer.parseInt(strEpisodeID[0]);
					String itemTitle = getTagValue("title", eElement);

					if (itemId >= epidId) {
						epidId++;
						myDb.InsertEpisodeItem(epidId,
								getString(R.string.text_episode) + " " + epidId);
					}
					myDb.InsertItem((temp + 1), epidId, itemTitle, strLocation);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			Log.d("XML", "Error Parsing");
		}

	}

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = (Node) nlList.item(0);
		return nValue.getNodeValue().trim();
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

	private void loadContent() {
		// TODO Auto-generated method stub
		final DatabaseHandler myDb = new DatabaseHandler(this);
		if (myDb.getTotalRow() > 0) {
			Log.d("DB", "load spiner");
			// load content to spiner
			final String[] episodeItem = myDb.SelectAllEpisode();
			Spinner spin = (Spinner) findViewById(R.id.episodSpinner);
			spin.setOnItemSelectedListener(this);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_dropdown_item, episodeItem);
			spin.setAdapter(adapter);
		}
		myDb.close();
	}

	private void checkEpisodeExist() {
		final DatabaseHandler myDb = new DatabaseHandler(this);
		// TODO Auto-generated method stub
		if (myDb.getTotalRow() <= 0) {
			Log.d("DB", "no episode data, should update from internet");
			// asking for update data from internet
			final AlertDialog.Builder dDialog = new AlertDialog.Builder(this);
			dDialog.setTitle(R.string.text_update);
			dDialog.setMessage(R.string.text_update_espisode);
			dDialog.setPositiveButton(R.string.button_yes,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							Log.d("DB", "update episode data from internet");
							// load episode data and items form internet
							startDownload();
							parseContent();
							loadContent();
						}
					});
			dDialog.show();
		}
		myDb.close();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("APP", "restart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
 		Log.d("APP", "resume");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_share:
			Log.d("MENU", "select menu share");
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/*");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getString(R.string.text_share_subject));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					getString(R.string.text_share_body));
			//startActivity(Intent.createChooser(sharingIntent,getString(R.string.menu_share)));
			startActivity(sharingIntent);
			break;
		case R.id.menu_update:
			Log.d("MENU", "select menu update");
			// ask for update
			final DatabaseHandler myDb = new DatabaseHandler(this);
			final AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(R.string.text_update);
			adb.setMessage(R.string.text_asking_for_update);
			adb.setPositiveButton(R.string.button_yes,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {							
							// check network connection
							if (checkNetworkStatus()) {
								Log.d("Network", "Has network connection");
								// clean table initial new update
								DatabaseHandler myDb = new DatabaseHandler(
										MainActivity.this);
								myDb.deleteTableData();
								// intent update database
								Log.d("DB", "update episode data from internet");
								// load episode data and items form internet
								startDownload();
								parseContent();
								loadContent();			
							} else {
								final AlertDialog.Builder alertAdb = new AlertDialog.Builder(
										MainActivity.this);
								alertAdb.setTitle(R.string.text_update);
								alertAdb.setMessage(R.string.text_no_network);
								alertAdb.setNegativeButton(R.string.button_yes,
										null);
								alertAdb.show();
								Log.d("Network", "No network connection");
							}
						}
					});
			adb.setNegativeButton(R.string.button_no, null);
			adb.show();
			myDb.close();
			break;
		case R.id.menu_fanpage:
			Log.d("MENU", "select menu fanpage");
			// intent to facebook fanpage
			Intent fanPageIntent = new Intent(Intent.ACTION_VIEW);
			fanPageIntent.setType("text/url");
			fanPageIntent
					.setData(Uri
							.parse("https://www.facebook.com/pages/ITGoodToKnow/248162135309558"));
			startActivity(fanPageIntent);
			break;
		case R.id.menu_about:
			Log.d("MENU", "select menu about");
			// intent to about page
			Intent aboutIntent = new Intent(MainActivity.this,
					AboutActivity.class);
			startActivity(aboutIntent);
			break;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		 
		// load list item
		final DatabaseHandler myDb = new DatabaseHandler(this);
		final Cursor myData = myDb.SelectEpisode((arg2 + 1));
		ListView lisView = (ListView) findViewById(R.id.listView);

		// ArrayAdapter<String> adapter = new
		// ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
		// myData);
		// lisView.setAdapter(adapter);

		SimpleCursorAdapter adapter;
		adapter = new SimpleCursorAdapter(MainActivity.this,
				R.layout.activity_column, myData, new String[] { "title", "id",
						"location" }, new int[] { R.id.ColTitle });
		lisView.setAdapter(adapter);

		// OnClick Item
		lisView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int position, long mylng) {

				String strLocation = myData.getString(myData
						.getColumnIndex("location"));
				Log.d("DB", "Select " + getString(R.string.media_url)
						+ strLocation);
				// initial media
				Uri uri = Uri.parse(getString(R.string.media_url) + strLocation);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setDataAndType(uri, "audio/mp3");
				startActivity(intent);	
			 
			}
		});

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	public boolean checkNetworkStatus() {
		final ConnectivityManager connMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (wifi.isAvailable()) {
			Log.d("Network", "Connect via Wifi");
			return true;
		} else if (mobile.isAvailable()) {
			Log.d("Network", "Connect via Mobile network");
			return true;
		} else {
			Log.d("Network", "No network connection");
			return false;
		}
	}

}
