import java.util.ArrayList;
import java.util.List;

/**
 * Simplified SwiftCart Main Class
 */
public class SwiftCartMain {
    
    private static Statistics statistics;
    private static OrderIntakeThread orderIntakeThread;
    private static final List<PickingStationThread> pickingThreads = new ArrayList<>();
    private static PackingStationThread packingThread;
    private static LabellingStationThread labellingThread;
    private static SortingThread sortingThread;
    private static final List<LoaderThread> loaderThreads = new ArrayList<>();
    private static TruckThread dispatcherTruck;
    private static RejectHandler rejectHandler;
    private static AGVFailureSimulator agvFailureSimulator;
    private static ThreadsShutdown shutdownManager;
    
    public static void main(String[] args) {
        System.out.println(StringUtils.repeat("=", 70));
        System.out.println(StringUtils.center("SWIFTCART E-COMMERCE SIMULATION", 70));
        System.out.println(StringUtils.center("Multiple Trip Truck System", 70));
        System.out.println(StringUtils.repeat("=", 70));
        
        try {
            initializeSimulation();
            startAllThreads();
            waitForCompletion();
        } catch (Exception e) {
            System.err.println("SwiftCartMain: Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            performShutdown();
        }
    }
    
    private static void initializeSimulation() {
        System.out.println("SwiftCartMain: Initializing...");
        statistics = new Statistics();
        statistics.startSimulation();
        SharedResources.initialize();
        printParameters();
    }
    
    private static void startAllThreads() {
        System.out.println("SwiftCartMain: Starting threads...");
        
        // Start shutdown timer with statistics
        shutdownManager = ThreadsShutdown.startShutdownMonitoring(statistics);
        
        // Start background systems
        rejectHandler = new RejectHandler();
        rejectHandler.start();
        
        agvFailureSimulator = new AGVFailureSimulator(statistics);
        agvFailureSimulator.start();
        
        // Start main processing threads
        orderIntakeThread = new OrderIntakeThread();
        orderIntakeThread.start();
        
        for (int i = 1; i <= Constants.MAX_CONCURRENT_PICKERS; i++) {
            PickingStationThread picker = new PickingStationThread(i);
            pickingThreads.add(picker);
            picker.start();
        }
        
        packingThread = new PackingStationThread();
        packingThread.start();
        
        labellingThread = new LabellingStationThread();
        labellingThread.start();
        
        sortingThread = new SortingThread();
        sortingThread.start();
        
        for (int i = 1; i <= Constants.MAX_AGVS; i++) {
            LoaderThread loader = new LoaderThread(i);
            loaderThreads.add(loader);
            loader.start();
        }
        
        dispatcherTruck = TruckThread.startTruckDispatchSystem(statistics);
        
        System.out.println("SwiftCartMain: All threads started");
    }
    
    private static void waitForCompletion() {
        try {
            if (shutdownManager != null) {
                shutdownManager.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void performShutdown() {
        System.out.println("SwiftCartMain: Performing shutdown...");
        
        if (rejectHandler != null) {
            rejectHandler.forceProcessRemainingBatch();
        }
        
        if (agvFailureSimulator != null) {
            agvFailureSimulator.forceRecoverAllAGVs();
        }
        
        // Print summary AFTER statistics report (which is already printed in ThreadsShutdown)
        printFinalSummary();
        System.out.println("SwiftCartMain: Shutdown completed");
    }
    
    private static void printParameters() {
        System.out.println("Parameters:");
        System.out.println("  Orders: " + Constants.TOTAL_ORDERS);
        System.out.println("  Interval: " + Constants.ORDER_INTERVAL_MS + "ms");
        System.out.println("  Pickers: " + Constants.MAX_CONCURRENT_PICKERS);
        System.out.println("  AGVs: " + Constants.MAX_AGVS);
        System.out.println("  Max Trips per Truck: " + Constants.CONTAINERS_PER_TRUCK);
        System.out.println("  Duration: " + (Constants.SIMULATION_DURATION_MS / 1000) + " seconds");
    }
    
    private static void printFinalSummary() {
        System.out.println("\n" + StringUtils.repeat("=", 50));
        System.out.println("FINAL SUMMARY");
        System.out.println(StringUtils.repeat("=", 50));
        
        if (orderIntakeThread != null) {
            System.out.println("Orders Generated: " + orderIntakeThread.getOrdersGenerated());
            System.out.println("Orders Accepted: " + orderIntakeThread.getOrdersAccepted());
        }
        
        int totalPicked = pickingThreads.stream().mapToInt(PickingStationThread::getOrdersPicked).sum();
        System.out.println("Orders Picked: " + totalPicked);
        
        if (packingThread != null) {
            System.out.println("Boxes Packed: " + packingThread.getBoxesPacked());
        }
        
        if (labellingThread != null) {
            System.out.println("Boxes Labelled: " + labellingThread.getBoxesLabelled());
        }
        
        if (sortingThread != null) {
            System.out.println("Containers Created: " + sortingThread.getContainersCreated());
        }
        
        int totalLoaded = loaderThreads.stream().mapToInt(LoaderThread::getContainersLoaded).sum();
        System.out.println("Containers Loaded: " + totalLoaded);
        System.out.println("Total Truck Dispatches: " + SharedResources.trucksDispatched.get());
        System.out.println("Orders Rejected: " + SharedResources.ordersRejected.get());
        
        System.out.println(StringUtils.repeat("=", 50));
    }
}