import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Truck Thread - 3 trucks total, each makes trips carrying one container per trip
 */
public class TruckThread extends Thread {
    private final int truckId;
    private final Random random = new Random();
    private int tripsCompleted = 0;
    private int totalContainersDelivered = 0;
    private boolean isDispatcher = false;
    private boolean isOperational = true;
    private final Statistics statistics;
    
    private static final AtomicInteger trucksCreated = new AtomicInteger(0);
    
    public TruckThread(int truckId, boolean isDispatcher, Statistics statistics) {
        super(Constants.TRUCK_THREAD + truckId);
        this.truckId = truckId;
        this.isDispatcher = isDispatcher;
        this.statistics = statistics;
    }
    
    @Override
    public void run() {
        try {
            if (isDispatcher) {
                startDispatcherMonitoring();
            }
            
            // Continuous operation until shutdown
            while (SharedResources.isRunning() || !SharedResources.loadingBayQueue.isEmpty()) {
                
                // Check if truck is operational
                if (!isOperational) {
                    Thread.sleep(1000); // Wait for repair
                    continue;
                }
                
                // Simulate random truck breakdown (2% chance)
                if (random.nextDouble() < 0.02) {
                    simulateBreakdown();
                    continue;
                }
                
                if (loadSingleContainer()) {
                    deliverAndReturn();
                    tripsCompleted++;
                    totalContainersDelivered++;
                } else {
                    // No containers available, check if we should continue waiting
                    if (SharedResources.isIntakeComplete() && SharedResources.loadingBayQueue.isEmpty()) {
                        break;
                    }
                    Thread.sleep(1000); // Wait for containers
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Truck-" + truckId + ": Completed all operations - " + 
                          totalContainersDelivered + " containers delivered in " + 
                          tripsCompleted + " trips (Thread: " + getName() + ")");
    }
    
    private void startDispatcherMonitoring() {
        Thread dispatcher = new Thread(() -> {
            try {
                // Create exactly 2 more trucks immediately (total 3 trucks)
                for (int i = 2; i <= 3; i++) {
                    TruckThread newTruck = new TruckThread(i, false, statistics);
                    newTruck.start();
                    trucksCreated.incrementAndGet();
                    System.out.println("Truck-" + truckId + ": Created Truck-" + i + 
                                     " (Total: " + trucksCreated.get() + " trucks active) (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    Thread.sleep(500); // Brief delay between truck creation
                }

                // Monitor and show bay status periodically
                while (SharedResources.isRunning() || !SharedResources.loadingBayQueue.isEmpty()) {
                    Thread.sleep(5000); // Check every 5 seconds
                    int containersInBay = SharedResources.loadingBayQueue.size();
                    int availableBays = SharedResources.loadingBaySemaphore.availablePermits();

                    if (containersInBay > 0 || availableBays < 2) {
                        System.out.println("Bay Status: " + containersInBay + " containers waiting, " + 
                                         (2 - availableBays) + "/2 bays occupied by trucks (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        dispatcher.setName("TruckDispatcher-" + truckId);
        dispatcher.setDaemon(true);
        dispatcher.start();
    }
    
    private void simulateBreakdown() throws InterruptedException {
        isOperational = false;
        System.out.println("Truck-" + truckId + ": Broken down (Thread: " + getName() + ")");
        
        // Schedule repair (3-5 seconds)
        Thread repairThread = new Thread(() -> {
            try {
                Thread.sleep(random.nextInt(2000) + 3000); // 3-5 seconds
                isOperational = true;
                System.out.println("Truck-" + truckId + ": Repaired and returned to bay (Thread: " + 
                                 Thread.currentThread().getName() + ")");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        repairThread.setName("TruckRepair-" + truckId);
        repairThread.setDaemon(true);
        repairThread.start();
    }
    
    private boolean loadSingleContainer() throws InterruptedException {
        long waitStartTime = System.currentTimeMillis();

        // Check if bays are full first
        if (SharedResources.loadingBaySemaphore.availablePermits() == 0) {
            System.out.println("Truck-" + truckId + ": Both loading bays occupied, waiting for free bay... (Thread: " + getName() + ")");
        }

        // BLOCK and wait until a bay becomes available (no timeout)
        SharedResources.loadingBaySemaphore.acquire(); // This will block until a bay is free

        try {
            long waitTime = System.currentTimeMillis() - waitStartTime;
            if (waitTime > 1000) { // Only record significant wait times
                statistics.addTruckWaitTime(waitTime);
                System.out.println("Truck-" + truckId + ": Waited " + (waitTime/1000.0) + " seconds for loading bay (Thread: " + getName() + ")");
            }

            Container container = SharedResources.loadingBayQueue.poll(2, TimeUnit.SECONDS);

            if (container != null) {
                long loadingStartTime = System.currentTimeMillis();

                // Simulate loading time for one container
                int loadingDelay = random.nextInt(200) + 100;
                Thread.sleep(loadingDelay);

                long loadingTime = System.currentTimeMillis() - loadingStartTime;
                statistics.addTruckLoadingTime(loadingTime);

                System.out.println("Truck-" + truckId + ": Loaded Container #" + 
                                 container.getContainerId() + " (" + 
                                 (2 - SharedResources.loadingBaySemaphore.availablePermits()) + 
                                 "/2 bays occupied) (Thread: " + getName() + ")");
                return true;
            } else {
                System.out.println("Truck-" + truckId + ": No containers available at loading bay (Thread: " + getName() + ")");
            }
            return false;

        } finally {
            SharedResources.loadingBaySemaphore.release();
            System.out.println("Truck-" + truckId + ": Released loading bay (" + 
                              SharedResources.loadingBaySemaphore.availablePermits() + 
                              "/2 bays now available) (Thread: " + getName() + ")");
        }
    }
    
    private void deliverAndReturn() throws InterruptedException {
        // Simulate departure to delivery hub
        System.out.println("Truck-" + truckId + ": Departing for delivery trip #" + 
                          (tripsCompleted + 1) + " (Thread: " + getName() + ")");
        
        SharedResources.trucksDispatched.incrementAndGet();
        
        // Simulate delivery time (1-2 seconds per container)
        Thread.sleep(random.nextInt(1000) + 1000);
        
        // Return to loading bay
        System.out.println("Truck-" + truckId + ": Returned from delivery, back at bay (Thread: " + getName() + ")");
        
        // Brief rest before next trip
        Thread.sleep(random.nextInt(200) + 100);
    }
    
    public static TruckThread startTruckDispatchSystem(Statistics statistics) {
        trucksCreated.set(1); // Start with 1 truck (the dispatcher)
        TruckThread dispatcher = new TruckThread(1, true, statistics);
        dispatcher.start();
        return dispatcher;
    }
    
    public int getTruckId() { return truckId; }
    public int getTotalContainersDelivered() { return totalContainersDelivered; }
    public int getTripsCompleted() { return tripsCompleted; }
}