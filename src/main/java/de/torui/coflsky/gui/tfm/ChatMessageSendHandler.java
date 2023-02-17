package de.torui.coflsky.gui.tfm;

import de.torui.coflsky.FlipperChatCommand;
import de.torui.coflsky.WSCommandHandler;
import io.netty.channel.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@ChannelHandler.Sharable
public class ChatMessageSendHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof C01PacketChatMessage) {
            if (FlipperChatCommand.useChatOnlyMode) {
                String message = ((C01PacketChatMessage) msg).getMessage();
                if (!message.startsWith("/")) {
                    WSCommandHandler.Execute("/cofl chat " + message, Minecraft.getMinecraft().thePlayer);
                    return;
                }
            }
        }
        ctx.write(msg, promise);
    }

    @SubscribeEvent
    public void connect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChannelPipeline pipeline = event.manager.channel().pipeline();
        pipeline.addBefore("packet_handler", this.getClass().getName(), this);
    }
}
