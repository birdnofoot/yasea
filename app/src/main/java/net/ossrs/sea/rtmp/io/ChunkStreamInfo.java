package net.ossrs.sea.rtmp.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.ossrs.sea.rtmp.Util;
import net.ossrs.sea.rtmp.packets.RtmpHeader;

/**
 * Chunk stream channel information
 * 
 * @author francois
 */
public class ChunkStreamInfo {

    public static final byte RTMP_STREAM_CHANNEL = 0x05;
    public static final byte RTMP_COMMAND_CHANNEL = 0x03;
    public static final byte RTMP_VIDEO_CHANNEL = 0x06;
    public static final byte RTMP_AUDIO_CHANNEL = 0x07;
    public static final byte RTMP_CONTROL_CHANNEL = 0x02;
    private RtmpHeader prevHeaderRx;
    private RtmpHeader prevHeaderTx;
    private long realLastTimestamp = 0;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 128);

    /** @return the previous header that was received on this channel, or <code>null</code> if no previous header was received */
    public RtmpHeader prevHeaderRx() {
        return prevHeaderRx;
    }

    /** Sets the previous header that was received on this channel, or <code>null</code> if no previous header was sent */
    public void setPrevHeaderRx(RtmpHeader previousHeader) {
        this.prevHeaderRx = previousHeader;
    }

    /** @return the previous header that was transmitted on this channel */
    public RtmpHeader getPrevHeaderTx() {
        return prevHeaderTx;
    }

    public boolean canReusePrevHeaderTx(RtmpHeader.MessageType forMessageType) {
        return (prevHeaderTx != null && prevHeaderTx.getMessageType() == forMessageType);
    }

    /** Sets the previous header that was transmitted on this channel */
    public void setPrevHeaderTx(RtmpHeader prevHeaderTx) {
        this.prevHeaderTx = prevHeaderTx;
    }
    
    /** Utility method for calculating & synchronizing transmitted timestamps & timestamp deltas */
    public long markRealAbsoluteTimestampTx() {
        realLastTimestamp = System.nanoTime() / 1000 - realLastTimestamp;
        return realLastTimestamp;
    }

    /** @return <code>true</code> if all packet data has been stored, or <code>false</code> if not */
    public boolean storePacketChunk(InputStream in, int chunkSize) throws IOException {
        final int remainingBytes = prevHeaderRx.getPacketLength() - baos.size();
        byte[] chunk = new byte[Math.min(remainingBytes, chunkSize)];
        Util.readBytesUntilFull(in, chunk);
        baos.write(chunk);
        return (baos.size() == prevHeaderRx.getPacketLength());
    }

    public ByteArrayInputStream getStoredPacketInputStream() {
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        baos.reset();
        return bis;
    }
    
    /** Clears all currently-stored packet chunks (used when an ABORT packet is received) */
    public void clearStoredChunks() {
        baos.reset();
    }
}
