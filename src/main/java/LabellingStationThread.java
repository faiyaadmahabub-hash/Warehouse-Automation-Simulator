import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Simplified Labelling Station Thread
 */
public class LabellingStationThread extends Thread {
    private final Random random = new Random();
    private int boxesLabelled = 0;
    
    private final String[] destinations = {
        "Kuala Lumpur", "Selangor", "Penang", "Johor", "Perak"
    };
    
    public LabellingStationThread() {
        super(Constants.LABELLER_THREAD + "1");
    }
    
    @Override
    public void run() {
        System.out.println("Labeller: Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning() || !SharedResources.labellingQueue.isEmpty()) {
                Box box = SharedResources.labellingQueue.poll(1, TimeUnit.SECONDS);
                
                if (box != null) {
                    // Simple rejection check
                    if (random.nextDouble() > Constants.REJECTION_RATE) {
                        processBox(box);
                    } else {
                        SharedResources.ordersRejected.incrementAndGet();
                        String[] labellingReasons = {"mislabelling", "quality scanner failed", "barcode printing error"};
                        String reason = labellingReasons[random.nextInt(labellingReasons.length)];
                        System.out.println("Labeller: Order #" + box.getOrder().getOrderId() + 
                                         " rejected at labelling (" + reason + ") (Thread: " + getName() + ")");
                    }
                } else if (SharedResources.isIntakeComplete()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Labeller: Completed - " + boxesLabelled + " boxes labelled (Thread: " + getName() + ")");
    }
    
    private void processBox(Box box) throws InterruptedException {
        // Simulate labelling time
        int labellingTime = Constants.PROCESSING_TIME_MIN + 
                           random.nextInt(Constants.PROCESSING_TIME_MAX - Constants.PROCESSING_TIME_MIN);
        Thread.sleep(labellingTime);
        
        String destination = destinations[random.nextInt(destinations.length)];
        box.assignLabel(destination);
        box.scan();
        
        boxesLabelled++;
        SharedResources.sortingQueue.put(box);
        
        System.out.println("Labeller: Order #" + box.getOrder().getOrderId() + 
                          " labelled with " + box.getTrackingId() + " (Thread: " + getName() + ")");
    }
    
    public int getBoxesLabelled() { return boxesLabelled; }
}