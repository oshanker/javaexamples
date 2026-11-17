package com.oshanker.examples.rail.assethealth;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class AssetHealth {

   public  record Event(String assetId, int ts, boolean heartbeat, 
		   int errCode) {}

   /**
    * state of asset
    */
    static class State {
        final Set<Integer> beats = new HashSet<>();  // distinct timestamps
        boolean err = false;
    }

    public static Map<String, State> assetHealth(List<Event> events, int now, int A) {
        final int start = now - 60;

        // LinkedHashMap keeps assets in first-seen order
        Map<String, State> assets = new LinkedHashMap<>();

        for (Event e : events) {
            if (e.ts() < start || e.ts() > now) continue;

            State s = assets.computeIfAbsent(e.assetId(), k -> new State());
            if (e.heartbeat()) s.beats.add(e.ts());
            if (e.errCode() == 1001) s.err = true;
        }

        return assets;
        		//printOutput(B, assets);
    }

	static String printOutput(final int B, Map<String, State> assets) {
		int healthy = 0, degraded = 0, unknown = 0;
        StringBuilder out = new StringBuilder();

        for (Map.Entry<String, State> entry : assets.entrySet()) {
            State s = entry.getValue();
            int hb = s.beats.size();
            int misses = Math.max(0, B - hb);

            String status, reason;
            if (s.err) {
                status = "degraded";
                reason = "errCode=1001";
                degraded++;
            } else if (hb >= B - 1) {
                status = "healthy";
                reason = "misses=" + misses;
                healthy++;
            } else {
                status = "unknown";
                reason = "misses=" + misses;
                unknown++;
            }

            out.append(String.format("%s: %s (heartbeats=%d/%d, reason=%s)%n",
                    entry.getKey(), status, hb, B, reason));
        }

        out.append(String.format("healthy=%d, degraded=%d, unknown=%d", 
        		healthy, degraded, unknown));
        return out.toString();
	}

    public static void main(String[] args) {
    	System.out.println(MethodHandles.lookup().lookupClass());
        List<Event> events = new ArrayList<>();

        // LOCO-1973: heartbeats every 5s from 100 to 155
        for (int t = 100; t <= 155; t += 5) {
            events.add(new Event("LOCO-1973", t, true, 0));
        }

        // CRANE-A12: duplicate at 135, one carrying errCode 1001
        events.add(new Event("CRANE-A12", 135, true, 1001));
        events.add(new Event("CRANE-A12", 135, true, 0));
        for (int t = 140; t <= 155; t += 5) {
            events.add(new Event("CRANE-A12", t, true, 0));
        }

        // HST-7: no heartbeats (the duplicated block is collapsed to one copy)
        for (int t = 140; t <= 155; t += 5) {
            events.add(new Event("HST-7", t, false, 0));
        }

        int A = 5;
		Map<String, State> assets = assetHealth(events, 160, A);
        final int B = 60 / A;
        System.out.println(printOutput(B, assets));
    }
}