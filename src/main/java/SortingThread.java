import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Simplified Sorting Thread
 */
public class SortingThread extends Thread {
    private final Random random = new Random();
    private int containersCreated = 0;
    private int boxesSorted = 0;
    private int batchesCompleted = 0;
    
    private final Map<String, List<Box>> zoneBatches = new HashMap<>();
    private final List<List<Box>> completedBatches = new ArrayList<>();
    
    public SortingThread() {
        super(Constants.SORTER_THREAD + "1");
    }
    
    @Override
    public void run() {
        System.out.println("Sorter: Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning() || !SharedResources.sortingQueue.isEmpty()) {
                Box box = SharedResources.sortingQueue.poll(1, TimeUnit.SECONDS);
                
                if (box != null) {
                    processBox(box);
                    checkForContainers();
                } else if (SharedResources.isIntakeComplete()) {
                    processRemainingBatches();
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Sorter: Completed - " + boxesSorted + " boxes sorted into " + 
                          batchesCompleted + " batches, " + containersCreated + " containers created (Thread: " + getName() + ")");
    }
    
    private void processBox(Box box) throws InterruptedException {
        // Simulate sorting time
        Thread.sleep(random.nextInt(100) + 50);
        
        String zone = box.getZone();
        List<Box> zoneBatch = zoneBatches.computeIfAbsent(zone, k -> new ArrayList<>());
        zoneBatch.add(box);
        boxesSorted++;
        
        // Increment orders processed when box is successfully sorted
        SharedResources.incrementOrdersProcessed();
        
        System.out.println("Sorter: Box from Order #" + box.getOrder().getOrderId() + 
                          " sorted to " + zone + " (" + zoneBatch.size() + "/6 boxes in zone batch) (Thread: " + getName() + ")");
        
        // Complete batch when 6 boxes
        if (zoneBatch.size() >= Constants.BOXES_PER_BATCH) {
            List<Box> batch = new ArrayList<>();
            for (int i = 0; i < Constants.BOXES_PER_BATCH && !zoneBatch.isEmpty(); i++) {
                batch.add(zoneBatch.remove(0));
            }
            completedBatches.add(batch);
            batchesCompleted++;
            
            System.out.println("Sorter: Completed Batch #" + batchesCompleted + 
                              " for " + zone + " (6 boxes) - Total batches ready: " + 
                              completedBatches.size() + " (Thread: " + getName() + ")");
        }
    }
    
    private void checkForContainers() throws InterruptedException {
        // Create container when we have 5 batches (30 boxes)
        int batchesNeeded = Constants.BOXES_PER_CONTAINER / Constants.BOXES_PER_BATCH;
        
        while (completedBatches.size() >= batchesNeeded) {
            createContainer();
        }
    }
    
    private void createContainer() throws InterruptedException {
        Container container = new Container(getNextContainerId());
        
        System.out.println("Sorter: Creating Container #" + container.getContainerId() + 
                          " from 5 batches (30 boxes) (Thread: " + getName() + ")");
        
        // Add 5 batches to container
        for (int i = 0; i < 5 && !completedBatches.isEmpty(); i++) {
            List<Box> batch = completedBatches.remove(0);
            System.out.println("Sorter: Loading Batch " + (i + 1) + "/5 into Container #" + 
                              container.getContainerId() + " (6 boxes from batch) (Thread: " + getName() + ")");
            
            for (Box box : batch) {
                container.addBox(box);
            }
        }
        
        container.seal();
        containersCreated++;
        SharedResources.containersShipped.incrementAndGet();
        SharedResources.loadingQueue.put(container);
        
        System.out.println("Sorter: Container #" + container.getContainerId() + 
                          " sealed and sent to loading queue with " + container.getBoxCount() + 
                          " boxes (Thread: " + getName() + ")");
    }
    
    private void processRemainingBatches() throws InterruptedException {
        System.out.println("Sorter: Processing remaining batches - " + completedBatches.size() + 
                          " batches left (Thread: " + getName() + ")");
        
        // CRITICAL FIX: Process ALL remaining individual boxes in zoneBatches
        int totalRemainingBoxes = 0;
        for (List<Box> zoneBatch : zoneBatches.values()) {
            totalRemainingBoxes += zoneBatch.size();
        }
        
        System.out.println("Sorter: Found " + totalRemainingBoxes + " individual boxes remaining in zones");
        
        // Convert remaining individual boxes to final batches
        for (Map.Entry<String, List<Box>> entry : zoneBatches.entrySet()) {
            String zone = entry.getKey();
            List<Box> zoneBatch = entry.getValue();
            
            if (!zoneBatch.isEmpty()) {
                List<Box> finalBatch = new ArrayList<>(zoneBatch);
                completedBatches.add(finalBatch);
                batchesCompleted++;
                
                System.out.println("Sorter: Created final partial batch for " + zone + 
                                  " with " + finalBatch.size() + " boxes");
                zoneBatch.clear();
            }
        }
        
        System.out.println("Sorter: Total batches to containerize: " + completedBatches.size());
        
        // Create final containers from ALL remaining batches
        while (!completedBatches.isEmpty()) {
            Container container = new Container(getNextContainerId());
            int boxesInContainer = 0;
            int batchesUsed = 0;
            
            // Fill container with remaining batches
            while (!completedBatches.isEmpty() && boxesInContainer < Constants.BOXES_PER_CONTAINER) {
                List<Box> batch = completedBatches.remove(0);
                batchesUsed++;
                
                for (Box box : batch) {
                    if (boxesInContainer < Constants.BOXES_PER_CONTAINER) {
                        container.addBox(box);
                        boxesInContainer++;
                    }
                }
            }
            
            container.seal();
            containersCreated++;
            SharedResources.containersShipped.incrementAndGet();
            SharedResources.loadingQueue.put(container);
            
            System.out.println("Sorter: Final Container #" + container.getContainerId() + 
                              " created from " + batchesUsed + " batches with " + 
                              boxesInContainer + " boxes");
        }
        
        System.out.println("Sorter: Finished - total containers created: " + containersCreated);
    }
    
    private static int containerIdCounter = 1;
    private synchronized int getNextContainerId() {
        return containerIdCounter++;
    }
    
    public int getContainersCreated() { return containersCreated; }
    public int getBoxesSorted() { return boxesSorted; }
}