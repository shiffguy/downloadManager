/***
 * Class that builds single downloaded/writable packet
 * it contains data + metadata
 */
class PacketBuilder {

    private final int packetIndex;
    private long packetPosition;
    private byte[] bytesData;
    private boolean kill;

    PacketBuilder(int packetIndex, long packetPosition, byte[] bytesData){
        this.packetPosition = packetPosition;
        this.bytesData = bytesData;
        this.packetIndex = packetIndex;
        this.kill = false;
    }

    PacketBuilder(boolean kill) {
        this.packetPosition = 0;
        this.bytesData = null;
        this.packetIndex = 0;
        this.kill = kill;
    }

    long getPacketPosition() {
        return packetPosition;
    }

    byte[] getBytesData() {
        return bytesData;
    }

    int getPacketIndex() {
        return packetIndex;
    }

    boolean getKillStatus() {
        return kill;
    }

}
