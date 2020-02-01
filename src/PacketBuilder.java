class PacketBuilder {

    private final int index;
    private long position;
    private byte[] bytesData;
    private boolean endPacket;

    PacketBuilder(int index, long position, byte[] bytesData){
        this.position = position;
        this.bytesData = bytesData;
        this.index = index;
        this.endPacket = false;
    }

    PacketBuilder(boolean endPacket) {
        this.position = 0;
        this.bytesData = null;
        this.index = 0;
        this.endPacket = endPacket;
    }

    boolean getEndPacketStatus() {
        return endPacket;
    }

    int getIndex() {
        return index;
    }

    long getPosition() {
        return position;
    }

    byte[] getBytesData() {
        return bytesData;
    }
}
