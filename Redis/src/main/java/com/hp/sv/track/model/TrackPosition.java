package com.hp.sv.track.model;

public class TrackPosition {
    public long id;
    public long trackId;
    public int cost;

    public TrackPosition(long id, long trackId, int cost) {
        this.id = id;
        this.trackId = trackId;
        this.cost = cost;
    }
}
