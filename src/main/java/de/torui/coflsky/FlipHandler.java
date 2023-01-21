package de.torui.coflsky;

import de.torui.coflsky.commands.models.SoundData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlipHandler {
    public static class Flip {
        public String id;
        public String message;
        public int worth;
        public SoundData sound;

        public Flip(String id, int worth, String message, SoundData sound) {
            super();
            this.id = id;
            this.message = message;
            this.worth = worth;
            this.sound = sound;
        }

        public Flip() {

        }

    }

    public static class FlipDataStructure {

        private Map<Long, Flip> Flips = new ConcurrentHashMap<>();
        private Map<Flip, Long> ReverseMap = new ConcurrentHashMap<>();

        private Flip HighestFlip = null;
        private Flip LastFlip = null;

        private Timer t = new Timer();
        private TimerTask CurrentTask = null;

        public synchronized void RunHouseKeeping() {
            synchronized (Flips) {

                Long RemoveAllPrior = System.currentTimeMillis() - (Config.KeepFlipsForSeconds * 1000);
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
                        RunHouseKeeping();
                    }
                };
                t.schedule(CurrentTask, Config.KeepFlipsForSeconds * 1000 + /* small arbitrary delay */150);
            }
        }

        public synchronized void Insert(Flip flip) {
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
                Flip f = Flips.get(l);
                if (f != null) {
                    ReverseMap.remove(f);
                    Flips.remove(l);
                }
            }
        }

        private void RemoveFlip(Flip f) {
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

        public Flip GetHighestFlip() {
            return HighestFlip;
        }

        public Flip GetLastFlip() {
            if (LastFlip == null) {
                return null;
            }
            Long l = ReverseMap.get(LastFlip);
            if (l == null) {
                LastFlip = null;
            }
            return LastFlip;
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
    public String lastClickedFlipMessage;

    public FlipHandler() {
        fds = new FlipDataStructure();
    }

}
