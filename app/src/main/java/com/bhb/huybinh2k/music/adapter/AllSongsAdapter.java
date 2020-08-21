package com.bhb.huybinh2k.music.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;

import java.text.SimpleDateFormat;
import java.util.List;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.ViewHolder> {
    private Context mContext;
    private int mRes;
    private List<Song> mList;
    private int mPlayingPosition = -1;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_music,parent,false);
        return new ViewHolder(view);
    }

    private IOnClickSongListener iOnClickSongListener;

    private static OnItemClickListener listener;

    public void setiOnClickSongListener(IOnClickSongListener iOnClickSongListener) {
        this.iOnClickSongListener = iOnClickSongListener;
    }


    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.stt.setVisibility(position == mPlayingPosition ? View.INVISIBLE : View.VISIBLE);
        holder.sn.setTypeface(position == mPlayingPosition ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        holder.imgstt.setVisibility(position == mPlayingPosition ? View.VISIBLE : View.INVISIBLE);

        Song song = mList.get(position);
        holder.stt.setText(song.getId() + "");
        holder.sn.setText(song.getSongName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss");
        String duration = simpleDateFormat.format(song.getDuration());

        holder.time.setText(duration);
        holder.imageView.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.addToFavorite:
                                Toast.makeText(mContext, "Add Succes", Toast.LENGTH_SHORT).show();
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView stt, sn, time;
        private ImageView imgstt, imageView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            stt = (TextView) itemView.findViewById(R.id.textstt);
            sn = (TextView) itemView.findViewById(R.id.tenbaihat);
            time = (TextView) itemView.findViewById(R.id.thoigian);
            imgstt = (ImageView) itemView.findViewById(R.id.imgstt);
            imageView = (ImageView) itemView.findViewById(R.id.threedot);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onItemClick(itemView, getLayoutPosition());
                        if (iOnClickSongListener!= null){
                            iOnClickSongListener.update(getLayoutPosition());
                        }
                    }

                }
            });
        }
    }

    public AllSongsAdapter(@NonNull Context context, int resource, @NonNull List<Song> objects) {
        this.mContext = context;
        this.mRes = resource;
        this.mList = objects;
    }


    public void setPlayingPosition(int mPlayingPosition) {
        this.mPlayingPosition = mPlayingPosition;
    }
}
