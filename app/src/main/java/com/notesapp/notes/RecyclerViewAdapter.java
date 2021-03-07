package com.notesapp.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.NoteHolder> {
    //Creating instance variables
    //This list is responsible to update the RecyclerView
    private List<Note> notes = new ArrayList<>();
    //Creating the onClick and onLongClick for the child views
    private OnItemClickListener listener;
    private OnItemLongClickListener listenerLongClick;

    @NonNull
    @Override
    //This method is responsible for inflating the CardView so that it can later be added to the recyclerView
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_box, parent, false);
        //Returning the view which will later be added to the RecyclerView in the onBing ViewHolder
        return new NoteHolder(itemView);
    }

    //This method is responsible for adding the text to the view and adding it to the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        //We're getting the note on that specific position and setting the title and note on the respective TextViews
        Note note = notes.get(position);
        holder.textViewTitle.setText(note.getTitle());
        holder.textViewNote.setText(note.getNote());
    }

    //Getting the number of items in the RecyclerView i.e. it will be equal to the size of the list
    @Override
    public int getItemCount() {
        return notes.size();
    }

    //Setting the notes List so that the RecyclerView can be populated
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    //The NoteHolder Class extends the ViewHolder class. The ViewHolder class in the class that inflates the layout
    class NoteHolder extends RecyclerView.ViewHolder {
        //We're declaring the TextViews present in the inflated layout i.e. my_box.xml
        private TextView textViewTitle;
        private TextView textViewNote;
        //Calling the constructor
        public NoteHolder(@NonNull View itemView) {
            //Calling the constructor of the super/parent class
            super(itemView);
            //Initializing the TextViews
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            //Setting an onClickListener for my_box.xml (i.e. the CardView)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Getting the position of the current View
                    int position = getAdapterPosition();
                    //Checking if the listener is not null and the position is valid
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.onItemClick(notes.get(position), view);
                }
            });
            //Setting an onLongClickListener for my_box.xml (i.e. the CardView)
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //Getting the position of the current View
                    int position = getAdapterPosition();
                    //Checking if the listener is not null and the position is valid
                    if (listenerLongClick != null && position != RecyclerView.NO_POSITION)
                        listenerLongClick.onItemLongClick(notes.get(position), view);
                    return true;
                }
            });
        }
    }

    //Creating an interface for the onClick functionality
    public interface OnItemClickListener {
        //This method provides the note and View that is clicked on so that we can use it in the method
        void onItemClick(Note note, View view);
    }

    //This method will set accept a listener so that the he Clicks can be detected
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //Creating an interface for the onLongClick functionality
    public interface OnItemLongClickListener {
        //This method provides the note and View that is clicked on so that we can use it in the method
        void onItemLongClick(Note note, View view);
    }

    //This method will set accept a listener so that the he long clicks can be detected
    public void setOnItemLongClickListener(OnItemLongClickListener listenerLongClick) {
        this.listenerLongClick = listenerLongClick;
    }
}
