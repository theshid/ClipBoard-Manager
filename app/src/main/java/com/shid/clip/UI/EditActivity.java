package com.shid.clip.UI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.shid.clip.Database.AppDatabase;
import com.shid.clip.Database.ClipEntry;
import com.shid.clip.R;
import com.shid.clip.Utils.AppExecutor;
import com.shid.clip.Utils.SharedPref;
import com.shid.clip.ViewModel.MainViewModel;

import java.util.List;

public class EditActivity extends AppCompatActivity {
    private Button btn_edit;
    private Button btn_cancel;
    private EditText editText;
    private List<ClipEntry> clipEntries;
    private MainViewModel viewModel;
    private int position;
    private AppDatabase mDb;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPref();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_edit = findViewById(R.id.btn_edit);
        editText = findViewById(R.id.editText_edit);
        mDb = AppDatabase.getInstance(this);
        clipEntries = (List<ClipEntry>)getIntent().getSerializableExtra("list");
        setupViewModel();
        checkIntent();

        final ClipEntry clipEntry = clipEntries.get(position);
        //editText.setText(clipEntry.getEntry());
        editText.append(clipEntry.getEntry());



        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppExecutor.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        String text = editText.getText().toString();
                        int id = clipEntry.getClipId();
                        mDb.clipDao().updateClip(text,id);
                    }
                });
                Toast.makeText(EditActivity.this,"Clip updated",Toast.LENGTH_LONG).show();
                backToMainIntent();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainIntent();
            }
        });
    }

    public void backToMainIntent() {
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void setupViewModel() {
        viewModel = ViewModelProviders.of(EditActivity.this).get(MainViewModel.class);
        viewModel.getClips().observe(this, new Observer<List<ClipEntry>>() {
            @Override
            public void onChanged(@Nullable List<ClipEntry> taskEntries) {
                Log.d("Fragment", "Updating list of tasks from LiveData in ViewModel");
                clipEntries = taskEntries;
            }
        });
    }

    private void checkPref() {
        sharedPref = new SharedPref(this);

        if (sharedPref.loadNightMode()) {
            setTheme(R.style.DarkTheme);

        } else {
            setTheme(R.style.DayTheme);

        }
    }

    private void checkIntent(){
        position = getIntent().getIntExtra("position",0);
    }
}
