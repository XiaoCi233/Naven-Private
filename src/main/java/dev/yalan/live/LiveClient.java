package dev.yalan.live;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.heypixel.heypixelmod.events.api.EventManager;
import dev.yalan.live.events.EventLiveConnectionStatus;
import dev.yalan.live.netty.LiveProto;
import dev.yalan.live.netty.codec.FrameDecoder;
import dev.yalan.live.netty.codec.FrameEncoder;
import dev.yalan.live.netty.codec.crypto.RSADecoder;
import dev.yalan.live.netty.codec.crypto.RSAEncoder;
import dev.yalan.live.netty.handler.LiveHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import oshi.SystemInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
@jnic.JNICInclude
public class LiveClient {
    public static LiveClient INSTANCE;
    public static final Gson GSON = new GsonBuilder().create();

    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("Live-Worker"));
    private final LiveReconnectionThread reconnectionThread = new LiveReconnectionThread();
    private final EventManager eventManager = new EventManager();
    private final AtomicBoolean isConnecting = new AtomicBoolean();
    private final LiveComponent liveComponent = new LiveComponent(this);
    private final HashMap<UUID, LiveUser> liveUserMap = new HashMap<>();
    private final RSAPrivateKey rsaPrivateKey;
    private final RSAPublicKey rsaPublicKey;
    private final String hardwareId;

    public int serversideProtocolVersion = LiveProto.PROTOCOL_VERSION;
    public ClientSetting clientSetting;
    public String autoUsername;
    public String autoPassword;
    public LiveUser liveUser;

    private Channel channel;

    public LiveClient() {
        try {
            rsaPrivateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDCSwWQ3Qap/JSy2t6n8d2CQwzlyDFt0CrXXYXsVgwj+5e4NafHzVrALd8cOaqNjAGqX1jBVJICHSdELEJuTJ5y5shEcA3/fjj2ol9OBRU6lEfOUnrn79F0Aex5uHOGz0dpefNv3VDugqoPjCCVzeXgofXjRFUkf9rzOjprirV1J84wUUH9eV0dsrr0rb51IONNc+eCQ7AVVJxHNHMq+3Yl8b8EN8pYP6+S3k2FrjZWnXTq6nXGdfeaT1K9NADgjEU1eCKRHyRX1XoouQRtKOPj8iPpZ8SEjIN4ypbreHro1/5dkAYIamamFyJEOE98pTGStC+7lZeq37sTVnVhwA1JAgMBAAECggEAAbh5HEUu0MzPMs3IJL/aNLP8DmFhWa37ISTfgZ4p9Zwd5fhYq+bsR7EoTYdPtLjxj1UQk8a4U8s3DK4hKPml+hHD4oJFB3cSUVHTCv11yIrlX2UA2GK57b9yHQpWgJI/mUYmkx4oQDJ5R9G3fKgiAeUhSLHTWLNGqLqCZ68m5/qOL4fZeX8qSnAoh9t7G5NwqNW/BQw+H9cV3l+FFhPrZg3puwqV0e/X0BbUs6qwu1U03Eks5bqhesrhxxzSQ8IvVSyoOeNiQz9b62eGkrbBb40AsZfp8UKkGtiP4P7f30G+EXl0xMw8IKuD2yEaFoT3EOp/pwpAWgsi1HXpihABwQKBgQDWhf1Cxx/cFGp/esmJ1HxZPiUDxP9SDzLffv593U1+6zU2kD/FDAD+zlTGPsdnIjhiVDnC33JhQuyS8q68ve3rFVleqgRx7lzrTTwQhWc0eS7Wcm64Tx0Qrh+kcMFc/39FfLGdXthvDF/lzNgRNtfaV7LA29PfPHsm9dcB+nXACQKBgQDn27cTS3XiCAlVxPuLOOjOFmhC8Oy78MGm91jEdt06c+LWb7/XooRiLC+jLV2eZf1ww/8wGuKiOsaSI+ifIXuherbgVVRNrKECh0NmX7MQKD1u1dDWUYSuLgF3xk5qVApzMzt7kpOZW8oU3pxNN2NUhq4JPWvddp0YLicRDPSzQQKBgEaVIWYmVDI6OWwmP0dtNVi7nLwnxgX+4DMu+pb5CHV4+DIytfikYXN0+emcYElrtfeMVNDsahrcRTB4TsvlBfHyPUxrTjkaP1JgQCIDcpiOQr3mOs3DogGJ+PlG7CgoHH0FcP8hZL7s6GSYyIKpc83wSu0vpv9tS+veuVZ6TOHZAoGARsnNLmXmmtkLSJV4mzwH0AwFTxLRpFvQNBLmli0YRCVnh0LdFo/2zSZicPaD072d0FT2z5AVy6QYRjwSZKtLXLTjqsBCmehvzB7SFCp5uCAhCXrcZOLEmI1RQ2lXPZ1lB2EH8yWATN06aH7Cx8x2VjM8ZXPMP/Rb6CsHIGyNNUECgYA/8S4iOUjkUrIgBu+SJZwJkPhPCbI2nUqP5gaeD+6rg0ovkRGDzmrFm0lYB9WtMbDg8EU+gXpkGiqWOMVlnSlYypiHqe1hJaMdZubdvgfQJPAHav33v0Szkl638r7ThseUrIRhgse1jFy+VuEnCNl1f/3vSTCAh5E6RKyc9RaXRA==")));
            rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqRDNKtTXANYWsW5AuZ8eJADIDjXVNJr1Yv4J62lpn7cuc0wl37tTSAlVPjxG1MDXv5o8/YogYyL7YeLS8gnSQUEXkXawwQ69zMzhY7kE2zosn533ZHpBoW/9T71DjtY+cr7+XK2CNbdYLCkasqEx0bOT3Sb+Q4zxOjd0NkTORqbAmcc+6653gvnN/SZX5Avcg9KUfkKLPNxZZoa5qsjPdkAO0tDxA6wXAckox/adMJ/pyRmODC0f5G/2EDvpbbl2Vt0zh4z4+C+yH1RVTwRppwlzfw8zhOGIDhCpXgiWb2dV1s5588A/oSGzRG+2i0yGrvGdBYnpI+fRD5hL/nykpwIDAQAB")));
            hardwareId = generateHardwareId();
            requestClientSetting();
        } catch (Exception e) {
            throw new RuntimeException("Can't init LiveClient", e);
        }
    }

    private void requestClientSetting() throws IOException {
        final URL url = URI.create("https://www.unitednetwork.cc/BlinkFix/clientSetting").toURL();
        final HttpURLConnection con = (HttpURLConnection) url.openConnection(Minecraft.getInstance().getProxy());

        try {
            con.connect();

            if (con.getResponseCode() != 200) {
                throw new IOException("HTTP Code： " + con.getResponseCode());
            }

            clientSetting = GSON.fromJson(IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8), ClientSetting.class);
        } finally {
            con.disconnect();
        }
    }

    public void connect() {
        if (isOpen() || isConnecting.get()) {
            return;
        }

        isConnecting.set(true);

        final Bootstrap bootstrap = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(workerGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ch.pipeline().addLast("frame_decoder", new FrameDecoder())
                                .addLast("rsa_decoder", new RSADecoder(rsaPrivateKey))
                                .addLast("frame_encoder", new FrameEncoder())
                                .addLast("rsa_encoder", new RSAEncoder(rsaPublicKey))
                                .addLast("live_handler", new LiveHandler(LiveClient.this));
                    }
                });

        bootstrap.connect("live.unitednetwork.cc", 1102).addListener((ChannelFutureListener) future -> {
            isConnecting.set(false);

            if (future.isSuccess()) {
                LiveProto.sendPacket(future.channel(), LiveProto.createHandshake());
            }

            Minecraft.getInstance().execute(() -> {
                if (future.isSuccess()) {
                    channel = future.channel();
                }

                eventManager.call(new EventLiveConnectionStatus(future.isSuccess(), future.cause()));
            });
        });
    }

    public void sendPacket(LiveProto.LivePacket packet) {
        if (isActive()) {
            LiveProto.sendPacket(channel, packet);
        }
    }

    public void shutdown() {
        stopReconnectionThread();

        if (isOpen()) {
            channel.close();
        }

        workerGroup.shutdownGracefully();
    }

    public void startReconnectionThread() {
        reconnectionThread.start();
    }

    public void stopReconnectionThread() {
        if (reconnectionThread.isAlive()) {
            reconnectionThread.interrupt();
        }
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public boolean isConnecting() {
        return isConnecting.get();
    }

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    public LiveComponent getLiveComponent() {
        return liveComponent;
    }

    public HashMap<UUID, LiveUser> getLiveUserMap() {
        return liveUserMap;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    private static String generateHardwareId() throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        final SystemInfo systemInfo = new SystemInfo();
        final String info =
                systemInfo.getHardware().getProcessor().getProcessorIdentifier().getName()
                + systemInfo.getHardware().getComputerSystem().getBaseboard().getSerialNumber()
                + systemInfo.getHardware().getComputerSystem().getSerialNumber();
        final byte[] digest = messageDigest.digest(info.getBytes(StandardCharsets.UTF_8));
        final StringBuilder digestSB = new StringBuilder();

        for (byte b : digest) {
            final String hexString = Integer.toHexString(b & 0xFF);

            if (hexString.length() == 1) {
                digestSB.append('0').append(hexString);
            } else {
                digestSB.append(hexString);
            }
        }

        return digestSB.toString();
    }
}
