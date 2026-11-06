package com.oshanker.examples.locker;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

class NoAvailableLockerException extends Exception {
    private static final long serialVersionUID = 1L;

    private final Size requestedSize;

    public NoAvailableLockerException(Size requestedSize) {
        super("No available locker for size " + requestedSize + " or larger");
        this.requestedSize = requestedSize;
    }

    public Size getRequestedSize() { return requestedSize; }
}

 class InvalidCodeException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidCodeException() { super("Invalid pickup code"); }
}

 class ExpiredCodeException extends Exception {
    private static final long serialVersionUID = 1L;
    public ExpiredCodeException() { super("Pickup code has expired"); }
}
 
public class LockerStation {
    private static final Duration TTL = Duration.ofDays(3);
    private static final int CODE_BOUND = 1_000_000; // 6-digit codes

    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final EnumMap<Size, Deque<Locker>> freeLockers = new EnumMap<>(Size.class);
    // LinkedHashMap keeps deposit order, which equals expiry order for a fixed TTL
    private final Map<String, Locker> lockersByCode = new LinkedHashMap<>();

    public LockerStation(Clock clock, List<Locker> lockers) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        Objects.requireNonNull(lockers, "lockers must not be null");
        if (lockers.isEmpty()) {
            throw new IllegalArgumentException("station needs at least one locker");
        }
        for (Size size : Size.values()) {
            freeLockers.put(size, new ArrayDeque<>());
        }
        Set<String> seenIds = new HashSet<>();
        for (Locker locker : lockers) {
            Objects.requireNonNull(locker, "locker must not be null");
            if (!locker.isFree()) {
                throw new IllegalArgumentException("locker " + locker.getId() + " is not free");
            }
            if (!seenIds.add(locker.getId())) {
                throw new IllegalArgumentException("duplicate locker id " + locker.getId());
            }
            freeLockers.get(locker.getSize()).addLast(locker);
        }
    }

    public synchronized String depositPackage(Parcel parcel) throws NoAvailableLockerException {
        Objects.requireNonNull(parcel, "parcel must not be null");
        Instant now = clock.instant();

        Locker locker;
        try {
            locker = takeFreeLocker(parcel.getSize());
        } catch (NoAvailableLockerException e) {
            reclaimExpired(now);                       // station looks full: free expired lockers, retry once
            locker = takeFreeLocker(parcel.getSize()); // throws again if still nothing fits
        }

        String code = generateUniqueCode();
        locker.assign(new Reservation(code, parcel, now, now.plus(TTL)));
        lockersByCode.put(code, locker);
        return code;
    }

    public synchronized Parcel pickupPackage(String code)
            throws InvalidCodeException, ExpiredCodeException {
        if (code == null || code.isBlank()) {
            throw new InvalidCodeException();
        }
        Locker locker = lockersByCode.get(code);
        if (locker == null) {
            throw new InvalidCodeException();          // unknown or already used
        }
        Reservation reservation = locker.getReservation()
                .orElseThrow(() -> new IllegalStateException("Locker " + locker.getId() + " has no reservation"));

        if (reservation.isExpired(clock.instant())) {
            throw new ExpiredCodeException();          // locker stays occupied until reclaimed
        }

        lockersByCode.remove(code);                    // makes a second pickup fail cleanly
        locker.release();
        freeLockers.get(locker.getSize()).addLast(locker);
        return reservation.getParcel();
    }

    /** Frees lockers holding expired parcels and returns those parcels (e.g., for return-to-sender). */
    public synchronized List<Parcel> reclaimExpired() {
        return reclaimExpired(clock.instant());
    }

    private List<Parcel> reclaimExpired(Instant now) {
        List<Parcel> reclaimed = new ArrayList<>();
        Iterator<Map.Entry<String, Locker>> it = lockersByCode.entrySet().iterator();
        while (it.hasNext()) {
            Locker locker = it.next().getValue();
            Reservation reservation = locker.getReservation()
                    .orElseThrow(() -> new IllegalStateException("Locker " + locker.getId() + " has no reservation"));
            if (!reservation.isExpired(now)) {
                break;                                 // later entries expire later
            }
            it.remove();
            locker.release();
            freeLockers.get(locker.getSize()).addLast(locker);
            reclaimed.add(reservation.getParcel());
        }
        return reclaimed;
    }

    private Locker takeFreeLocker(Size requested) throws NoAvailableLockerException {
        Size[] sizes = Size.values();
        for (int i = requested.ordinal(); i < sizes.length; i++) {
            Locker locker = freeLockers.get(sizes[i]).pollFirst();
            if (locker != null) {
                return locker;
            }
        }
        throw new NoAvailableLockerException(requested);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = String.format("%06d", random.nextInt(CODE_BOUND));
        } while (lockersByCode.containsKey(code));
        return code;
    }
}