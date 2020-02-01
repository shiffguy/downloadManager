import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;

public class FileWriter implements Runnable {

    private int statusOfProgressDownload;
    private MetaData metaData;
    private LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue;
    private String destFile;
    private boolean openingPrint;

    FileWriter(LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue, MetaData metaData, String destFile) throws IOException {
        this.metaData = metaData;
        this.packetsBlockingQueue = packetsBlockingQueue;
        this.statusOfProgressDownload = this.metaData.GetCounterOfDownloadedPackets() / this.metaData.GetNumberOfPackets();
        this.destFile = destFile;
        this.openingPrint = true;
        createDestFile();
    }

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

    private void createDestFile() throws IOException {
        File cur = new File(this.destFile);
        try {
            cur.createNewFile();
        } catch (IOException e) {
            DmUI.printFileNotCreated();
        }
    }

    private boolean processSinglePacketData(PacketBuilder dataOfPacket){
        boolean isDownloadCompleted = this.isEndPacket(dataOfPacket);
        if (!isDownloadCompleted) {
            long updatedPosition = dataOfPacket.getPosition();
            int packetIndex = dataOfPacket.getIndex();
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

    private boolean isEndPacket(PacketBuilder dataOfPacket) {
        return dataOfPacket.getEndPacketStatus();
    }

}