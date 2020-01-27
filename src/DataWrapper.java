/***
 * This class represent a single downloaded packet
 * it contains the meta data attributes and the actual downloaded data
 */
public class DataWrapper {

    //region Fields
    private final int packetIndex;
    private long packetNumber;
    private byte[] packet;
    //endregion

    //region Constructor
    public DataWrapper(int packetIndex, long packetNumber, byte[] packet){
        this.packetNumber = packetNumber;
        this.packet = packet;
        this.packetIndex = packetIndex;
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
    //endregion


}
