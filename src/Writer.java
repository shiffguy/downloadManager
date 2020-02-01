import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.LinkedBlockingQueue;

public class Writer implements Runnable {

    private String downloadedFilePath;
    private LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue;
    private MetaData metaData;
    private int statusOfProgressDownload;
    private boolean openingPrint;

    Writer(LinkedBlockingQueue<PacketBuilder> packetsBlockingQueue, MetaData metaData, String downloadedFileName) throws IOException {
        this.packetsBlockingQueue = packetsBlockingQueue;
        this.metaData = metaData;
        this.downloadedFilePath = downloadedFileName;
        this.statusOfProgressDownload = this.metaData.GetCounterOfDownloadedPackets() / this.metaData.GetNumberOfPackets();
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
            if (status != this.statusOfProgressDownload || openingPrint) {
                this.statusOfProgressDownload = status;
                DmUI.printDownloadStatus(this.statusOfProgressDownload);
                this.openingPrint = false;
            }
        }

        return isDownloadCompleted;
    }


    /**
     * Writes data using randomAccessFile object
     *
     * @param dataToWrite      byte array containing the data need to be written to the file
     * @param updatedPosition long number represent the position where the data need to written from
     */
    private void writeDataOfPacket(byte[] dataToWrite, long updatedPosition) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(downloadedFilePath, "rw")) {

            randomAccessFile.seek(updatedPosition);
            randomAccessFile.write(dataToWrite);
        } catch (IOException e) {
            DmUI.printFailedToWritePacket();
        }
    }

    /**
     * Create the destination file of the downloaded packets of data
     */
    private void createDestFile() throws IOException {
        File myFile = new File(this.downloadedFilePath);
        try {
            myFile.createNewFile();
        } catch (IOException e) {
            DmUI.printFileNotCreated();
            throw e;
        }
    }

    private boolean checkIfKill(PacketBuilder dataOfPacket) {
        return dataOfPacket.getKillStatus();
    }

}
