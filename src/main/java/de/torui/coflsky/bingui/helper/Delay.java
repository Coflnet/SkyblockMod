package de.torui.coflsky.bingui.helper;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//this is important so the gui doesn't get closed instantly
/*
 * Thanks to PolyFrost for OneConfig where I took this method from.
 * https://github.com/Polyfrost/OneConfig/blob/master/LICENSE
 */
public class Delay {
    private final Runnable function;
    private int delay;

    public Delay(Runnable functionName, int ticks) {
        if (ticks < 1) {
            functionName.run();
        } else {
            MinecraftForge.EVENT_BUS.register(this);
            delay = ticks;
        }
        function = functionName;
    }

    @SubscribeEvent
    protected void onTick(TickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // Delay expired
            if (delay < 1) {
                function.run();
                MinecraftForge.EVENT_BUS.unregister(this);
            }
            delay--;
        }
    }
}
