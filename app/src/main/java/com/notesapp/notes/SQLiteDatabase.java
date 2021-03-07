package com.notesapp.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notes.db";

    // Notes Table Name
    private static final String TABLE_NOTES = "notes";

    // Notes Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CREATED_AT = "createdAt";
    private static final String COLUMN_OBJECT_ID = "objectId";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_PINNED = "pinned";
    private static final String COLUMN_TO_EDIT = "toEdit";
    private static final String COLUMN_TO_DELETE = "toDelete";
    private static final String COLUMN_NOTIFICATION_ID = "notificationID";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //Creating the query to create a table
    String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" + COLUMN_ID +
            " INTEGER PRIMARY KEY, " + COLUMN_CREATED_AT + " DATETIME DEFAULT /*This variable (CURRENT_TIMESTAMP) " +
            "holds the current time therefore the time at which the query is created is added to the database*/ CURRENT_TIMESTAMP, " +
            COLUMN_OBJECT_ID + " TEXT, " + COLUMN_TITLE + " TEXT, " + COLUMN_NOTE + " TEXT, " + COLUMN_PINNED + " INTEGER, " +
            COLUMN_TO_EDIT + " TEXT DEFAULT \"No\", " + COLUMN_TO_DELETE + " TEXT DEFAULT \"No\"," + COLUMN_NOTIFICATION_ID + " INTEGER  DEFAULT -1" +")";

    public SQLiteDatabase(Context context) {
        super(context, /*name if the database*/DATABASE_NAME, /*cursor factory*/null,/*version of the database*/ DATABASE_VERSION);

    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase sqLiteDatabase) {
        //On creating the database it will create the table
        sqLiteDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //on Upgrading the database drop the previous table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
    }

    //Creating a new Note
    public void addNote(Note note) {
        //Getting the writable version of the database so that the new note can be added to it
        android.database.sqlite.SQLiteDatabase database = this.getWritableDatabase();
        //Creating ContentValues object which will store the values of the columns
        ContentValues values = new ContentValues();
        //We're not inserting createdAt as the date is automatically inserted as the default value of date is the current date and time
        //Putting the objectId column as null as in the LoggedInActivity all notes with null objectIds will be uploaded to the server
        values.putNull(COLUMN_OBJECT_ID);
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_NOTE, note.getNote());
        values.put(COLUMN_PINNED, note.getPinned());
        values.put(COLUMN_TO_EDIT, note.getToEdit());
        values.put(COLUMN_TO_DELETE, note.getToDelete());
        //Inserting the ContentValue object i.e. the values to the database
        database.insert(/*Table Name*/TABLE_NOTES, null, /*ContentValues*/values);
        database.close();
    }

    // Getting a single Note - read
    public Note getNote(int id) {
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Setting the cursor to the first entry which fulfills the criteria i.e. the id of the note should be equal to id passed as a parameter. The cursor basically holds the queries that match the criteria
        Cursor cursor = database.query(/*Name of the table*/TABLE_NOTES, /*Columns you want to select in the form of an array of strings*/new String[]{COLUMN_ID,
                        COLUMN_CREATED_AT, COLUMN_OBJECT_ID, COLUMN_TITLE, COLUMN_NOTE, COLUMN_PINNED, COLUMN_TO_EDIT, COLUMN_TO_DELETE, COLUMN_NOTIFICATION_ID},
                /*This is basically the where clause*/COLUMN_ID + "=?",/*The question marks will be replaced the String array elements here*/ new String[]{String.valueOf(id)}, /*group by*/null,/*Having*/ null,/*order by*/ null);

        //Checking if cursor is not null i.e. query exists
        if (cursor != null) {
            //Moving the cursor to the beginning i.e. the first query (can be understood as index 0)
            cursor.moveToFirst();
        }
        //Creating a Note object
        Note note = null;
        //Trying to get the note this is because we are using the SimpleDateFormat.parse() which must be surrounded by a try catch block else we'll get a compile error
        try {
            note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Returning the note object
        return note;
    }

    // Getting all Note Objects
    public List<Note> getAllOtherNotes() {
        //Creating a list of notes that are not pinned
        List<Note> noteList = new ArrayList<>();
        //Creating a raw SQL query if you don't understand this learn SQL and come and if you still don't understand leave programming
        String selectAllQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_PINNED + " = 0 AND " + COLUMN_TO_DELETE + " != \"Yes\"" + " ORDER BY " + COLUMN_CREATED_AT + " DESC" + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a Cursor object that is going to hold the queries of the raw query mentioned above
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Now this is interesting the cursor.move to first returns a boolean value i.e. the boolean value depends on whether cursor is null or not i.e tue if not null false if null
        if (cursor.moveToFirst()) {
            do {
                try {
                    Note note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
                    noteList.add(note);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        //Returning the list of notes
        return noteList;
    }

    public List<Note> getAllPinnedNotes() {
        //Creating a list of notes that are pinned
        List<Note> noteList = new ArrayList<>();
        //Creating a raw SQL query if you don't understand this learn SQL and come and if you still don't understand leave programming
        String selectAllQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_PINNED + " = 1 AND " + COLUMN_TO_DELETE + " <> \"Yes\"" + " ORDER BY " + COLUMN_CREATED_AT + " DESC" + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a Cursor object that is going to hold the queries of the raw query mentioned above
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                try {
                    Note note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
                    noteList.add(note);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        //Returning the list of notes
        return noteList;
    }

    // Updating a single Note
    //Here it we are returning an int value as the update method returns an integer. This integer is the number of rows that has been affected by the update query
    public int updateNote(Note note) {
        //Getting the writable version of the database so that the note can be updated.
        android.database.sqlite.SQLiteDatabase database = this.getWritableDatabase();
        //Creating content values to store the updated values of the column
        ContentValues values = new ContentValues();
        values.put(COLUMN_OBJECT_ID, note.getObjectId());
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_NOTE, note.getNote());
        values.put(COLUMN_PINNED, note.getPinned());
        values.put(COLUMN_TO_EDIT, note.getToEdit());
        values.put(COLUMN_TO_DELETE, note.getToDelete());
        values.put(COLUMN_NOTIFICATION_ID, note.getNotificationID());
        //Updating the database and returning the number of rows affected
        return database.update(TABLE_NOTES, values, COLUMN_ID + " = ? ",
                new String[]{String.valueOf(note.getId())});
    }

    // Deleting a single Note
    public void deleteNote(Note note) {
        //Getting the writable version of the database so that the note can be deleted.
        android.database.sqlite.SQLiteDatabase database = this.getWritableDatabase();
        //Deleting the note (by the way the delete function also returns a integer value showing the number of rows it has affected)
        database.delete(TABLE_NOTES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
//        database.close();
    }

    // Getting all Note Objects to be deleted
    public List<Note> getAllNotesToBeDeleted() {
        //Creating the list that will contain the notes that have to be deleted
        List<Note> noteList = new ArrayList<>();
        //Creating a raw query to get all the notes that have to be deleted by checking if the toDelete column is equal to "Yes"
        String selectAllQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_TO_DELETE + " = \"Yes\" " + " ORDER BY " + COLUMN_CREATED_AT + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a cursor that will hold these queries/rows
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                try {
                    Note note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
                    //Adding all these notes to the ArrayList which will later be returned
                    noteList.add(note);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        //Returning the list of notes
        return noteList;
    }

    // Getting all Note Objects to be deleted
    public List<Note> getAllNotesToBeUpdated() {
        //Creating a List of notes that will hold the notes that have to be updated in the server
        List<Note> noteList = new ArrayList<>();
        //Creating a raw query to check which notes have to be updating by checking toEdit column equals to "Yes"
        String selectAllQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_TO_EDIT + " = \"Yes\" " + " ORDER BY " + COLUMN_CREATED_AT + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a cursor that will hold these queries/rows
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                try {
                    Note note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
                    noteList.add(note);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        //Returning the list of notes
        return noteList;
    }

    public List<Note> getAllNotesToBeUploaded() {
        //Creating a List of notes that will hold the notes that have to be uploaded to the server
        List<Note> noteList = new ArrayList<>();
        //Creating a raw query to check which notes have to be uploaded by checking if objectId is null
        String selectAllQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_OBJECT_ID + " IS NULL " + " ORDER BY " + COLUMN_CREATED_AT + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a cursor that will hold these queries/rows
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                try {
                    Note note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
                    noteList.add(note);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        return noteList;
    }

    public Note getByObjectID(String id) {
        //Creating a List of notes that will hold the notes that have to be uploaded to the server
        Note note;
        //Creating a raw query to check which notes have to be uploaded by checking if objectId is null
        String selectAllQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_OBJECT_ID + " = \""+ id +"\"" +";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a cursor that will hold these queries/rows
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                try {
                    note = new Note(Integer.parseInt(cursor.getString(0)), sdf.parse(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4), Integer.parseInt(cursor.getString(5)), cursor.getString(6), cursor.getString(7), Integer.parseInt(cursor.getString(8)));
                    return note;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        return null;
    }

    public List<String> getAllObjectIdsToBeUploadedOrDeleted() {
        //Creating a List of notes that will hold the notes that have to be uploaded to the server
        List<String> objectIdList = new ArrayList<>();
        //Creating a raw query to check which notes have to be uploaded by checking if objectId is null
        String selectAllQuery = "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_NOTES + " WHERE " + COLUMN_TO_DELETE + " = \"Yes\" OR " + COLUMN_TO_EDIT + " = \"Yes\"" + " ORDER BY " + COLUMN_CREATED_AT + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a cursor that will hold these queries/rows
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                String objectId = cursor.getString(0);
                objectIdList.add(objectId);

            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        return objectIdList;
    }

    public List<String> getAllObjectIdsToBeReplaced() {
        //Creating a List of notes that will hold the notes that have to be uploaded to the server
        List<String> objectIdList = new ArrayList<>();
        //Creating a raw query to check which notes have to be uploaded by checking if objectId is null
        String selectAllQuery = "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_NOTES + " WHERE " + COLUMN_TO_DELETE + " = \"No\" AND " + COLUMN_TO_EDIT + " = \"No\" AND " + COLUMN_OBJECT_ID + " IS NOT NULL" + " ORDER BY " + COLUMN_CREATED_AT + ";";
        //Getting readable database as we do not have to write anything in the database
        android.database.sqlite.SQLiteDatabase database = this.getReadableDatabase();
        //Creating a cursor that will hold these queries/rows
        Cursor cursor = database.rawQuery(selectAllQuery, null);
        //Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and returns a boolean value based on that(if null false else true). It also moves to the first query.
        if (cursor.moveToFirst()) {
            do {
                String objectId = cursor.getString(0);
                objectIdList.add(objectId);

            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        return objectIdList;
    }

    public void addNoteWithObjectID(Note note) {
        //Getting the writable version of the database so that the new note can be added to it
        android.database.sqlite.SQLiteDatabase database = this.getWritableDatabase();
        //Creating ContentValues object which will store the values of the columns
        ContentValues values = new ContentValues();
        //We're not inserting createdAt as the date is automatically inserted as the default value of date is the current date and time
        //Putting the objectId column as null as in the LoggedInActivity all notes with null objectIds will be uploaded to the server
        values.put(COLUMN_OBJECT_ID, note.getObjectId());
        values.put(COLUMN_CREATED_AT, sdf.format(note.getCreatedAt()));
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_NOTE, note.getNote());
        values.put(COLUMN_PINNED, note.getPinned());
        values.put(COLUMN_TO_EDIT, note.getToEdit());
        values.put(COLUMN_TO_DELETE, note.getToDelete());
        //Inserting the ContentValue object i.e. the values to the database
        database.insert(/*Table Name*/TABLE_NOTES, null, /*ContentValues*/values);
        database.close();
    }

}
