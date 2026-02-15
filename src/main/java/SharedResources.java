import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simplified shared resources
 */
public class SharedResources {
    
    // Queues for pipeline stages
    public static final BlockingQueue<Order> pickingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Order> packingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Box> labellingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Box> sortingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Container> loadingQueue = new LinkedBlockingQueue<>();
    public static final BlockingQueue<Container> loadingBayQueue = new LinkedBlockingQueue<>();
    
    // Resource management
    public static final Semaphore agvSemaphore = new Semaphore(Constants.MAX_AGVS);
    public static final Semaphore loadingBaySemaphore = new Semaphore(Constants.MAX_LOADING_BAYS);
    public static final Semaphore containerCapacitySemaphore = new Semaphore(Constants.MAX_CONTAINERS_AT_BAY);
    
    // Simple counters
    public static final AtomicInteger ordersProcessed = new AtomicInteger(0);
    public static final AtomicInteger boxesPacked = new AtomicInteger(0);
    public static final AtomicInteger containersShipped = new AtomicInteger(0);
    public static final AtomicInteger trucksDispatched = new AtomicInteger(0);
    public static final AtomicInteger ordersRejected = new AtomicInteger(0);
    
    // Control flags  // Simple AGV status
    public static final AtomicBoolean simulationRunning = new AtomicBoolean(true);
    public static final AtomicBoolean intakeComplete = new AtomicBoolean(false);
    
   
    public static final AtomicBoolean[] agvOperational = new AtomicBoolean[Constants.MAX_AGVS];
    
    public static void breakdownAGV(int agvId) {
        if (agvId >= 0 && agvId < Constants.MAX_AGVS) {
            agvOperational[agvId].set(false);
            System.out.println("SharedResources: AGV-" + (agvId + 1) + " broken down");
        }
    }
    
    public static void repairAGV(int agvId) {
        if (agvId >= 0 && agvId < Constants.MAX_AGVS) {
            agvOperational[agvId].set(true);
            System.out.println("SharedResources: AGV-" + (agvId + 1) + " repaired");
        }
    }
    
    
    public static void initialize() {
        // Initialize AGVs as operational
        for (int i = 0; i < Constants.MAX_AGVS; i++) {
            agvOperational[i] = new AtomicBoolean(true);
        }
        System.out.println("SharedResources: Initialized");
    }
    
    public static void shutdown() {
        simulationRunning.set(false);
        System.out.println("SharedResources: Shutdown signaled");
    }
    
    public static boolean isRunning() {
        return simulationRunning.get();
    }
    
    public static void markIntakeComplete() {
        intakeComplete.set(true);
        System.out.println("SharedResources: Order intake completed");
    }
    
    public static boolean isIntakeComplete() {
        return intakeComplete.get();
    }
    
    // Simple AGV management

    
    public static boolean isAGVOperational(int agvId) {
        return agvId >= 0 && agvId < Constants.MAX_AGVS && agvOperational[agvId].get();
    }
    
    // Increment orders processed when box is successfully sorted
    public static void incrementOrdersProcessed() {
        ordersProcessed.incrementAndGet();
    }
    
    // Simple statistics
    public static void printFinalReport() {
        System.out.println("\n=== FINAL SIMULATION REPORT ===");
        System.out.println("Orders Processed: " + ordersProcessed.get());
        System.out.println("Orders Rejected: " + ordersRejected.get());
        System.out.println("Boxes Packed: " + boxesPacked.get());
        System.out.println("Containers Shipped: " + containersShipped.get());
        System.out.println("Trucks Dispatched: " + trucksDispatched.get());
        System.out.println("==============================");
    }
}