# downloadManager
Names:
Itay Koren - 205782113
Guy Shiff - 308577469

Purpose(can be seen at the beginning of every class):

IdcDm.java - Holds the main method. Gets the args from the user and checks their validity and starts the download process.

DmUi.java - Manages the user interface prints.

UrlManager - Checks whether the input is url address or list of urls and creates the according list.

Downloader.java - Determines the data chunks and manages the working threads.

MetaData.java - MetaData constructor.

PacketBuilder.java - Creates packet including index and offset from which to start writing.

HTTPRangeGetter.java - Connects to the urls and queuing the chunks to the BlockingQueue.

FileWriter.java - Takes the chunks from the BlockingQueue and writes to the file.


