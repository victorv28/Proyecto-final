package com.example.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class SongListFragment extends Fragment implements AdapterView.OnItemClickListener {
    ArrayList<Song> songFiles;
    SongListAdapter songAdapter;
    ListView lvSongs;
    Communicator comm;
    int songId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songlist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        lvSongs = getActivity().findViewById(R.id.lvSongs);
        lvSongs.setOnItemClickListener(this);

        songFiles = comm.get_song_list();
        songId = comm.get_song_id();
        songAdapter = new SongListAdapter(songFiles);
        lvSongs.setAdapter(songAdapter);
        lvSongs.setSelectionFromTop(songId, lvSongs.getHeight() / 2);
        Log.e("MP3 files found...", String.valueOf(songFiles.size()));
    }

    public void refreshList() {
        songId = comm.get_song_id();
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        comm.open_song(i);
    }

    static class ViewHolderItem {
        TextView titleHolder;
        TextView artistHolder;
        TextView durationHolder;
        ImageView imageHolder;
    }

    public class SongListAdapter extends BaseAdapter implements SectionIndexer {
        ArrayList<Song> songs;
        HashMap<String, Integer> mapSectionToPosn;
        SparseArray<String> mapPosnToSection;
        String[] sections;

        SongListAdapter(ArrayList<Song> songs) {
            this.songs = songs;
            mapSectionToPosn = new HashMap<>();
            mapPosnToSection = new SparseArray<>();
            for (int i = 0; i < songs.size(); i++) {
                String firstchar = songs.get(i).getTitle().substring(0, 1);
                firstchar = firstchar.toUpperCase(Locale.US);
                if (firstchar.matches("[0-9]"))
                    firstchar = "#";
                if (!mapSectionToPosn.containsKey(firstchar))
                    mapSectionToPosn.put(firstchar, i);
                mapPosnToSection.put(i, firstchar);
            }
            Set<String> sectionLetters = mapSectionToPosn.keySet();

            ArrayList<String> sectionList = new ArrayList<>(sectionLetters);


            Collections.sort(sectionList);

            sections = new String[sectionList.size()];

            sectionList.toArray(sections);
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int i) {
            return songs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolderItem viewHolder;
            if (view == null) {


                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.list_item_song, viewGroup, false);

                viewHolder = new ViewHolderItem();
                viewHolder.imageHolder = view.findViewById(R.id.ivListItem);
                viewHolder.titleHolder = view.findViewById(R.id.tvSongTitle_ListItem);
                viewHolder.artistHolder = view.findViewById(R.id.tvArtist_ListItem);
                viewHolder.durationHolder = view.findViewById(R.id.tvDuration);

                view.setTag(viewHolder);
            } else {

                viewHolder = (ViewHolderItem) view.getTag();
            }
            viewHolder.titleHolder.setText(songs.get(i).getTitle());
            viewHolder.artistHolder.setText(songs.get(i).getArtist());
            viewHolder.durationHolder.setText(songs.get(i).getDuration());

            if (songs.get(i).getId() == songFiles.get(songId).getId()) {
                viewHolder.imageHolder.setImageResource(R.drawable.playing_icon);
            } else
                viewHolder.imageHolder.setImageResource(R.drawable.song_icon);
            return view;
        }

        @Override
        public String[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {

            return mapSectionToPosn.get(sections[section]);
        }

        @Override
        public int getSectionForPosition(int position) {
            for (int i = 0; i < sections.length; i++) {
                if (mapPosnToSection.get(position).equals(sections[i])) {

                    return i;
                }
            }
            return 0;
        }
    }
}
