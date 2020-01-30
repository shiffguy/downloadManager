/***
 * Class that builds single downloaded/writable packet
 * it contains data + metadata
 */
public class PacketBuilder {

    //region Fields
    private final int packetIndex;
    private long packetNumber;
    private byte[] bytesData;
    private boolean kill;
    //endregion

    //region Constructor
    public PacketBuilder(int packetIndex, long packetNumber, byte[] bytesData, boolean kill){
        this.packetNumber = packetNumber;
        this.bytesData = bytesData;
        this.packetIndex = packetIndex;
        this.kill = kill;
    }
    //endregion

    //region Getters & Setters
    public long getPacketNumber() {
        return packetNumber;
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
    //endregion


}
