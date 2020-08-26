package com.bhb.huybinh2k.music.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bhb.huybinh2k.music.IOnClickSongListener;
import com.bhb.huybinh2k.music.R;
import com.bhb.huybinh2k.music.Song;
import com.bhb.huybinh2k.music.database.FavoriteSongsProvider;

import java.text.SimpleDateFormat;
import java.util.List;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.ViewHolder> {
    private Context mContext;
    private boolean isFavorite;
    private List<Song> mList;
    private int mPlayingPosition = -1;

    public void setmPlayingIdProvider(int mPlayingIdProvider) {
        this.mPlayingIdProvider = mPlayingIdProvider;
    }

    private int mPlayingIdProvider = -1;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_music, parent, false);
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
//        holder.stt.setVisibility(position == mPlayingPosition ? View.INVISIBLE : View.VISIBLE);
//        holder.sn.setTypeface(position == mPlayingPosition ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
//        holder.imgstt.setVisibility(position == mPlayingPosition ? View.VISIBLE : View.INVISIBLE);

        final Song song = mList.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(holder.itemView, position);
                    if (iOnClickSongListener != null) {
                        iOnClickSongListener.update(song.getId() - 1);
                    }
                }

            }
        });


        if (song.getIdProvider() == mPlayingIdProvider) {
            holder.stt.setVisibility(View.INVISIBLE);
            holder.sn.setTypeface(Typeface.DEFAULT_BOLD);
            holder.imgstt.setVisibility(View.VISIBLE);
        } else {
            holder.stt.setVisibility(View.VISIBLE);
            holder.sn.setTypeface(Typeface.DEFAULT);
            holder.imgstt.setVisibility(View.INVISIBLE);
        }

        holder.stt.setText(String.valueOf(song.getId()));
        holder.sn.setText(song.getSongName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("m:ss");
        String duration = simpleDateFormat.format(song.getDuration());

        holder.time.setText(duration);
        holder.imageView.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopupMenu(v, song);
            }
        });
    }

    private void createPopupMenu(View v, final Song song) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        if (isFavorite) {
            popupMenu.getMenuInflater().inflate(R.menu.remove_from_favorite, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.removeFromFavorite) {
                        song.setIsFavorite(0);
                        new FavoriteSongsProvider(mContext).update(song);
                        mList.remove(song);
                        notifyDataSetChanged();
                        Toast.makeText(mContext, "Remove Succes", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        } else {
            popupMenu.getMenuInflater().inflate(R.menu.add_to_favorite, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.addToFavorite) {
                        song.setIsFavorite(2);
                        new FavoriteSongsProvider(mContext).update(song);
                        Toast.makeText(mContext, "Add Succes", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
        }

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView stt, sn, time;
        private ImageView imgstt, imageView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            stt = (TextView) itemView.findViewById(R.id.textstt);
            sn = (TextView) itemView.findViewById(R.id.tenbaihat);
            time = (TextView) itemView.findViewById(R.id.thoigian);
            imgstt = (ImageView) itemView.findViewById(R.id.imgstt);
            imageView = (ImageView) itemView.findViewById(R.id.threedot);
        }
    }

    public AllSongsAdapter(@NonNull Context context, @NonNull List<Song> objects, boolean isFavorite) {
        this.mContext = context;
        this.mList = objects;
        this.isFavorite = isFavorite;
    }


    public void setPlayingPosition(int mPlayingPosition) {
        this.mPlayingPosition = mPlayingPosition;
    }
}
