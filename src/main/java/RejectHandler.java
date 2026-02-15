/**
 * Simplified Reject Handler
 */
public class RejectHandler extends Thread {
    private int rejectedOrdersProcessed = 0;
    
    public RejectHandler() {
        super("RejectHandler-1");
    }
    
    @Override
    public void run() {
        System.out.println("RejectHandler: Starting (Thread: " + getName() + ")");
        
        try {
            while (SharedResources.isRunning()) {
                // Simulate processing rejected orders
                int currentRejected = SharedResources.ordersRejected.get();
                if (currentRejected > rejectedOrdersProcessed) {
                    int newRejections = currentRejected - rejectedOrdersProcessed;
                    
                    for (int i = 0; i < newRejections; i++) {
                        Thread.sleep(100); // Process rejection
                        rejectedOrdersProcessed++;
                        
                        if (rejectedOrdersProcessed % 10 == 0) {
                            System.out.println("RejectHandler: Processed " + rejectedOrdersProcessed + 
                                             " rejected orders (Thread: " + getName() + ")");
                        }
                    }
                }
                
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("RejectHandler: Completed - " + rejectedOrdersProcessed + 
                          " rejected orders processed (Thread: " + getName() + ")");
    }
    
    public void forceProcessRemainingBatch() {
        // Process any remaining rejections
        int remaining = SharedResources.ordersRejected.get() - rejectedOrdersProcessed;
        if (remaining > 0) {
            System.out.println("RejectHandler: Force processing " + remaining + 
                             " remaining rejections (Thread: " + getName() + ")");
            rejectedOrdersProcessed += remaining;
        }
    }
    
    public int getTotalRejectedOrdersProcessed() { return rejectedOrdersProcessed; }
}