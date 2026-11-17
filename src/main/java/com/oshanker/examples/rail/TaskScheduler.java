package com.oshanker.examples.rail;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class TaskScheduler {
	static class Task {
		String id;
		Instant runAt;
		
		public Task(String id, Instant runAt) {
			super();
			this.id = id;
			this.runAt = runAt;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Instant getRunAt() {
			return runAt;
		}
		
		public void setRunAt(Instant runAt) {
			this.runAt = runAt;
		}
		@Override
		public String toString() {
			return "Task [id=" + id + ", runAt=" + runAt + "]";
		}
		
		public void execute() {
			System.out.printf("executing %s %n", id );
		}
	}
	
	Clock clock;
	
	Map<String, Task> tasksById = new ConcurrentHashMap<>(
			);
	Set<Task> tasks = new ConcurrentSkipListSet<>(
			Comparator.comparing(Task::getRunAt));

	public void removeTask(String id) {
		Task task = tasksById.get(id);
		System.out.println("Removing " + task);
		tasks.remove(task);
	}
	
	public void scheduleTask(String id, Instant executionTime) {
		Task task = new Task(id, executionTime);
		tasks.add(task);
		tasksById.put(id, task);
	}
	
	public void execute () {
		List<Task> ready = new LinkedList<>();
		for (Task task : tasks) {
			if(task.getRunAt().isAfter(clock.instant())) {
				
				System.out.println("breaking at " + task);
				break;
				
			}
			ready.add(task);
		
		}
		System.out.println("ready tasks " + ready);
		
		for (Task task : ready) {
			task.execute();
			tasks.remove(task);
			
		}
		
	}

	public TaskScheduler(Clock clock) {
		super();
		this.clock = clock;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Instant fixed = Instant.parse("2026-11-16T10:00:00Z");
		Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);
		TaskScheduler taskScheduler  = 
				new TaskScheduler( clock);
		int[] offsets = {30,10,20,50,40};
		for (int i = 0; i < offsets.length; i++) {
			int delay = offsets[i];
			taskScheduler.scheduleTask(Integer.toString(delay),
					fixed.plus(Duration.ofMinutes(delay)));
			
		}
		for (Task task : taskScheduler.tasks) {
			System.out.println(task);
			
		}
		taskScheduler.removeTask("20");
		
        taskScheduler.clock = Clock.offset(clock, 
        		Duration.ofMinutes(30));
        
        taskScheduler.execute();
		System.out.println("tasks " +taskScheduler. tasks);
	}

}
