package myLogger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyLogger {
    /**
     * NOTE:
     * Simple loggers used for debugging purposes. The loggers assume that there is a folder "files" which contain two folders "debug" and "results".
     * New file is created for every simulation, without overwriting previous files.
     */

    private final static Logger debugLogger = Logger.getLogger("debugLogger");
    private final static Logger resultLogger = Logger.getLogger("resultLogger");

    public MyLogger(Level debugLevel){
        try {
            /* Initialize logger for debugging purposes */
            Handler debugHandler = new FileHandler("files/debug/debugFile%g.txt", 400000000, 100);
            debugHandler.setFormatter(new MyFormatter());
            this.debugLogger.setLevel(debugLevel);
            this.debugLogger.addHandler(debugHandler);
            this.debugLogger.setUseParentHandlers(false);
            this.debugLogger.log(Level.FINEST, "Created debugLogger");

            /* Initialize logger for printing out results (packet latency etc.) */
            Handler resultHandler = new FileHandler("files/results/resultFile%g.txt", 400000000, 100);
            resultHandler.setFormatter(new MyFormatter());
            this.resultLogger.setLevel(Level.INFO);
            this.resultLogger.addHandler(resultHandler);
            this.resultLogger.setUseParentHandlers(false);
            this.debugLogger.log(Level.FINEST, "Created resultLogger");


        } catch (IOException e){
            System.out.println("Error while initializing loggers");
        }
    }
}
