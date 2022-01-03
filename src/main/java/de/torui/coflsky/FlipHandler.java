package de.torui.coflsky;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FlipHandler {
	public static class Flip {
		public String id;
		public int worth;

		public Flip(String id, int worth) {
			super();
			this.id = id;
			this.worth = worth;
		}

		public Flip() {

		}

	}

	public static class FlipDataStructure {

		private Map<Long, Flip> Flips = new HashMap<>();
		private Map<Flip, Long> ReverseMap = new HashMap<>();

		private Flip HighestFlip = null;

		private Timer t = new Timer();
		private TimerTask CurrentTask = null;

		public synchronized void RunHouseKeeping() {
			synchronized (Flips) {

				Long RemoveAllPrior = System.currentTimeMillis() - 30000;
				Flips.keySet().stream().filter(l -> l <= RemoveAllPrior).forEach(l -> RemoveLong(l));
				if (!Flips.isEmpty()) {
					HighestFlip = Flips.values().stream().max((f1, f2) -> f1.worth - f2.worth).orElse(null);
				} else {
					HighestFlip = null;
				}
			}

			if (CurrentTask != null) {
				CurrentTask.cancel();
				CurrentTask = null;
				t.purge();
			}
			if (!Flips.isEmpty()) {
				CurrentTask = new TimerTask() {
					@Override
					public void run() {
						try {
							RunHouseKeeping();
						} catch (ConcurrentModificationException c) {

						}
					}
				};
				t.schedule(CurrentTask, 30 * 1000/* 30 seconds */ + /* small arbitrary delay */150);
			}
		}

		public synchronized void Insert(Flip flip) {
			Long l = System.currentTimeMillis();
			Flips.put(l, flip);
			ReverseMap.put(flip, l);

			RunHouseKeeping();
		}

		public void RemoveLong(Long l) {
			if (l == null)
				return;

			Flip f = Flips.get(l);
			if (f != null) {
				ReverseMap.remove(f);
				Flips.remove(l);
			}
		}

		public void RemoveFlip(Flip f) {
			if (f == null)
				return;

			Long l = ReverseMap.get(f);
			if (l != null) {
				Flips.remove(l);
				ReverseMap.remove(f);
			}

		}

		public Flip GetHighestFlip() {
			return HighestFlip;
		}

		public void InvalidateFlip(Flip flip) {
			RemoveFlip(flip);
			RunHouseKeeping();
		}

		public int CurrentFlips() {
			return Flips.size();
		}

	}

	public FlipDataStructure fds;

	public FlipHandler() {
		fds = new FlipDataStructure();
	}

}