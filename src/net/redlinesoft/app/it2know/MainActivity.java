package net.redlinesoft.app.it2know;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.ads.*;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
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

		Log.d("ACTIVITY","load main activity");


		// load admob
		// Create the adView
		adView = new AdView(this, AdSize.BANNER, "a1508d7e3b1c61b");
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
		myDb.getWritableDatabase();		
		// check espidose exist and update from internet
		checkEpisodeExist();		
		// load content
		loadContent();
		
	}
	
	private void loadContent() {
		// TODO Auto-generated method stub
		final DatabaseHandler myDb = new DatabaseHandler(this);
		if (myDb.getTotalRow() > 0) {
			// load content to spiner
			final String[] episodeItem = myDb.SelectAllEpisode();
			Spinner spin = (Spinner) findViewById(R.id.episodSpinner);
			spin.setOnItemSelectedListener(this);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, episodeItem);
			spin.setAdapter(adapter);
		}
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
							Intent updateEpisodeActivity = new Intent(
									MainActivity.this,
									UpdateEpisodeActivity.class);
							startActivity(updateEpisodeActivity);
							parseContent();
						}
					});
			dDialog.show();
		}

	}
	
	private void parseContent(){
		final DatabaseHandler myDb = new DatabaseHandler(this);
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
			 
			mProgressDialog = ProgressDialog.show(MainActivity.this, "","");
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
						if (myDb.isEpisodeExist(epidId)<=0) {
							myDb.InsertEpisodeItem(epidId,getString(R.string.text_episode) + " " + epidId);
							Log.d("DB", "add episode");
						}
					}
 					if (myDb.isItemExist((temp + 1))<=0) {
 						myDb.InsertItem((temp + 1), epidId,itemTitle, strLocation);
 						Log.d("DB", "add item");
 					}
				}

			}			
			loadContent();
			mProgressDialog.cancel();

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
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.text_share_subject));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.text_share_body));
			startActivity(Intent.createChooser(sharingIntent, getString(R.string.menu_share)));
			break;
		case R.id.menu_update:
			Log.d("MENU", "select menu update");
			// ask for update			
			final AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(R.string.text_update);
			adb.setMessage(R.string.text_asking_for_update);
			adb.setPositiveButton(R.string.button_yes,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog,	int arg1) {
							// check network connection
							if (checkNetworkStatus()){
								Log.d("Network", "Has network connection");
								// clean table initial new update
								DatabaseHandler myDb = new DatabaseHandler(MainActivity.this);
								myDb.deleteTableData();
								// intent update database
								Log.d("DB", "update episode data from internet");
								// load episode data and items form internet
								Intent updateEpisodeActivity = new Intent(MainActivity.this,UpdateEpisodeActivity.class);
								startActivity(updateEpisodeActivity);
								parseContent();
								loadContent();
								
							} else {
								final AlertDialog.Builder alertAdb = new AlertDialog.Builder(MainActivity.this);
								alertAdb.setTitle(R.string.text_update);
								alertAdb.setMessage(R.string.text_no_network);
								alertAdb.setNegativeButton(R.string.button_yes,null);
								alertAdb.show();
								Log.d("Network", "No network connection");
							}
						}
					});
			adb.setNegativeButton(R.string.button_no,null);
			adb.show();		
			break;		
		case R.id.menu_fanpage:
			Log.d("MENU", "select menu fanpage");
			// intent to facebook fanpage
			Intent fanPageIntent = new Intent(Intent.ACTION_VIEW);
			fanPageIntent.setData(Uri.parse("https://www.facebook.com/pages/ITGoodToKnow/248162135309558"));
			startActivity(fanPageIntent);
			break;		
		case R.id.menu_about:
			Log.d("MENU", "select menu about");
			//intent to about page
			Intent aboutIntent = new Intent(MainActivity.this,AboutActivity.class);
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
		mProgressDialog = ProgressDialog.show(MainActivity.this, "",getString(R.string.text_update));
		// load list item
		final DatabaseHandler myDb = new DatabaseHandler(this);
		final Cursor myData = myDb.SelectEpisode((arg2+1));
        ListView lisView = (ListView)findViewById(R.id.listView);
        
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, myData);
        //lisView.setAdapter(adapter);
        
        SimpleCursorAdapter adapter;
        adapter = new SimpleCursorAdapter(MainActivity.this, R.layout.activity_column, myData,new String[] {"title","id","location"},new int[] {R.id.ColTitle});
        lisView.setAdapter(adapter);       
		mProgressDialog.dismiss();
		
		// OnClick Item
		lisView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int position, long mylng) {

				String strLocation = myData.getString(myData.getColumnIndex("location"));
				Log.d("DB", "Select " + getString(R.string.media_url)+ strLocation);
				// initial media
				Uri uri = Uri.parse(getString(R.string.media_url)+ strLocation);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
	
	public boolean checkNetworkStatus() {
		final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
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
