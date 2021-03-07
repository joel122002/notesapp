package com.notesapp.notes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class LoggedInActivity extends AppCompatActivity implements View.OnClickListener {
    //Creating instance variables
    //RecyclerView Pinned and others
    private RecyclerView recyclerViewPinned, recyclerViewOthers;
    //swipeRefreshLayout for refreshing on swipe
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton floatingActionButton;
    private EditText editTextSearch;
    private TextView textViewPinned, textViewOthers;
    //Search button
    private ImageView imageViewSearch;
    //LogoutButton button
    private ImageButton imageButtonLogoutUser;
    //During delete this will hold al the selected items IDs
    private ArrayList<Note> noOfSelectedItems;
    //Determines the state whether the activity is in delete state or not
    enum selectedOrNot {
        Yes, No
    }
    //Creating an object of the above enum and setting its default value to No
    selectedOrNot selectedOrNotObject = LoggedInActivity.selectedOrNot.No;
    //Creating an object of type SQLiteDatabase so that various tasks (Uploading to server, Updating, Deleting, Populating the RecyclerViews etc can be done)
    private SQLiteDatabase database;
    //Creating objects of type RecylcerViewsAdapter
    private RecyclerViewAdapter recyclerViewAdapterPinned, recyclerViewAdapterOthers;
    //Creating Lists which will hold Pinned Notes Other Notes and all notes
    private List<Note> notesOthers, notesPinned, allNotes;

    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        //Initializing the instance variables
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerViewPinned = findViewById(R.id.recyclerViewPinned);
        recyclerViewOthers = findViewById(R.id.recyclerViewOthers);
        recyclerViewAdapterPinned = new RecyclerViewAdapter();
        recyclerViewAdapterOthers = new RecyclerViewAdapter();
        recyclerViewPinned.setAdapter(recyclerViewAdapterPinned);
        recyclerViewOthers.setAdapter(recyclerViewAdapterOthers);
        imageViewSearch = findViewById(R.id.imageViewSearch);
        imageButtonLogoutUser = findViewById(R.id.imageButtonLogoutUser);
        editTextSearch = findViewById(R.id.editTextSearch);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        //These TextViews will separate the pinned notes form the other notes
        textViewPinned = findViewById(R.id.textViewPinned);
        textViewOthers = findViewById(R.id.textViewOthers);
        //This ArrayList will hold the selected items when the app is in delete state
        noOfSelectedItems = new ArrayList<>();
        //Initializing the SQLiteDatabase object
        database = new SQLiteDatabase(this);
        //Initializing the allNotes List
        allNotes = new ArrayList<>();
        getAllNotes();
        //Setting up the showcase view for demo
        new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.buttonPlaceHolder))
                .setContentText("Tap this to add a note")
                .setDismissText("GOT IT")
                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse("ADD_NOTE") // provide a unique ID used to ensure it is only shown once
                .show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewPinned.getChildCount() > 0) {
                    try {
                        new MaterialShowcaseView.Builder(LoggedInActivity.this)
                                .setTarget(recyclerViewPinned.getChildAt(0))
                                .setDismissText("GOT IT")
                                .setContentText("Tap and hold a note to delete it")
                                .setDelay(500) // optional but starting animations immediately in onCreate can make them choppy
                                .singleUse("DELETE_NOTE") // provide a unique ID used to ensure it is only shown once
                                .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 10000);
        //Setting up the onEditorActionListener
        editTextSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //If search icon is clicked on the keyboard
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    imageViewSearch.performClick();
                    return true;
                }
                return false;
            }
        });
        //Setting up the OnClickListener
        imageViewSearch.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
        imageButtonLogoutUser.setOnClickListener(this);
        //Setting up an OnClickListener for the child views
        recyclerViewAdapterOthers.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note, View view) {
                onNoteClick(note, view);
            }
        });
        //Setting up an OnClickListener for the child views
        recyclerViewAdapterPinned.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note, View view) {
                onNoteClick(note, view);
            }
        });
        recyclerViewAdapterOthers.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Note note, View view) {
                onNoteLongClick(note, view);
            }
        });
        recyclerViewAdapterPinned.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Note note, View view) {
                onNoteLongClick(note, view);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllNotes();
            }
        });
    }

    //onClick method
    @Override
    public void onClick(View v) {
        //Checking if selectedOrNotObject is yes or no i.e if it is in delete mode or not
        if (selectedOrNotObject == selectedOrNot.No) {
            //If the + icon is clicked
            if (v.getId() == R.id.floatingActionButton) {
                //Transitioning to AddNoteActivity so that the user can add a new note
                Intent gotoAddNote = new Intent(this, AddNoteActivity.class);
                startActivity(gotoAddNote);
            }
            //If search icon is clicked
            else if (v.getId() == R.id.imageViewSearch) {
                //Calling the search method will search for a note
                search();
            }
            //If the user icon is clicked
            else if (v.getId() == R.id.imageButtonLogoutUser) {
                //Creating an AlertDialog to confirm the action of logging out
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
                //Asking the user if he/she is sure that he/she wants to logout
                builder.setMessage("Are you sure you want to logout?")
                        //This AlertDialog cannot be canceled by clicking elsewhere on the screen
                        .setCancelable(false)
                        //Setting the positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Logging out the user
                                ParseUser.logOutInBackground(new LogOutCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            //If successful then transition to LogInActivity
                                            Intent intent = new Intent(LoggedInActivity.this, LogInActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        })
                        //Setting the negative button button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Cancelling the AlertDialog
                                dialog.cancel();
                            }
                        });
                //Creating the AletDialog from the builder
                AlertDialog alert = builder.create();
                //Showing/Displaying the alerDialog
                alert.show();
            }
        }
        //If selectedOrNotObject is yes
        else {
            //If delete button (floatingActionButton) is clicked
            if (v.getId() == R.id.floatingActionButton) {
                //Counting the number of items to display on the AlertDialog
                int noOfItems = 0;
                for (Note note : noOfSelectedItems) {
                    noOfItems++;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
                //Checking if noOfItems > 1 so as to concatenate it's value in the AlertDialog
                if (noOfItems > 1) {
                    //Setting the AlertDialog message for more than one items
                    builder.setMessage("Are you sure you want to delete " + noOfItems + " entries?");
                } else {
                    //Setting the AlertDialog message for one item
                    builder.setMessage("Are you sure you want to delete this entry?");
                }
                //This AlertDialog cannot be canceled by clicking elsewhere on the screen
                builder.setCancelable(false)
                        //Setting the positive button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (final Note note : noOfSelectedItems) {
                                    Note deleteNote = new Note(note.getId(), note.getCreatedAt(), note.getObjectId(), note.getTitle(), note.getNote(), note.getPinned(), note.getToEdit(), "Yes", note.getNotificationID());
                                    database.updateNote(deleteNote);
                                    //database.deleteNote(note);
                                }
                                getAllNotes();
                                setSelectedOrNotObjectToNo();
                                List<Note> deleteNotes = database.getAllNotesToBeDeleted();
                                for (final Note note : deleteNotes) {
                                    ParseQuery<ParseObject> deleteQuery = ParseQuery.getQuery("Notes");
                                    deleteQuery.whereEqualTo("objectId", note.getObjectId());
                                    deleteQuery.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> objects, ParseException e) {
                                            if (e == null) {
                                                if (objects.size() == 0) {
                                                    database.deleteNote(note);
                                                    return;
                                                }
                                                objects.get(0).deleteEventually(new DeleteCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e != null) {
                                                            Note deleteNote = new Note(note.getObjectId(), note.getTitle(), note.getNote(), note.getPinned(), note.getToEdit(), "Yes", note.getNotificationID());
                                                            database.updateNote(deleteNote);
                                                        } else {
                                                            database.deleteNote(note);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        //Setting the Negative button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Cancelling the AlertDialog
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }

    }

    //Method to check if the string contains the other string (i.e the other string is a part of the first string). It is used in the search method
    private static boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }

    private void search() {
        //If search box is not empty
        if (!editTextSearch.getText().toString().isEmpty()) {
            List<Note> foundNotes = new ArrayList<>();
            for (Note searchNote : allNotes) {
                String findInTitle = searchNote.getTitle();
                String findInString = searchNote.getNote();
                //Checking if findInTitle & findInString contain the text in the search box
                if (!containsIgnoreCase(findInString, editTextSearch.getText().toString()) && !containsIgnoreCase(findInTitle, editTextSearch.getText().toString())) {

                } else {
                    foundNotes.add(searchNote);
                }
            }
            recyclerViewPinned.setVisibility(View.GONE);
            textViewPinned.setVisibility(View.GONE);
            textViewOthers.setVisibility(View.GONE);
            recyclerViewAdapterOthers.setNotes(foundNotes);
        }
    }

    @Override
    public void onBackPressed() {
        if (selectedOrNotObject == selectedOrNot.Yes) {
            setSelectedOrNotObjectToNo();
        } else if (editTextSearch.isFocused()) {
            editTextSearch.clearFocus();
            if (!editTextSearch.getText().toString().isEmpty()) {
                editTextSearch.setText("");
                getAllNotes();
            }
        } else {
            finish();
        }
    }

    private void setSelectedOrNotObjectToNo() {
        //Setting selectedOrNot to No
        selectedOrNotObject = selectedOrNot.No;
        //Removing all the circles
        for (int i = 0; i < recyclerViewPinned.getChildCount(); i++) {
            //Trying as some of the IDs may not exist as they are deleted. Doing these operations on a non existing item will cause errors.
            try {
                recyclerViewPinned.getChildAt(i).findViewById(R.id.imageViewBlueTick).setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < recyclerViewOthers.getChildCount(); i++) {
            //Trying as some of the IDs may not exist as they are deleted. Doing these operations on a non existing item will cause errors.
            try {
                recyclerViewOthers.getChildAt(i).findViewById(R.id.imageViewBlueTick).setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Setting the icon of the floatingActionButton to a plus (before it was a dustbin)
        floatingActionButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_input_add));
        noOfSelectedItems.clear();
    }

    private void onNoteClick(Note note, View view) {
        if (selectedOrNotObject == selectedOrNot.No) {
            //Creating an intent to switch to  EditNoteActivity
            Intent intent = new Intent(LoggedInActivity.this, EditNoteActivity.class);
            if (note.getPinned() == 1) {
                intent.putExtra("isPinned", true);
            }
            //Try to pass the Title as the title may be empty
            try {
                intent.putExtra("Title", note.getTitle());
            } catch (Exception e) {
                intent.putExtra("Title", "");
            }
            //Try to pass the Note as the note may be empty
            try {
                intent.putExtra("Note", note.getNote());
            } catch (Exception e) {
                intent.putExtra("Note", "");
            }
            try {
                intent.putExtra("objectId", note.getObjectId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            intent.putExtra("objectId", note.getObjectId());
            intent.putExtra("id", note.getId());
            startActivity(intent);
        }
        //Else i.e. if it is in delete mode
        else {
            try {
                //Trying to get the tag as the tag might be null and so it might produce a Null Pointer Exception. If it is equal to "Yes" then remove the note form the ArrayList of selected item and replace the blue tick with an empty circle
                if (view.getTag() != null && view.getTag().equals("Yes")) {
                    //Removing the ID of the view from the noOfSelectedItems ArrayList which holds the IDs of the selected items
                    noOfSelectedItems.remove(note);
                    //Setting the tag back to the original ObjectId
                    view.setTag("");
                    //Setting the empty circle as the ImageDrawable to show that it is deselected
                    ((ImageView) view.findViewById(R.id.imageViewBlueTick)).setImageDrawable(getResources().getDrawable(R.drawable.rounded_box));
                    Toast.makeText(LoggedInActivity.this, noOfSelectedItems.size() + "", Toast.LENGTH_SHORT).show();
                }
                //If the tag is not equal to "Yes" then add it from the ArrayList od selected items
                else {
                    //Adding the ID of the view from the noOfSelectedItems ArrayList which holds the IDs of the selected items
                    noOfSelectedItems.add(note);
                    //Modifying the tag as now it is selected
                    view.setTag("Yes");
                    //Setting the tick as the ImageDrawable to show that it is selected
                    ((ImageView) view.findViewById(R.id.imageViewBlueTick)).setImageDrawable(getResources().getDrawable(R.drawable.blue_tick));
                }
            }
            //Catch the error if any
            catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
            //If there is no selected note then set it back to normal mode i.e. add and edit mode and not delete mode
            if (noOfSelectedItems.size() == 0) {
                setSelectedOrNotObjectToNo();
            }
        }
    }

    private void onNoteLongClick(Note note, View view) {
        if (selectedOrNotObject == selectedOrNot.No) {
            //Setting to delete mode
            selectedOrNotObject = selectedOrNot.Yes;
            //Changing the plus icon on  to a dustbin
            floatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_black_24dp));
            //Adding that item to noOfSelectedItems that holds the IDs of all selected items
            noOfSelectedItems.add(note);
            view.setTag("Yes");
            //Making the ImageView visible (It's default is gone)
            view.findViewById(R.id.imageViewBlueTick).setVisibility(View.VISIBLE);
            //Setting the ImageView's ImageDrawable to a tick
            ((ImageView) view.findViewById(R.id.imageViewBlueTick)).setImageDrawable(getResources().getDrawable(R.drawable.blue_tick));
            //Making all ImageViews visible. (It has a default drawable of an empty circle indicating that it is not selected)
            for (int i = 0; i < recyclerViewOthers.getChildCount(); i++) {
                recyclerViewOthers.getChildAt(i).findViewById(R.id.imageViewBlueTick).setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < recyclerViewPinned.getChildCount(); i++) {
                recyclerViewPinned.getChildAt(i).findViewById(R.id.imageViewBlueTick).setVisibility(View.VISIBLE);
            }
        }
    }

    private void getAllNotes() {
        uploadNoteToServer();
        updateNoteInServer();
        deleteNoteFromServer();
        ParseQuery<ParseObject> toSaveQuery = ParseQuery.getQuery("Notes");
        toSaveQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        toSaveQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    //Creating a list of object id of notes that either have to be updated or have to be deleted i.e. toEdit or toDelete = "Yes"
                    List<String> idsToSubtract = database.getAllObjectIdsToBeUploadedOrDeleted();
                    //Getting all the objects that are present on the server and have not to be updated or deleted i.e. toEdit or toDelete = "No"
                    List<String> idsToDelete = database.getAllObjectIdsToBeReplaced();
                    //An arraylist that will hold the remaining ids that will be added later to the database
                    List<Note> subtractedList = new ArrayList<>();
                    for (ParseObject object : objects) {
                        //If the id is not there in idsToSubtract then add it to the subtractedList i.e. do not add the common elements but add the element that is not there in idsToSubtract list
                        if (!idsToSubtract.contains(object.getObjectId())) {
                            Note addNote;
                            if ((boolean) object.get("isPinned")) {
                                addNote = new Note((Date) object.get("createdAtDate"), object.getObjectId(), object.get("title").toString(), object.get("note").toString(), 1, "No", "No", -1);
                            } else {
                                addNote = new Note((Date) object.get("createdAtDate"), object.getObjectId(), object.get("title").toString(), object.get("note").toString(), 0, "No", "No", -1);
                            }
                            //Adding that note to subtractedList
                            subtractedList.add(addNote);
                        }
                    }
                    Note note;
                    for (String id : idsToDelete) {
                        note = database.getByObjectID(id);
                        database.deleteNote(note);
                    }
                    for (Note noteToAdd : subtractedList) {
                        database.addNoteWithObjectID(noteToAdd);
                    }
                    setNotes();
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        setNotes();
    }

    private void uploadNoteToServer() {
        //Getting all notes that have to be uploaded i.e. they exist in the local database but not in the server. If it is able to upload the note successfully then it will update the note in the local database by setting an object Id for the note
        List<Note> toUploadNotes = database.getAllNotesToBeUploaded();
        if (toUploadNotes != null && toUploadNotes.size() > 0) {
            for (final Note note : toUploadNotes) {
                final ParseObject newNote = new ParseObject("Notes");
                newNote.put("createdAtDate", note.getCreatedAt());
                newNote.put("title", note.getTitle());
                newNote.put("note", note.getNote());
                newNote.put("createdAt", note.getCreatedAt());
                newNote.put("username", ParseUser.getCurrentUser().getUsername());
                if (note.getPinned() == 1) {
                    newNote.put("isPinned", true);
                } else {
                    newNote.put("isPinned", false);
                }
                newNote.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            //Updating the note if upload is successful and giving it an object ID
                            Note updateExisting = new Note(note.getId(), note.getCreatedAt(), newNote.getObjectId(), note.getTitle(), note.getNote(), note.getPinned(), "No", "No", note.getNotificationID());
                            database.updateNote(updateExisting);
                        }
                    }
                });
            }
        }
    }

    private void updateNoteInServer() {
        //Getting all notes that have to be updated i.e. they were updated in the local database but not in the server because of connectivity issues. If it is able to upload the note successfully then it will update the note in the local database by setting an toEdit to "No" for the note
        List<Note> unsavedNotes = database.getAllNotesToBeUpdated();
        if (unsavedNotes != null && unsavedNotes.size() > 0) {
            for (final Note note : unsavedNotes) {
                ParseQuery<ParseObject> updateQuery = new ParseQuery<ParseObject>("Notes");
                updateQuery.whereEqualTo("objectId", note.getObjectId());
                updateQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        objects.get(0).put("title", note.getTitle());
                        objects.get(0).put("note", note.getNote());
                        if (note.getPinned() == 1)
                            objects.get(0).put("isPinned", true);
                        else
                            objects.get(0).put("isPinned", false);
                        objects.get(0).saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    //Updating the note if upload is successful by setting toEdit to false
                                    Note updateNote = new Note(note.getId(), note.getCreatedAt(), note.getObjectId(), note.getTitle(), note.getNote(), note.getPinned(), "No", "No", note.getNotificationID());
                                    database.updateNote(updateNote);
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void deleteNoteFromServer() {
        //Getting all notes that have to be deleted i.e. they were deleted in the local database but not in the server because of connectivity issues. If it is able to delete the note successfully then it will delete the note from the local database.
        List<Note> deleteNotes = database.getAllNotesToBeDeleted();
        for (final Note note : deleteNotes) {
            ParseQuery<ParseObject> deleteQuery = ParseQuery.getQuery("Notes");
            deleteQuery.whereEqualTo("objectId", note.getObjectId());
            deleteQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() == 0) {
                            database.deleteNote(note);
                            return;
                        }
                        objects.get(0).deleteEventually(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Note deleteNote = new Note(note.getId(), note.getCreatedAt(), note.getObjectId(), note.getTitle(), note.getNote(), note.getPinned(), note.getToEdit(), "Yes", note.getNotificationID());
                                    database.updateNote(deleteNote);
                                } else {
                                    //Deleting the note from the server
                                    database.deleteNote(note);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void setNotes() {
        //Populating notesPinned List and setting it as the list to populate the RecyclerView by passing it as a parameter to the setNotes function of the RecyclerViewAdapter
        notesOthers = database.getAllOtherNotes();
        recyclerViewPinned.setVisibility(View.VISIBLE);
        recyclerViewAdapterOthers.setNotes(notesOthers);
        notesPinned = database.getAllPinnedNotes();
        recyclerViewAdapterPinned.setNotes(notesPinned);
        //If Pinned and Other notes exist then set the visibility of the TextViews to VISIBLE. These hold the the texts Pinned and Others
        if (notesOthers.size() > 0 && notesPinned.size() > 0) {
            textViewOthers.setVisibility(View.VISIBLE);
            textViewPinned.setVisibility(View.VISIBLE);
        }
        //If not then setting it to gone
        else {
            textViewOthers.setVisibility(View.GONE);
            textViewPinned.setVisibility(View.GONE);
        }
        allNotes.clear();
        //Combining noteOthers and notePinned to form allNotes List
        allNotes.addAll(notesOthers);
        allNotes.addAll(notesPinned);
    }
}