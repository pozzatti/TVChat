package br.com.tvchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class TVChatOpenHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TVChatDB";

    public TVChatOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE READED_COMMENTS(thread_id TEXT, comment_id TEXT)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS READED_COMMENTS");

        onCreate(db);
    }

    public void addReadedComments(String threadId, List<String> commentIds){
        commentIds.removeAll(getCommentIds(threadId));

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        for (String commentId : commentIds){
            ContentValues values = new ContentValues();
            values.put("thread_id", threadId);
            values.put("comment_id", commentId);

            db.insert("READED_COMMENTS", null, values);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public List<String> getCommentIds(String threadId){
        SQLiteDatabase db = getReadableDatabase();

        List<String> commentIds = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT comment_id FROM READED_COMMENTS WHERE thread_id = ?", new String[]{threadId});
        if (cursor!=null && cursor.moveToFirst()){
            do {
                commentIds.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        return commentIds;
    }

    public int countReadedComments(String threadId){
        SQLiteDatabase db = getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "READED_COMMENTS", "thread_id = ?", new String[]{threadId});
        db.close();

        return (int)count;
    }
}
