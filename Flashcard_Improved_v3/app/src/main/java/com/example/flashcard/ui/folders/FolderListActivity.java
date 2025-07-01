package com.example.flashcard.ui.folders;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.flashcard.R;
import com.example.flashcard.db.DBHandler;
import com.example.flashcard.modal.FolderModal;
import com.example.flashcard.ui.form.FolderFormActivity;

import java.util.ArrayList;

public class FolderListActivity extends AppCompatActivity implements FolderListAdapter.OnFolderDeletedListener {
    private ArrayList<FolderModal> folderModalArrayList;
    private DBHandler dbHandler;
    private FolderListAdapter cardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        folderModalArrayList = new ArrayList<>();
        dbHandler = new DBHandler(this);
        folderModalArrayList = dbHandler.getFolderNames();

        if (folderModalArrayList.isEmpty()) {
            findViewById(R.id.emptyFolderListMessage).setVisibility(View.VISIBLE);
        }
        else{
            findViewById(R.id.emptyFolderListMessage).setVisibility(View.GONE);
        }

        RecyclerView recyclerViewDashboard = findViewById(R.id.foldersListRecyclerView);
        recyclerViewDashboard.setLayoutManager(new LinearLayoutManager(this));

        cardAdapter = new FolderListAdapter(folderModalArrayList, this, this);
        recyclerViewDashboard.setAdapter(cardAdapter);

        Button createFolderButton = findViewById(R.id.createFolderButton);
        createFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFolder();
            }
        });
    }

    public void createFolder() {
        Intent intent = new Intent(this, FolderFormActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFolderDeleted(ArrayList<FolderModal> updatedData) {
        cardAdapter.updateData(updatedData);
    }
}