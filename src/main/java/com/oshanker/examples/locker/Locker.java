package com.oshanker.examples.locker;

import java.util.Objects;
import java.util.Optional;

public final class Locker {
    private final String id;
    private final Size size;
    private Reservation reservation; // null when free

    public Locker(String id, Size size) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.id = id;
        this.size = Objects.requireNonNull(size, "size must not be null");
    }

    public boolean isFree() { return reservation == null; }

    /**
     * put reservation
     * @param reservation
     */
    public void assign(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation must not be null");
        if (!isFree()) {
            throw new IllegalStateException("Locker " + id + " is already occupied");
        }
        this.reservation = reservation;
    }

    /**
     * remove reservation
     * @return
     */
    public Reservation release() {
        if (isFree()) {
            throw new IllegalStateException("Locker " + id + " is already free");
        }
        Reservation released = reservation;
        reservation = null;
        return released;
    }

    public Optional<Reservation> getReservation() { return Optional.ofNullable(reservation); }

    public String getId() { return id; }
    public Size getSize() { return size; }
}