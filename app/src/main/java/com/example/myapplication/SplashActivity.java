package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class SplashActivity extends Activity {
    ProgressBar pbLoading;
    TextView tvFound;
    ArrayList<Song> songFiles;
    int numFilesFound;
    boolean exited;
    Cursor musicCursor = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();
        pbLoading = findViewById(R.id.pbLoading);
        tvFound = findViewById(R.id.tvFound);
        numFilesFound = 0;
        songFiles = new ArrayList<>();
        tvFound.setText("No se ha encontrado nada...");
        exited = false;
        new AsyncContentResolve().execute();
    }

    private void fetch_list() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


        // Enable if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            musicCursor = musicResolver.query(musicUri, null, null, null, null);
        }
        // Else ask for permission
        else {
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbLoading.setMax(musicCursor.getCount());
            }
        });

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int displayNameColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisFileName = musicCursor.getString(displayNameColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisDuration = musicCursor.getString(durationColumn);

                try {
                   Thread.sleep(30);
               } catch (InterruptedException e) {
                   e.printStackTrace();
                }

                // Filter out songs less than 5 seconds in length.
                if (thisDuration != null && Integer.parseInt(thisDuration) > 5000) {
                    songFiles.add(new Song(thisId, thisTitle, thisFileName, thisArtist, thisAlbum, thisDuration));
                    numFilesFound++;
                }
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        tvFound.setText(numFilesFound + " Archivos encontrados...");
                        pbLoading.setProgress(numFilesFound);
                    }
                });
                if (exited)
                    break;
            }
            while (musicCursor.moveToNext());
            runOnUiThread(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    tvFound.setText(numFilesFound + " Archivos encontrados...");
                    pbLoading.setProgress(musicCursor.getCount());
                }
            });
        }
    }

    @Override
    protected void onStop() {
        exited = true;
        finish();
        super.onStop();
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncContentResolve extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exited) {
                return;
            }

            Intent main = new Intent(getBaseContext(), MainActivity.class);
            main.putExtra("songs", songFiles);
            startActivity(main);
            finish();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            fetch_list(); // Call to the content resolver.
            return null;
        }
    }
}
