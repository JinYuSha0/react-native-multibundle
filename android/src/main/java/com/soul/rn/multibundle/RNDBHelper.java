package com.soul.rn.multibundle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.soul.rn.multibundle.constant.ComponentType;

import java.util.ArrayList;
import java.util.HashMap;

public class RNDBHelper extends SQLiteOpenHelper {
  private static final int DB_VERSION = 1;
  private static final String DB_NAME = "rn.multiBundle.db";
  private static final String TABLE_NAME = "bundle";
  public static RNDBHelper mRNDBHelper;

  public RNDBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    String sql = "create table if not exists " + TABLE_NAME + " (BundleName varchar(100), ComponentName varchar(100), ComponentType interge, Version interge, Hash varchar(100), Filepath varchar(100), PublishTime interge, InstallTime integer, primary key(BundleName, Version))";
    sqLiteDatabase.execSQL(sql);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
    sqLiteDatabase.execSQL(sql);
    onCreate(sqLiteDatabase);
  }

  public static RNDBHelper init (Context context) {
    mRNDBHelper = new RNDBHelper(context);
    return mRNDBHelper;
  }

  public static int getRowsCount() {
    SQLiteDatabase db = mRNDBHelper.getReadableDatabase();
    Cursor cursor = db.rawQuery("select count(*) from " + TABLE_NAME,null);
    cursor.moveToFirst();
    int count = cursor.getInt(0);
    return count;
  }

  public static void insertRow(ContentValues contentValues) {
    SQLiteDatabase db = mRNDBHelper.getWritableDatabase();
    db.insertOrThrow(TABLE_NAME, null, contentValues);
  }

  public static void insertRows(ArrayList<ContentValues> contentValuesArrayList) {
    SQLiteDatabase db = mRNDBHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (ContentValues contentValues : contentValuesArrayList) {
        db.insertOrThrow(TABLE_NAME, null, contentValues);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }

  public static ContentValues createContentValues(String BundleName, String ComponentName, Integer ComponentType, Integer Version, String Hash, String Filepath, Long PublishTime) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("ComponentName", ComponentName);
    contentValues.put("ComponentType", ComponentType);
    contentValues.put("BundleName", BundleName);
    contentValues.put("Version", Version);
    contentValues.put("Hash", Hash);
    contentValues.put("Filepath", Filepath);
    contentValues.put("PublishTime", PublishTime);
    contentValues.put("InstallTime", System.currentTimeMillis());
    return contentValues;
  }

  public static Result parseCursor(Cursor cursor) {
    Result result = new Result(cursor.getString(cursor.getColumnIndex("BundleName")),
            cursor.getString(cursor.getColumnIndex("ComponentName")),
            cursor.getInt(cursor.getColumnIndex("ComponentType")),
            cursor.getInt(cursor.getColumnIndex("Version")),
            cursor.getString(cursor.getColumnIndex("Hash")),
            cursor.getString(cursor.getColumnIndex("Filepath")),
            cursor.getLong(cursor.getColumnIndex("PublishTime")),
            cursor.getLong(cursor.getColumnIndex("InstallTime")));
    return result;
  }

  public static Result selectByComponentName(String ComponentName) {
    SQLiteDatabase db = mRNDBHelper.getReadableDatabase();
    String sql = String.format("SELECT * FROM %s WHERE ComponentName = \"%s\" ORDER BY Version DESC LIMIT 1;",TABLE_NAME,ComponentName);
    Cursor cursor = db.rawQuery(sql,null);
    Result result = null;
    if (cursor.moveToNext()) {
      result = parseCursor(cursor);
    }
    return result;
  }

  public static ArrayList<Result> selectAll() {
    SQLiteDatabase db = mRNDBHelper.getReadableDatabase();
    ArrayList<Result> result = new ArrayList<>();
    String sql = String.format("SELECT * FROM %s a WHERE Version = (SELECT MAX(b.Version) FROM %s b WHERE b.BundleName = a.BundleName) ORDER BY a.BundleName",TABLE_NAME,TABLE_NAME);
    Cursor cursor = db.rawQuery(sql,null);
    while (cursor.moveToNext()) {
      result.add(parseCursor(cursor));
    }
    return result;
  }

  public static HashMap<String, Result> selectAllMap() {
    HashMap<String, Result> result = new HashMap<>();
    ArrayList<Result> components = RNDBHelper.selectAll();
    for (int i = 0; i < components.size(); i++) {
      Result curr = components.get(i);
      switch (ComponentType.getByValue(curr.ComponentType)) {
        case Common:
          result.put("Common", curr);
          break;
        case Bootstrap:
          result.put("Bootstrap", curr);
          break;
        default:
          result.put(curr.ComponentName, curr);
          break;
      }
    }
    return result;
  }

  public static class Result {
    String BundleName;
    String ComponentName;
    Integer ComponentType;
    Integer Version;
    String Hash;
    String FilePath;
    Long PublishTime;
    Long InstallTime;

    public Result(String bundleName, String componentName, Integer componentType, Integer version, String hash, String filePath, Long publishTime, Long installTime) {
      BundleName = bundleName;
      ComponentName = componentName;
      ComponentType = componentType;
      Version = version;
      Hash = hash;
      FilePath = filePath;
      PublishTime = publishTime;
      InstallTime = installTime;
    }
  }
}
