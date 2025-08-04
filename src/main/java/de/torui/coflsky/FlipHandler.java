package de.torui.coflsky;

import de.torui.coflsky.commands.models.FlipData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlipHandler {

    public static class FlipDataStructure {

        private Map<Long, FlipData> Flips = new ConcurrentHashMap<>();
        private Map<FlipData, Long> ReverseMap = new ConcurrentHashMap<>();

        private FlipData HighestFlip = null;
        private FlipData LastFlip = null;

        private Timer t = new Timer();
        private TimerTask CurrentTask = null;

        public synchronized void RunHouseKeeping() {
            synchronized (Flips) {

                Long RemoveAllPrior = System.currentTimeMillis() - (Config.KeepFlipsForSeconds * 1000);
                Flips.keySet().stream().filter(l -> l <= RemoveAllPrior).forEach(l -> RemoveLong(l));
                if (!Flips.isEmpty()) {
                    HighestFlip = Flips.values().stream().max((f1, f2) -> f1.Worth - f2.Worth).orElse(null);
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
                        RunHouseKeeping();
                    }
                };
                t.schedule(CurrentTask, Config.KeepFlipsForSeconds * 1000 + /* small arbitrary delay */150);
            }
        }

        public synchronized void Insert(FlipData flip) {
            Long l = System.currentTimeMillis();
            LastFlip = flip;

            synchronized (Flips) {
                Flips.put(l, flip);
                ReverseMap.put(flip, l);
            }

            RunHouseKeeping();
        }

        private void RemoveLong(Long l) {
            if (l == null)
                return;
            synchronized (Flips) {
                FlipData f = Flips.get(l);
                if (f != null) {
                    ReverseMap.remove(f);
                    Flips.remove(l);
                }
            }
        }

        private void RemoveFlip(FlipData f) {
            if (f == null)
                return;

            synchronized (Flips) {
                Long l = ReverseMap.get(f);
                if (l != null) {
                    Flips.remove(l);
                    ReverseMap.remove(f);
                }
            }
        }

        public FlipData GetHighestFlip() {
            return HighestFlip;
        }

        public FlipData GetLastFlip() {
            if (LastFlip == null) {
                return null;
            }
            Long l = ReverseMap.get(LastFlip);
            if (l == null) {
                LastFlip = null;
            }
            return LastFlip;
        }

        public FlipData getFlipById(String id) {
            FlipData[] flips = Flips.values().stream().filter(flipData -> flipData.Id.equals(id)).toArray(FlipData[]::new);
            if (flips.length == 0) {
                return null;
            }
            return flips[0];
        }

        public void InvalidateFlip(FlipData flip) {
            RemoveFlip(flip);
            RunHouseKeeping();
        }

        public int CurrentFlips() {
            return Flips.size();
        }

    }

    public FlipDataStructure fds;
    public String lastClickedFlipMessage;

    public FlipHandler() {
        fds = new FlipDataStructure();
    }

}
