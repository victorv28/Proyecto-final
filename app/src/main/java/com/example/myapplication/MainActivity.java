package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;



public class MainActivity extends Activity implements Communicator {
    PlayerFragment playerFrag;
    SongListFragment songListFragment;
    MiniPlayerFragment miniPlayerFragment;
    ArrayList<Song> songFiles;
    private MusicService musicSvc;
    private Intent playIntent;
    private SongCompletedListener songCompletedListener;
    private Toast vol_toast;
    FragmentManager manager;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (savedInstanceState == null) {
            playerFrag = new PlayerFragment();
            songListFragment = new SongListFragment();
            miniPlayerFragment = new MiniPlayerFragment();
            songFiles = (ArrayList<Song>) getIntent().getSerializableExtra("songs");
            Collections.sort(songFiles);
            vol_toast = Toast.makeText(getBaseContext(), "Volume", Toast.LENGTH_LONG);
        }
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    //Connecta al servicio que maneja las operaciones del reproductor
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSvc = binder.getService();

            //Pasa la lista de canciones al servicio

            musicSvc.setList(songFiles);
            musicSvc.setSong(0);
            show_list();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //Crea el menu del toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }

    //Maneja las opciones del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rescan:
                Intent rescan = new Intent(MainActivity.this, SplashActivity.class);
                rescan.putExtra("rescan", true);
                startActivity(rescan);
                finish();
                break;
            case R.id.action_exit:
                finish();
                break;
        }
        return false;
    }

    //Maneja los controles de repetir y shuffle
    @Override
    public void playback_mode(int id, boolean status) {
        switch (id) {
            case R.id.tbRep:
                musicSvc.repeatSongs(status, get_song_id());
                break;
            case R.id.tbShuf:
                musicSvc.shuffleSongs(status);
                break;
        }
    }

    //Maneja los controles del reproductor
    @Override
    public void song_operations(int id) {
        switch (id) {
            case R.id.bPlay:
                togglePlayPause();
                break;
            case R.id.bNext:
                musicSvc.nextSong();
                updateSongInfo();
                break;
            case R.id.bPrev:
                musicSvc.prevSong();
                updateSongInfo();
                break;

                //Vuelve a la lista
            case R.id.bBrowse:
                show_list();
                break;

                //Desplega el controlador de volumen
            case R.id.bVolume:
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                break;
        }
    }


    //Actualiza los datos de la cancion seleccionada
    private void updateSongInfo() {
        if (playerFrag.isVisible()) {
            playerFrag.updateAlbumArt();
            playerFrag.updateTags();
            playerFrag.setMaxDuration(musicSvc.getDuration());
        } else if (miniPlayerFragment.isVisible()) {
            miniPlayerFragment.updateTags();
            songListFragment.refreshList();
        }
    }

    private void togglePlayPause() {
        musicSvc.togglePlayPause();
    }


    //Abre el reproductor al abrir una cancion
    @Override
    public void open_song(int position) {
        musicSvc.setStartIndex(position);
        musicSvc.setSong(position);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.addToBackStack(null);

        transaction.commit();
        musicSvc.playSong();
    }

    //Abre la lista y el mini-reproductor
    public void show_list() {
        manager = getFragmentManager();
        if (manager.getBackStackEntryCount() > 0)
            manager.popBackStack();
        else {
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.remove(playerFrag);

            transaction.add(R.id.container, songListFragment);
            transaction.add(R.id.container, miniPlayerFragment);
            transaction.commit();
        }
    }

    @Override
    public ArrayList<Song> get_song_list() {
        return songFiles;
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        unbindService(musicConnection);
        musicSvc = null;
        super.onDestroy();
    }


    //Maneja la barra de tiempo
    @Override
    public void set_progress(int i) {
        musicSvc.seekTo(i);
    }

    //Muestra el volumen actual
    @Override
    public void set_volume(float diff_vol) {
        float current_vol = musicSvc.getVolume();
        float new_vol = current_vol + diff_vol;
        if (new_vol <= 0.0f)
            new_vol = 0.0f;
        if (new_vol >= 1.0f)
            new_vol = 1.0f;
        Log.d("Volume Diff", String.valueOf(diff_vol));
        String vol_text = "Volumen: " + Math.round(new_vol / 1.0f * 100) + "%";
        vol_toast.setText(vol_text);
        vol_toast.show();
        musicSvc.setVolume(new_vol);
    }

    //Abre el reproductor al presionar el mini-reproductor
    @Override
    public void goToPlayer() {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    //getters y seters
    @Override
    public String get_artist() {
        return songFiles.get(musicSvc.playingIndex()).getArtist();
    }

    @Override
    public String get_album() {
        return songFiles.get(musicSvc.playingIndex()).getAlbum();
    }

    @Override
    public String get_title() {
        return songFiles.get(musicSvc.playingIndex()).getTitle();
    }

    @Override
    public byte[] get_album_art() {
        return songFiles.get(musicSvc.playingIndex()).getAlbum_Art(getBaseContext());
    }

    @Override
    public int get_song_id() {
        return musicSvc.playingIndex();
    }

    @Override
    public int get_duration() {
        return musicSvc.getDuration();
    }

    @Override
    public int get_elapsed() {
        return musicSvc.getElapsed();
    }

    @Override
    public boolean is_playing() {
        return musicSvc.isPlaying();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (songCompletedListener == null)
            songCompletedListener = new SongCompletedListener();
        IntentFilter intentFilter = new IntentFilter("Refresh the Song Info");
        registerReceiver(songCompletedListener, intentFilter);
        updateSongInfo();
    }

    @Override
    protected void onPause() {
        if (songCompletedListener != null) unregisterReceiver(songCompletedListener);
        super.onPause();
    }

    private class SongCompletedListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Refresh the Song Info")) {
                updateSongInfo();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (manager.getBackStackEntryCount() > 0)
            manager.popBackStack();
        else
            moveTaskToBack(true);
    }
}
