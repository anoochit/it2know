package net.redlinesoft.app.it2know;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	public DatabaseHandler(Context context) {
		super(context, context.getExternalFilesDir(null).getAbsolutePath().toString() + "/" + "it2know.db", null, 1);
	} 
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS episode (id INTEGER PRIMARY KEY AUTOINCREMENT, title STRING)");
		db.execSQL("CREATE TABLE IF NOT EXISTS item (id INTEGER PRIMARY KEY AUTOINCREMENT, epid INTEGER, title STRING,location STRING)");
		Log.d("DB", "Create Table Successfully.");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
	
	public long InsertEpisodeItem(Integer epidId, String strTitle) {
		try {
			SQLiteDatabase db;
			db = this.getWritableDatabase(); // Write Data

			/**
			 * for API 11 and above SQLiteStatement insertCmd; String strSQL =
			 * "INSERT INTO " + TABLE_MEMBER +
			 * "(MemberID,Name,Tel) VALUES (?,?,?)";
			 * 
			 * insertCmd = db.compileStatement(strSQL); insertCmd.bindString(1,
			 * strMemberID); insertCmd.bindString(2, strName);
			 * insertCmd.bindString(3, strTel); return
			 * insertCmd.executeInsert();
			 */

			ContentValues Val = new ContentValues();
			Val.put("id", epidId);
			Val.put("title", strTitle); 
			
			long rows = db.insert("episode", null, Val);

			db.close();
			return rows; // return rows inserted.

		} catch (Exception e) {
			return -1;
		}

	}
	
	public long InsertItem(Integer itemId,Integer epidId, String strTitle, String strLocation) {
		try {
			SQLiteDatabase db;
			db = this.getWritableDatabase(); // Write Data

			/**
			 * for API 11 and above SQLiteStatement insertCmd; String strSQL =
			 * "INSERT INTO " + TABLE_MEMBER +
			 * "(MemberID,Name,Tel) VALUES (?,?,?)";
			 * 
			 * insertCmd = db.compileStatement(strSQL); insertCmd.bindString(1,
			 * strMemberID); insertCmd.bindString(2, strName);
			 * insertCmd.bindString(3, strTel); return
			 * insertCmd.executeInsert();
			 */

			ContentValues Val = new ContentValues();
			Val.put("id", itemId);
			Val.put("epid", epidId);
			Val.put("title", strTitle);
			Val.put("location", strLocation); 
			
			long rows = db.insert("item", null, Val);

			db.close();
			return rows; // return rows inserted.

		} catch (Exception e) {
			return -1;
		}

	}
	

	public String[] SelectAllEpisode() {
		// TODO Auto-generated method stub
		try {
			String arrData[] = null;
			SQLiteDatabase db;
			db = this.getReadableDatabase(); // Read Data

			String strSQL = "SELECT title FROM  episode";
			Cursor cursor = db.rawQuery(strSQL, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					arrData = new String[cursor.getCount()];
					/***
					 * [x] = Name
					 */
					int i = 0;
					do {
						arrData[i] = cursor.getString(0);
						i++;
					} while (cursor.moveToNext());

				}
			}
			cursor.close();

			return arrData;

		} catch (Exception e) {
			return null;
		}
	}

	public Cursor SelectEpisode(Integer epId) {
		// TODO Auto-generated method stub
		try {
		 
			SQLiteDatabase db;
			db = this.getReadableDatabase(); // Read Data

			String strSQL = "SELECT id As _id , * FROM item WHERE epid="+epId;
			Cursor cursor = db.rawQuery(strSQL, null);
			return cursor;

		} catch (Exception e) {
			return null;
		}

	}

	public Integer getTotalRow() {
		// TODO Auto-generated method stub
		try {
			SQLiteDatabase db;
			db = this.getReadableDatabase(); // Read Data
			Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM episode", null);
			cursor.moveToFirst();
			int count = cursor.getInt(0);
			cursor.close();
			return count;
		} catch (Exception e) {
			return 0;
		}
	}

}
