/**
 * 
 */
package com.oshanker.examples.locker;

import java.time.Clock;
import java.util.List;

/**
 * 
 */
public class LockerFlow {
	
	static LockerStation station;
	
	/*
	 * populate freeLockers
	 */
	static void initFlow() {
        List<Locker> lockers = List.of(
                new Locker("S1", Size.SMALL),
                new Locker("M1", Size.MEDIUM),
                new Locker("L1", Size.LARGE));
        station = new LockerStation(Clock.systemUTC(), lockers);
		
	}

	static void depositAndPickup() throws Exception {
        Parcel parcel1 = new Parcel(Size.SMALL);
		String c1 = station.depositPackage(parcel1);
        String c2 = station.depositPackage(new Parcel(Size.SMALL));
        String c3 = station.depositPackage(new Parcel(Size.SMALL));
	
        // code handed to consumer
        
        Parcel p1 = station.pickupPackage(c1); // frees S1
        if(!p1.equals(parcel1)) {
        	System.out.println("incorrect parcel delivered");
        }
    	System.out.println("parcel delivered");
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		initFlow();
		depositAndPickup();

	}

}
