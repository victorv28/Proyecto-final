package com.example.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MiniPlayerFragment extends Fragment implements View.OnClickListener {
    RelativeLayout rlMiniPlayer;
    TextView tvSongTitle;
    TextView tvArtist;
    Button bPlayPause;
    Communicator comm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rlMiniPlayer = getActivity().findViewById(R.id.rlPlayer);
        tvSongTitle = getActivity().findViewById(R.id.tvSongTitle_MiniPlayerFrag);
        tvArtist = getActivity().findViewById(R.id.tvArtist_MiniPlayerFrag);
        bPlayPause = getActivity().findViewById(R.id.bPlay_MiniPlayerFrag);
        rlMiniPlayer.setOnClickListener(this);
        tvSongTitle.setSelected(true);
        tvArtist.setSelected(true);
        bPlayPause.setOnClickListener(this);
        comm = (Communicator) getActivity();

        //Muestra el boton de play/pause cuando se abra una cancion
        if (comm.is_playing()) {
            bPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
        else{
            bPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        updateTags();
    }


    //Muestra el titulo y el autor
    public void updateTags() {
        tvArtist.setText(comm.get_artist());
        tvSongTitle.setText(comm.get_title());
    }

    //Maneja las opciones del mini-reproductor
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //abre el reproductor
            case R.id.rlPlayer:
                comm.goToPlayer();
                break;
                //Maneja el boton de play/pause
            case R.id.bPlay_MiniPlayerFrag:
                comm.song_operations(R.id.bPlay);
                if (comm.is_playing()) {
                    bPlayPause.setBackgroundResource(android.R.drawable.ic_media_pause);
                }
                else{
                    bPlayPause.setBackgroundResource(android.R.drawable.ic_media_play);
                }
                break;
        }
    }
}
