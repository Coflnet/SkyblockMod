package de.torui.coflsky;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlipHandler {
	public static class Flip {
		public final String id;
		public final int worth;

		public Flip(String id, int worth) {
			this.id = id;
			this.worth = worth;
		}

	}

	public static class FlipDataStructure {

		private final Map<Long, Flip> flips = new ConcurrentHashMap<>();
		private final Map<Flip, Long> reverseMap = new ConcurrentHashMap<>();

		private Flip highestFlip = null;

		private final Timer t = new Timer();
		private TimerTask currentTask = null;

		public synchronized void runHouseKeeping() {
			synchronized (flips) {

				long RemoveAllPrior = System.currentTimeMillis() - (Config.KEEP_FLIPS_FOR_SECONDS *1000);
				flips.keySet().stream().filter(l -> l <= RemoveAllPrior).forEach(this::removeLong);
				if (!flips.isEmpty()) {
					highestFlip = flips.values().stream().max(Comparator.comparingInt(f -> f.worth)).orElse(null);
				} else {
					highestFlip = null;
				}
			}

			if (currentTask != null) {
				currentTask.cancel();
				currentTask = null;
				t.purge();
			}
			if (!flips.isEmpty()) {
				currentTask = new TimerTask() {
					@Override
					public void run() {
						runHouseKeeping();
					}
				};
				t.schedule(currentTask, Config.KEEP_FLIPS_FOR_SECONDS * 1000 + /* small arbitrary delay */150);
			}
		}

		public synchronized void insert(Flip flip) {
			long l = System.currentTimeMillis();
			
			synchronized(flips) {
				flips.put(l, flip);
				reverseMap.put(flip, l);
			}		

			runHouseKeeping();
		}

		private void removeLong(Long l) {
			if (l == null)
				return;
			synchronized(flips) {
				Flip f = flips.get(l);
				if (f != null) {
					reverseMap.remove(f);
					flips.remove(l);
				}
			}
		}

		private void removeFlip(Flip f) {
			if (f == null)
				return;
			
			synchronized(flips) {
				Long l = reverseMap.get(f);
				if (l != null) {
					flips.remove(l);
					reverseMap.remove(f);
				}
			}
		}

		public Flip getHighestFlip() {
			return highestFlip;
		}

		public void invalidateFlip(Flip flip) {
			removeFlip(flip);
			runHouseKeeping();
		}

		public int getFlipsSize() {
			return flips.size();
		}

	}

	public final FlipDataStructure fds = new FlipDataStructure();


}
