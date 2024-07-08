package com.example.concertapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnOrderClickListener onOrderClickListener;

    public EventAdapter(List<Event> eventList, OnOrderClickListener onOrderClickListener) {
        this.eventList = eventList;
        this.onOrderClickListener = onOrderClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.namaKonser.setText(event.getNamaKonser());
        holder.artist.setText(event.getArtist());
        holder.harga.setText(String.valueOf(event.getHarga()));
        holder.lokasi.setText(event.getLokasi());
        holder.tanggal.setText(event.getTanggal());
        holder.orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrderClickListener.onOrderClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView namaKonser, artist, harga, lokasi, tanggal;
        Button orderButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            namaKonser = itemView.findViewById(R.id.nama_konser);
            artist = itemView.findViewById(R.id.artist);
            harga = itemView.findViewById(R.id.harga);
            lokasi = itemView.findViewById(R.id.lokasi);
            tanggal = itemView.findViewById(R.id.tanggal);
            orderButton = itemView.findViewById(R.id.order_button);
        }
    }

    public interface OnOrderClickListener {
        void onOrderClick(Event event);
    }
}

