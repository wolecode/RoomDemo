package com.example.pavneet_singh.roomdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.pavneet_singh.roomdemo.adapter.NotesAdapter;
import com.example.pavneet_singh.roomdemo.notedb.NoteDatabase;
import com.example.pavneet_singh.roomdemo.notedb.model.Note;

import java.lang.ref.WeakReference;
import java.util.List;

public class NoteListActivity extends AppCompatActivity implements NotesAdapter.OnNoteItemClick{

    private TextView textViewMsg;
    private RecyclerView recyclerView;
    private NoteDatabase noteDatabase;
    private List<Note> notes;
    private NotesAdapter notesAdapter;
    private int pos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeVies();
        displayList();
    }

    private void displayList(){
        noteDatabase = NoteDatabase.getInstance(NoteListActivity.this);
        new RetrieveTask(this).execute();
    }

     private static class RetrieveTask extends AsyncTask<Void,Void,List<Note>>{

        private WeakReference<NoteListActivity> activityReference;

        // only retain a weak reference to the activity
        RetrieveTask(NoteListActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Note> doInBackground(Void... voids) {
            if (activityReference.get()!=null)
                return activityReference.get().noteDatabase.getNoteDao().getNotes();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            if (notes!=null && notes.size()>0 ){
                activityReference.get().notes = notes;
                // hides empty text view
                activityReference.get().textViewMsg.setVisibility(View.GONE);

                // create and set the adapter on RecyclerView instance to display list
                activityReference.get().notesAdapter = new NotesAdapter(notes,activityReference.get());
                activityReference.get().recyclerView.setAdapter(activityReference.get().notesAdapter);
            }
        }
    }

    private void initializeVies(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewMsg =  (TextView) findViewById(R.id.tv__empty);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(NoteListActivity.this));
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(NoteListActivity.this,AddNoteActivity.class),100);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 ){
            if( resultCode == 1){
                notes.add((Note) data.getSerializableExtra("note"));
                if (textViewMsg.getVisibility()== View.VISIBLE)
                    textViewMsg.setVisibility(View.GONE);
            }else if( resultCode == 2){
                notes.set(pos,(Note) data.getSerializableExtra("note"));
            }
            notesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNoteClick(final int pos) {
            new AlertDialog.Builder(NoteListActivity.this)
            .setTitle("Select Options")
            .setItems(new String[]{"Delete", "Update"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i){
                        case 0:
                            noteDatabase.getNoteDao().deleteNote(notes.get(pos));
                            notes.remove(pos);
                            notesAdapter.notifyDataSetChanged();
                            break;
                        case 1:
                            NoteListActivity.this.pos = pos;
                            startActivityForResult(
                                    new Intent(NoteListActivity.this,
                                            AddNoteActivity.class).putExtra("note",notes.get(pos)),
                                    100);

                            break;
                    }
                }
            }).show();

    }

    @Override
    protected void onDestroy() {
        noteDatabase.cleanUp();
        super.onDestroy();
    }
}