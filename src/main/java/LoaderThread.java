import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Simplified Loader Thread (AGV)
 */
public class LoaderThread extends Thread {
    private final int loaderId;
    private final Random random = new Random();
    private int containersLoaded = 0;
    
    public LoaderThread(int loaderId) {
        super(Constants.LOADER_THREAD + loaderId);
        this.loaderId = loaderId;
    }
    
    @Override
    public void run() {
        System.out.println("Loader-" + loaderId + ": Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning() || !SharedResources.loadingQueue.isEmpty()) {
                // Check if AGV is operational
                if (!SharedResources.isAGVOperational(loaderId - 1)) {
                    Thread.sleep(1000); // Wait for repair
                    continue;
                }
                
                Container container = SharedResources.loadingQueue.poll(2, TimeUnit.SECONDS);
                
                if (container != null) {
                    processContainer(container);
                } else if (SharedResources.isIntakeComplete()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Loader-" + loaderId + ": Completed - " + containersLoaded + 
                          " containers loaded (Thread: " + getName() + ")");
    }
    
    private void processContainer(Container container) throws InterruptedException {
        // Check if loading bay is full (capacity constraint)
        if (SharedResources.loadingBayQueue.size() >= Constants.MAX_CONTAINERS_AT_BAY) {
            System.out.println("Loader-" + loaderId + ": Loading bay full (" + 
                             SharedResources.loadingBayQueue.size() + "/" + 
                             Constants.MAX_CONTAINERS_AT_BAY + "), waiting... (Thread: " + getName() + ")");
            
            // Put container back and wait
            SharedResources.loadingQueue.put(container);
            Thread.sleep(2000);
            return;
        }
        
        // Try to acquire AGV and bay resources
        if (SharedResources.agvSemaphore.tryAcquire(3, TimeUnit.SECONDS) &&
            SharedResources.loadingBaySemaphore.tryAcquire(2, TimeUnit.SECONDS)) {
            
            try {
                // Simulate loading time
                int loadingTime = Constants.PROCESSING_TIME_MIN + 
                                 random.nextInt(Constants.PROCESSING_TIME_MAX - Constants.PROCESSING_TIME_MIN);
                Thread.sleep(loadingTime);
                
                containersLoaded++;
                SharedResources.loadingBayQueue.put(container);
                
                System.out.println("Loader-" + loaderId + ": Container #" + container.getContainerId() + 
                                 " moved to loading bay (" + SharedResources.loadingBayQueue.size() + 
                                 "/" + Constants.MAX_CONTAINERS_AT_BAY + " capacity) (Thread: " + getName() + ")");
                
            } finally {
                SharedResources.loadingBaySemaphore.release();
                SharedResources.agvSemaphore.release();
            }
        } else {
            // Put container back if can't acquire resources
            SharedResources.loadingQueue.put(container);
            Thread.sleep(500);
        }
    }
    
    public int getContainersLoaded() { return containersLoaded; }
    public int getLoaderId() { return loaderId; }
}