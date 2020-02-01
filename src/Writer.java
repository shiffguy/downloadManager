import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;

public class Writer implements Runnable {

    private int statusOfProgressDownload;
    private MetaData metaData;
    private LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue;
    private String destFile;
    private boolean openingPrint;

    Writer(LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue, MetaData metaData, String destFile) throws IOException {
        this.metaData = metaData;
        this.packetsBlockingQueue = packetsBlockingQueue;
        this.statusOfProgressDownload = this.metaData.GetCounterOfDownloadedPackets() / this.metaData.GetNumberOfPackets();
        this.destFile = destFile;
        this.openingPrint = true;
        createDestFile();
    }

    /**
     * Write all the data to the dest file.
     */
    @Override
    public void run() {
        boolean isDownloadCompleted = false;
        while (!isDownloadCompleted) {
            PacketBuilder dataOfPacket = packetsBlockingQueue.poll();
            if (dataOfPacket != null) {
                isDownloadCompleted = this.processSinglePacketData(dataOfPacket);
            }
        }
        boolean isWholeFileCompleteDownload = this.metaData.IsDownloadCompleted();
        if (isWholeFileCompleteDownload) {
            this.metaData.deleteMetaData();
            DmUI.printDownloadSucceeded();
        }
        else{
            DmUI.printDownloadFailed();
        }
    }

    /**
     * Checks the packet, until it is not the kill packet will write it to the file
     * @param dataOfPacket curr packet of data
     * @return true if we got kill packet and thus download is completed
     */
    private boolean processSinglePacketData(PacketBuilder dataOfPacket){
        boolean isDownloadCompleted = this.checkIfKill(dataOfPacket);
        if (!isDownloadCompleted) {
            long updatedPosition = dataOfPacket.getPacketPosition();
            int packetIndex = dataOfPacket.getPacketIndex();
            byte[] DataBytesOfPacketToWrite = dataOfPacket.getBytesData();

            writeDataOfPacket(DataBytesOfPacketToWrite, updatedPosition);
            metaData.UpdateIndex(packetIndex);
            double downloadCounterStatus = metaData.GetCounterOfDownloadedPackets();
            int status = (int) ((downloadCounterStatus / metaData.GetNumberOfPackets()) * 100);
            if (status > this.statusOfProgressDownload || openingPrint) {
                this.statusOfProgressDownload = status;
                DmUI.printDownloadStatus(this.statusOfProgressDownload);
                this.openingPrint = false;
            }
        }

        return isDownloadCompleted;
    }



    private void writeDataOfPacket(byte[] packetData, long updatedPosition) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(destFile, "rw")) {

            randomAccessFile.seek(updatedPosition);
            randomAccessFile.write(packetData);
        } catch (IOException e) {
            DmUI.printFailedToWritePacket();
        }
    }


    private void createDestFile() throws IOException {
        File cur = new File(this.destFile);
        try {
            cur.createNewFile();
        } catch (IOException e) {
            DmUI.printFileNotCreated();
            throw e;
        }
    }

    private boolean checkIfKill(PacketBuilder dataOfPacket) {
        return dataOfPacket.getKillStatus();
    }

}