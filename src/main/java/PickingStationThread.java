import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Simplified Picking Station Thread
 */
public class PickingStationThread extends Thread {
    private final int pickerId;
    private final Random random = new Random();
    private int ordersPicked = 0;
    
    public PickingStationThread(int pickerId) {
        super(Constants.PICKER_THREAD + pickerId);
        this.pickerId = pickerId;
    }
    
    @Override
    public void run() {
        System.out.println("Picker-" + pickerId + ": Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning() || !SharedResources.pickingQueue.isEmpty()) {
                Order order = SharedResources.pickingQueue.poll(1, TimeUnit.SECONDS);
                
                if (order != null) {
                    // Simple rejection check
                    if (random.nextDouble() > Constants.REJECTION_RATE) {
                        processOrder(order);
                    } else {
                        SharedResources.ordersRejected.incrementAndGet();
                        String[] pickingReasons = {"out-of-stock items", "damaged items on shelf", "missing inventory"};
                        String reason = pickingReasons[random.nextInt(pickingReasons.length)];
                        System.out.println("Picker-" + pickerId + ": Order #" + order.getOrderId() + 
                                         " rejected at picking (" + reason + ") (Thread: " + getName() + ")");
                    }
                } else if (SharedResources.isIntakeComplete()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Picker-" + pickerId + ": Completed - " + ordersPicked + 
                          " orders picked (Thread: " + getName() + ")");
    }
    
    private void processOrder(Order order) throws InterruptedException {
        // Simulate picking time
        int pickingTime = Constants.PROCESSING_TIME_MIN + 
                         random.nextInt(Constants.PROCESSING_TIME_MAX - Constants.PROCESSING_TIME_MIN);
        Thread.sleep(pickingTime);
        
        ordersPicked++;
        SharedResources.packingQueue.put(order);
        
        System.out.println("Picker-" + pickerId + ": Order #" + order.getOrderId() + 
                          " picked (Thread: " + getName() + ")");
    }
    
    public int getOrdersPicked() { return ordersPicked; }
    public int getPickerId() { return pickerId; }
}