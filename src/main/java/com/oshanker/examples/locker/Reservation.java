package com.oshanker.examples.locker;

import java.time.Instant;
import java.util.Objects;

enum Size { SMALL, MEDIUM, LARGE }

final class Parcel {
    private final Size size;

    public Parcel(Size size) {
        this.size = Objects.requireNonNull(size, "size must not be null");
    }

    public Size getSize() { return size; }
}

public final class Reservation {
    private final String code;
    private final Parcel parcel;
    private final Instant depositedAt;
    private final Instant expiresAt;

    public Reservation(String code, Parcel parcel, Instant depositedAt, Instant expiresAt) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        this.code = code;
        this.parcel = Objects.requireNonNull(parcel, "parcel must not be null");
        this.depositedAt = Objects.requireNonNull(depositedAt, "depositedAt must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (expiresAt.isBefore(depositedAt)) {
            throw new IllegalArgumentException("expiresAt must not be before depositedAt");
        }
    }

    public boolean isExpired(Instant now) { return !now.isBefore(expiresAt); }

    public String getCode() { return code; }
    public Parcel getParcel() { return parcel; }
    public Instant getDepositedAt() { return depositedAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
