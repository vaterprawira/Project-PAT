package com.example.concertapp;

public class Event {
    private int id;
    private String namaKonser;
    private String artist;
    private int harga;
    private String lokasi;
    private String tanggal;

    public Event(int id, String namaKonser, String artist, int harga, String lokasi, String tanggal) {
        this.id = id;
        this.namaKonser = namaKonser;
        this.artist = artist;
        this.harga = harga;
        this.lokasi = lokasi;
        this.tanggal = tanggal;
    }

    public int getId() { return id; }
    public String getNamaKonser() { return namaKonser; }
    public String getArtist() { return artist; }
    public int getHarga() { return harga; }
    public String getLokasi() { return lokasi; }
    public String getTanggal() { return tanggal; }
}
