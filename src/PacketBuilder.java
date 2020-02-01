class PacketBuilder {

    private final int packetIndex;
    private long packetPosition;
    private byte[] bytesData;
    private boolean endPacket;

    PacketBuilder(int packetIndex, long packetPosition, byte[] bytesData){
        this.packetPosition = packetPosition;
        this.bytesData = bytesData;
        this.packetIndex = packetIndex;
        this.endPacket = false;
    }

    PacketBuilder(boolean endPacket) {
        this.packetPosition = 0;
        this.bytesData = null;
        this.packetIndex = 0;
        this.endPacket = endPacket;
    }

    boolean getEndPacketStatus() {
        return endPacket;
    }

    int getPacketIndex() {
        return packetIndex;
    }

    long getPacketPosition() {
        return packetPosition;
    }

    byte[] getBytesData() {
        return bytesData;
    }
}
