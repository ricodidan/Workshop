package org.meruvian.workshop.content.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import org.meruvian.workshop.content.database.model.NewsDatabaseModel;

/**
 * Created by Enrico_Didan on 24/12/2016.
 */

public class NewsDatabase extends SQLiteOpenHelper
{
    public static final String DATABASE = "sample_news";
    private static final int VERSION = 1;
    public static final String NEWS_TABLE = "tbl_news";
    private Context context;
    public NewsDatabase(Context context) {
        super(context, DATABASE, null, VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + NEWS_TABLE + "("
                + NewsDatabaseModel.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + NewsDatabaseModel.TITLE + " TEXT, "
                + NewsDatabaseModel.CONTENT + " TEXT, "
                + NewsDatabaseModel.CREATE_DATE + " INTEGER, "
                + NewsDatabaseModel.STATUS + " INTEGER)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
        }
    }
}
