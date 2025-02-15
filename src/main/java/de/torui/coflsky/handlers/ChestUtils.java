package de.torui.coflsky.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ChestUtils {

    public static BlockPos getLookedAtChest() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return null;

        MovingObjectPosition rayTraceResult = mc.thePlayer.rayTrace(5.0, 1.0F); // 5-block reach
        if (rayTraceResult == null || rayTraceResult.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return null;

        BlockPos pos = rayTraceResult.getBlockPos();
        World world = mc.theWorld;
        Block block = world.getBlockState(pos).getBlock();

        if (block instanceof BlockChest) {
            return pos;
        }

        return null; // Not a chest
    }
}