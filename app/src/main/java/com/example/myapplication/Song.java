package com.example.myapplication;

import android.content.ContentUris;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.Serializable;

public class Song implements Serializable, Comparable<Song> {
    private long id;
    private String file_name;
    private String title;
    private String artist;
    private String album;
    private String duration;

    public Song(long id, String title, String file_name, String artist, String album, String duration) {
        this.id = id;
        this.title = title;
        this.file_name = file_name;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public String getDuration() {
        String minutes = String.valueOf(Integer.parseInt(duration) / 60000);
        String seconds = String.valueOf((Integer.parseInt(duration) / 1000) % 60);
        if (minutes.length() < 2) {
            minutes = "0" + minutes;
        }
        if (seconds.length() < 2) {
            seconds = "0" + seconds;
        }
        return minutes + ":" + seconds;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        if (title.equals("")) {
            return file_name.substring(0, file_name.length() - 4);
        }
        return title;
    }

    public String getArtist() {
        if (artist.equals("<unknown>")) {
            return "Artista Desconocido";
        }
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public byte[] getAlbum_Art(Context context) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context, getUri());
        return mediaMetadataRetriever.getEmbeddedPicture();
    }

    public Uri getUri() {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    @Override
    public int compareTo(Song song) {
        return this.title.toUpperCase().compareTo(song.getTitle().toUpperCase());
    }
}
