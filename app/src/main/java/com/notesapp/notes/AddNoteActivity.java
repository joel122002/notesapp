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

public class AddNoteActivity extends AppCompatActivity {
    //Creating instance variables
    private EditText editTextAddNote, editTextAddTitle;
    //Notification Builder for building//displaying a notification that is pinned by a user
    private NotificationCompat.Builder builder;
    //A unique notification ID that creates a unique integer based on the system time
    private int NOTIFICATION_ID = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    private SQLiteDatabase database;

    //OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        //Setting up the title for the ActionBar
        setTitle("Add a note");
        //Initializing the EditTexts for the Title and Note
        editTextAddNote = findViewById(R.id.editTextAddNote);
        editTextAddTitle = findViewById(R.id.editTextAddTitle);
        database = new SQLiteDatabase(this);
    }

    //Creating a menu in the ActionBar for pinning and saving
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Setting up an onOptionsItemSelected for listening to the clicks on the menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) {
            //method to add an new note
            addNewNote();
        } else if (item.getItemId() == R.id.notify) {
            //Method to pin a new Note
            pinNewNote();
        }
        return super.onOptionsItemSelected(item);
    }

    //Method to add a note to the server
    private void addNewNote() {
        //Checking if atleast one of the EditTexts (Title or Note) is not empty i.e. is filled
        if (!editTextAddTitle.getText().toString().isEmpty() || !editTextAddNote.getText().toString().isEmpty()) {
            Note newNote = new Note(null, editTextAddTitle.getText().toString().trim(), editTextAddNote.getText().toString().trim(), 0, "No", "No", -1);
            Toast.makeText(AddNoteActivity.this, "Note Added", Toast.LENGTH_SHORT).show();
            database.addNote(newNote);
            //Switching to LoggedInActivity and clearing the previous LoggedInActivity which was not destroyed (It was not destroyed so that the user could go back to the previous activity if he had entered this activity by mistake)
            Intent addnote = new Intent(AddNoteActivity.this, LoggedInActivity.class);
            addnote.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(addnote);
            finish();
        }
    }

    //Method that will pin the note and create a non dismiss-able notification and also save it
    private void pinNewNote() {
        //Checking if atleast one of the EditTexts (Title or Note) is not empty i.e. is filled
        if (!editTextAddTitle.getText().toString().isEmpty() || !editTextAddNote.getText().toString().isEmpty()) {
            final Note newNote = new Note(null, editTextAddTitle.getText().toString().trim(), editTextAddNote.getText().toString().trim(), 1, "No", "No", NOTIFICATION_ID);
            database.addNote(newNote);
            Toast.makeText(AddNoteActivity.this, "Note Added", Toast.LENGTH_SHORT).show();
            //Creating a notification by creating a NotificationManager and initializing it
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //Creating an intent that will dismiss the notification and passing the NOTIFICATION_ID as an IntExtra so that it can be dismissed in the Notification Activity
            Intent intentDismissNotification = new Intent(AddNoteActivity.this, NotificationActivity.class);
            intentDismissNotification.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);
            //Creating a PendingIntent which wil be called when the User clicks on the Dismiss Button in the notification. The NOTIFICATION_ID is set as the request code as the request code must be unique for all notification. If it is not unique and there are multiple notifications we will only be able to dismiss the most recent notification
            PendingIntent resultPendingIntent = PendingIntent.getActivity(AddNoteActivity.this, NOTIFICATION_ID, intentDismissNotification, 0);
            //Checking the android version as notifications are made differently in android O and above
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
                //Creating the notification channel
                notificationManager.createNotificationChannel(mChannel);
                //Initializing the NotificationCompat.Builder
                builder = new NotificationCompat.Builder(AddNoteActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_icon_notification)
                        //This attribute is set so as to prevent the notification from being dismissed by the user by swiping (i.e. the user can only use the dismiss button So that there is emphasis laid on the fact that he/she has pinned it)
                        .setOngoing(true)
                        //Setting the dismiss button
                        .addAction(0, "Dismiss", resultPendingIntent);
                //If title exist for the note the title it is set as the notification's title and note (if exists) is set as the notification's text, else the note is kept as title and notification's text is kept empty
                if (!newNote.getTitle().isEmpty()) {
                    builder.setContentTitle(newNote.getTitle());
                    if (!newNote.getNote().isEmpty())
                        builder.setContentText(newNote.getNote());
                } else {
                    builder.setContentTitle(newNote.getNote());
                }
            }
            //If android is below O
            else {
                //Creating the notification
                builder = new NotificationCompat.Builder(AddNoteActivity.this)
                        .setSmallIcon(R.drawable.ic_icon_notification)
                        //This attribute is set so as to prevent the notification from being dismissed by the user by swiping (i.e. the user can only use the dismiss button So that there is emphasis laid on the fact that he/she has pinned it)
                        .setOngoing(true)
                        //Setting the dismiss button
                        .addAction(0, "Dismiss", resultPendingIntent)
                        .setPriority(NotificationManagerCompat.IMPORTANCE_LOW);
                //If title exist for the note the title it is set as the notification's title and note (if exists) is set as the notification's text, else the note is kept as title and notification's text is kept empty
                if (!newNote.getTitle().isEmpty()) {
                    builder.setContentTitle(newNote.getTitle());
                    if (!newNote.getNote().isEmpty())
                        builder.setContentText(newNote.getNote());
                } else {
                    builder.setContentTitle(newNote.getNote());
                }
            }
            //Creating the notification with the notification ID
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            //Switching to LoggedInActivity after pinning and notifying the user
            Intent addnote = new Intent(AddNoteActivity.this, LoggedInActivity.class);
            addnote.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(addnote);
            finish();
        }
    }
}

