/***
 * This class represent a single downloaded packet
 * it contains the meta data attributes and the actual downloaded data
 */
public class DataWrapper {

    //region Fields
    private final int packetIndex;
    private long packetNumber;
    private byte[] packet;
    private boolean kill;
    //endregion

    //region Constructor
    public DataWrapper(int packetIndex, long packetNumber, byte[] packet, boolean kill){
        this.packetNumber = packetNumber;
        this.packet = packet;
        this.packetIndex = packetIndex;
        this.kill = kill;
    }
    //endregion

    //region Getters & Setters
    public long getPacketNumber() {
        return packetNumber;
    }

    public byte[] getPacket() {
        return packet;
    }


    public int getPacketIndex() {
        return packetIndex;
    }

    public boolean getKillStatus() {
        return kill;
    }
    //endregion


}
