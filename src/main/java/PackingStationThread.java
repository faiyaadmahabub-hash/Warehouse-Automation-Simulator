import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Simplified Packing Station Thread
 */
public class PackingStationThread extends Thread {
    private final Random random = new Random();
    private int boxesPacked = 0;
    
    public PackingStationThread() {
        super(Constants.PACKER_THREAD + "1");
    }
    
    @Override
    public void run() {
        System.out.println("Packer: Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning() || !SharedResources.packingQueue.isEmpty()) {
                Order order = SharedResources.packingQueue.poll(1, TimeUnit.SECONDS);
                
                if (order != null) {
                    // REMOVED BLOCKING SEMAPHORE - capacity constraint handled by loaders
                    
                    // Simple rejection check
                    if (random.nextDouble() > Constants.REJECTION_RATE) {
                        processOrder(order);
                    } else {
                        SharedResources.ordersRejected.incrementAndGet();
                        String[] packingReasons = {"packing errors", "damaged packaging", "items don't fit container"};
                        String reason = packingReasons[random.nextInt(packingReasons.length)];
                        System.out.println("Packer: Order #" + order.getOrderId() + 
                                         " rejected at packing (" + reason + ") (Thread: " + getName() + ")");
                    }
                } else if (SharedResources.isIntakeComplete()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Packer: Completed - " + boxesPacked + " boxes packed (Thread: " + getName() + ")");
    }
    
    private void processOrder(Order order) throws InterruptedException {
        // Simulate packing time
        int packingTime = Constants.PROCESSING_TIME_MIN + 
                         random.nextInt(Constants.PROCESSING_TIME_MAX - Constants.PROCESSING_TIME_MIN);
        Thread.sleep(packingTime);
        
        Box box = new Box(order);
        box.verify();
        
        boxesPacked++;
        SharedResources.boxesPacked.incrementAndGet();
        SharedResources.labellingQueue.put(box);
        
        System.out.println("Packer: Order #" + order.getOrderId() + " packed (Thread: " + getName() + ")");
    }
    
    public int getBoxesPacked() { return boxesPacked; }
}