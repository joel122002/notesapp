package com.notesapp.notes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Date;

public class EditNoteActivity extends AppCompatActivity {
    //Creating instance variables
    private EditText editTextNote, editTextTitle;
    //noteFromId.getObjectId() will be obtained from the intent
    private int id;
    //Notification Builder for building//displaying a notification that is pinned by a user
    private NotificationCompat.Builder builder;
    //A unique notification ID that creates a unique integer based on the system time
    private int NOTIFICATION_ID = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    //This is to check if the item to be edited is pinned or not. It gets its value from the intent
    private SQLiteDatabase database;
    private Note noteFromId;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        //Initializing the EditTexts for the Title and Note
        editTextNote = findViewById(R.id.editTextNote);
        editTextTitle = findViewById(R.id.editTextTitle);
        //Getting the note, title ans isPinned from the intent
        Intent receivedIntent = getIntent();
        id = receivedIntent.getIntExtra("id", -1);
        database = new SQLiteDatabase(this);
        noteFromId = database.getNote(id);
        //Setting note and title from the values got from intent
        editTextNote.setText(noteFromId.getNote());
        editTextTitle.setText(noteFromId.getTitle());
        //This method will call the onPrepareOptionsMenu() method that will chang the icon if the item is pinned
        invalidateOptionsMenu();
    }

    //Creating a menu in the ActionBar for pinning and saving
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //This method is responsible to change the icon if the note is pinned to unpin
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (noteFromId.getPinned() == 1) {
            menu.findItem(R.id.notify).setIcon(R.drawable.ic_unpin);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //Setting up an onOptionsItemSelected for listening to the clicks on the menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) {
            //Updating the note and setting toEdit to "Yes"
            updateExistingNote(id);
        } else if (item.getItemId() == R.id.notify) {
            //Updating the note and setting toEdit to "Yes"
            pinUpdatedNote(id);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateExistingNote(int id) {
        //Checking if atleast one of the EditTexts (Title or Note) is not empty i.e. is filled
        if (!editTextTitle.getText().toString().isEmpty() || !editTextNote.getText().toString().isEmpty()) {
            //Updating the note
            if (noteFromId.getPinned() == 1) {
                //Trying to dismiss the notification as the user may have already dismissed it
                dismissNotification();
                //Creating a notification
                createNotification();
                Note note = new Note(id, noteFromId.getCreatedAt(), noteFromId.getObjectId(), editTextTitle.getText().toString().trim(), editTextNote.getText().toString().trim(), noteFromId.getPinned(), "Yes", "No", NOTIFICATION_ID);
                database.updateNote(note);
            }
            else {
                Note note = new Note(id, noteFromId.getCreatedAt(), noteFromId.getObjectId(), editTextTitle.getText().toString().trim(), editTextNote.getText().toString().trim(), noteFromId.getPinned(), "Yes", "No", -1);
                database.updateNote(note);
            }
            //Switching to LoggedInActivity after pinning and notifying the user
            Intent intent = new Intent(EditNoteActivity.this, LoggedInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            //If note and title is empty
            Toast.makeText(EditNoteActivity.this, "Note cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void pinUpdatedNote(final int id) {
        //Checking if atleast one of the EditTexts (Title or Note) is not empty i.e. is filled
        if (!editTextTitle.getText().toString().isEmpty() || !editTextNote.getText().toString().isEmpty()) {
            //if not pinned then
            if (noteFromId.getPinned() == 0) {
                //Update the note and pin it
                Note note = new Note(id, noteFromId.getCreatedAt(), noteFromId.getObjectId(), editTextTitle.getText().toString().trim(), editTextNote.getText().toString().trim(), 1, "Yes", "No", NOTIFICATION_ID);
                database.updateNote(note);
                createNotification();
            }
            //if it is already pinned
            else {
                //Update and unpin it and try to dismiss the notification
                Note note = new Note(id, noteFromId.getCreatedAt(), noteFromId.getObjectId(), editTextTitle.getText().toString().trim(), editTextNote.getText().toString().trim(), 0, "Yes", "No", -1);
                database.updateNote(note);
                dismissNotification();
            }
            //Switching to LoggedInActivity after pinning and notifying the user
            Intent intent = new Intent(EditNoteActivity.this, LoggedInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void createNotification()
    {
        //Creating a notification by creating a NotificationManager and initializing it
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Creating an intent that will dismiss the notification and passing the NOTIFICATION_ID as an IntExtra so that it can be dismissed in the Notification Activity
        Intent intentdDismissNotification = new Intent(EditNoteActivity.this, NotificationActivity.class);
        intentdDismissNotification.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);
        //Creating a PendingIntent which wil be called when the User clicks on the Dismiss Button in the notification. The NOTIFICATION_ID is set as the request code as the request code must be unique for all notification. If it is not unique and there are multiple notifications we will only be able to dismiss the most recent notification
        PendingIntent resultPendingIntent = PendingIntent.getActivity(EditNoteActivity.this, NOTIFICATION_ID, intentdDismissNotification, 0);
        //Checking the android version as notifications are made differently in android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "Dismiss", resultPendingIntent).build();
            //Setting the attributes of a notification channel and initializing it
            String CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            //Setting the importance/priority of the notification
            int importance = NotificationManager.IMPORTANCE_LOW;
            //Creating a notification channel
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            //Setting the LED light color to red
            mChannel.setLightColor(Color.RED);
            //Enabling and setting vibration for the notification
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //This is basically not to show the badge in the launcher icon (that dot/Circle/no of how many notifications)
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
            //Initializing the NotificationCompat.Builder
            builder = new NotificationCompat.Builder(EditNoteActivity.this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_icon_notification)
                    //This attribute is set so as to prevent the notification from being dismissed by the user by swiping (i.e. the user can only use the dismiss button So that there is emphasis laid on the fact that he/she has pinned it)
                    .setOngoing(true)
                    //Setting the dismiss button
                    .addAction(action);
            //If title exist for the note the title it is set as the notification's title and note (if exists) is set as the notification's text, else the note is kept as title and notification's text is kept empty
            if (!editTextTitle.getText().toString().isEmpty()) {
                builder.setContentTitle(editTextTitle.getText().toString());
                if (!editTextNote.getText().toString().isEmpty())
                    builder.setContentText(editTextNote.getText().toString());
            } else {
                builder.setContentTitle(editTextNote.getText().toString());
            }
        }
        //If android is below O
        else {
            //Creating the notification
            builder = new NotificationCompat.Builder(EditNoteActivity.this)
                    .setSmallIcon(R.drawable.ic_icon_notification)
                    //This attribute is set so as to prevent the notification from being dismissed by the user by swiping (i.e. the user can only use the dismiss button So that there is emphasis laid on the fact that he/she has pinned it)
                    .setOngoing(true)
                    //Setting the dismiss button
                    .addAction(0, "Dismiss", resultPendingIntent)
                    .setPriority(NotificationManagerCompat.IMPORTANCE_LOW);
            //If title exist for the note the title it is set as the notification's title and note (if exists) is set as the notification's text, else the note is kept as title and notification's text is kept empty
            if (!editTextTitle.getText().toString().isEmpty()) {
                builder.setContentTitle(editTextTitle.getText().toString());
                if (!editTextNote.getText().toString().isEmpty())
                    builder.setContentText(editTextNote.getText().toString());
            } else {
                builder.setContentTitle(editTextNote.getText().toString());
            }
        }
        //Creating the notification with the notification ID
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void dismissNotification(){
        //Trying to dismiss the notification as the user may have already dismissed it
        try {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(noteFromId.getNotificationID());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}



