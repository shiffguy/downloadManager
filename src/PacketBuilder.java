/***
 * Class that builds single downloaded/writable packet
 * it contains data + metadata
 */
public class PacketBuilder {


    private final int packetIndex;
    private long packetPosition;
    private byte[] bytesData;
    private boolean kill;

    public PacketBuilder(int packetIndex, long packetPosition, byte[] bytesData, boolean kill){
        this.packetPosition = packetPosition;
        this.bytesData = bytesData;
        this.packetIndex = packetIndex;
        this.kill = kill;
    }

    public PacketBuilder(boolean kill) {
        this.packetPosition = 0;
        this.bytesData = null;
        this.packetIndex = 0;
        this.kill = kill;
    }


    public long getPacketPosition() {
        return packetPosition;
    }

    public byte[] getBytesData() {
        return bytesData;
    }


    public int getPacketIndex() {
        return packetIndex;
    }

    public boolean getKillStatus() {
        return kill;
    }

}
