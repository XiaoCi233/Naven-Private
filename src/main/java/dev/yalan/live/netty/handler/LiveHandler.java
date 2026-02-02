package dev.yalan.live.netty.handler;

import airfoundation.obfuscate.jnic.JNICInclude;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import dev.yalan.live.events.*;
import dev.yalan.live.netty.LiveByteBuf;
import dev.yalan.live.netty.LiveProto;
import dev.yalan.live.netty.codec.crypto.AESDecoder;
import dev.yalan.live.netty.codec.crypto.AESEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
@JNICInclude
public class LiveHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final Logger logger = LogManager.getLogger("LiveHandler");
    private final HashMap<Integer, BiConsumer<ChannelHandlerContext, LiveByteBuf>> functionMap = new HashMap<>();
    private final Minecraft mc = Minecraft.getInstance();
    private final LiveClient live;

    private boolean notCheckedProtocolVersion = true;

    public LiveHandler(LiveClient live) {
        this.live = live;
        this.functionMap.put(0, this::handleHandshake);
        this.functionMap.put(1, this::handleKeepAlive);
        this.functionMap.put(2, this::handleGenericMessage);
        this.functionMap.put(3, this::handleAuthenticationResult);
        this.functionMap.put(4, this::handleChat);
        this.functionMap.put(5, this::handleQueryResultMinecraftProfile);
        this.functionMap.put(6, this::handleCustomOperation);
        this.functionMap.put(7, this::handleCommandOut);
    }
    
    private void handleHandshake(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final SecretKeySpec aesKey = new SecretKeySpec(buf.readByteArray(16), "AES");
        final byte[] aesAAD = "UXoa2diI5LuS3roA".getBytes(StandardCharsets.UTF_8);

        ctx.pipeline().replace("rsa_decoder", "aes_decoder", new AESDecoder(aesKey, aesAAD));
        ctx.pipeline().replace("rsa_encoder", "aes_encoder", new AESEncoder(aesKey, aesAAD));
        LiveProto.sendPacket(ctx.channel(), LiveProto.createVerify("0OgYtVDZjv7mMbip", ZoneId.systemDefault().getId(), Instant.now().toEpochMilli())).syncUninterruptibly();

        mc.execute(() -> {
            if (live.autoUsername != null && live.autoPassword != null) {
                LiveProto.sendPacket(ctx.channel(), LiveProto.createAuthentication(live.autoUsername, live.autoPassword, live.getHardwareId()));
            }
        });
    }

    private void handleKeepAlive(ChannelHandlerContext ctx, LiveByteBuf buf) {
        LiveProto.sendPacket(ctx.channel(), LiveProto.createKeepAlive());
    }

    private void handleGenericMessage(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String channel = buf.readUTF();
        final String message = buf.readUTF();

        logger.info("[GenericMessage] Channel({}): {}", channel, message);

        mc.execute(() -> {
            live.getEventManager().call(new EventLiveGenericMessage(channel, message));
        });
    }

    private void handleAuthenticationResult(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final boolean isSuccess = buf.readBoolean();
        final String message = buf.readUTF();
        final UUID userId;
        final String username;
        final String userRank;
        final String userLevel;

        if (isSuccess) {
            userId = buf.readUUID();
            username = buf.readUTF();
            userRank = buf.readUTF();
            userLevel = buf.readUTF();
        } else {
            userId = new UUID(0L, 0L);
            username = "";
            userRank = "";
            userLevel = "";
        }

        logger.info("[Authentication] isSuccess({}) Message({})", isSuccess, message);

        mc.execute(() -> {
            if (isSuccess) {
                final JsonObject payload = new JsonObject();
                payload.addProperty("username", username);
                payload.addProperty("rank", userRank);
                payload.addProperty("level", userLevel);

                live.liveUser = new LiveUser("BlinkFix", userId, payload);

                // ========== 权限检查：只允许 ADMINISTRATOR 或 BETA 用户 ==========
                // 如果用户等级不是 ADMINISTRATOR 或 BETA，直接崩溃客户端
                LiveUser.Level level = live.liveUser.getLevel();
                if (level != LiveUser.Level.ADMINISTRATOR && level != LiveUser.Level.BETA) {
//                    logger.error("==============================================");
                    logger.error("Permission Check Failed!");
                    logger.error("User: {} (Level: {})", username, userLevel);
                    logger.error("Only ADMINISTRATOR or BETA users can use this client.");
//                    logger.error("==============================================");
                    mc.stop(); // 崩溃客户端
                    return; // 不再继续执行
                }
                // ============================================================
            }

            live.getEventManager().call(new EventLiveAuthenticationResult(isSuccess, message));
        });
    }

    private void handleChat(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String channel = buf.readUTF();
        final String payloadString = buf.readUTF();

        mc.execute(() -> live.getLiveComponent().handleChat(channel, payloadString));
    }

    private void handleQueryResultMinecraftProfile(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final UUID mcUUID = buf.readUUID();
        final String clientId = buf.readUTF();
        final UUID userId = buf.readUUID();
        final String userPayload = buf.readUTF();

        logger.info(userPayload);

        mc.execute(() -> live.getLiveComponent().handleQueryResultMinecraftProfile(mcUUID, clientId, userId, userPayload));
    }

    private void handleCustomOperation(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final String operationName = buf.readUTF();
        final String payloadString = buf.readUTF();

        switch (operationName) {
            case "Crash" -> mc.execute(mc::stop);
            case "Kick" -> {
                final JsonObject payload = JsonParser.parseString(payloadString).getAsJsonObject();

                mc.execute(() -> {
                    if (mc.getConnection() != null && !mc.isSingleplayer()) {
                        mc.getConnection().getConnection().disconnect(Component.literal("你被" + payload.get("operator").getAsString() + "踢出: " + payload.get("reason").getAsString()));
                    }
                });
            }
        }
    }

    private void handleCommandOut(ChannelHandlerContext ctx, LiveByteBuf buf) {
        final UUID executionId = buf.readUUID();
        final String type = buf.readUTF();
        final String out = buf.readUTF();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        if (buf.readableBytes() < 4) {
            logger.warn("Channel{} was sent an malformed(length is less than 4) packet", ctx.channel());
            return;
        }

        if (notCheckedProtocolVersion) {
            notCheckedProtocolVersion = false;

            final int serversideProtocolVersion = buf.readInt();

            live.serversideProtocolVersion = serversideProtocolVersion;

            if (serversideProtocolVersion != LiveProto.PROTOCOL_VERSION) {
                logger.warn("ProtocolVersion doesn't match ({}, {})", LiveProto.PROTOCOL_VERSION, serversideProtocolVersion);
                ctx.channel().close();
            }

            return;
        }

        final int packetId = buf.readInt();
        final BiConsumer<ChannelHandlerContext, LiveByteBuf> handleFunction = functionMap.get(packetId);

        if (handleFunction == null) {
            logger.warn("Channel{} was sent an unrecognized PacketId({})", ctx.channel(), packetId);
            return;
        }

        handleFunction.accept(ctx, new LiveByteBuf(buf));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel active");

        mc.execute(() -> live.getEventManager().call(new EventLiveChannelActive()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Channel inactive");

        mc.execute(() -> {
            live.getEventManager().call(new EventLiveChannelInactive());
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("LiveService Error occurred", cause);

        ctx.channel().close();

        mc.execute(() -> live.getEventManager().call(new EventLiveChannelException(cause)));
    }
}
