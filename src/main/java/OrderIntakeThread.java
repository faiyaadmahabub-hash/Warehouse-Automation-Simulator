import java.util.Random;

/**
 * Simplified Order Intake System
 */
public class OrderIntakeThread extends Thread {
    private final Random random = new Random();
    private int ordersGenerated = 0;
    private int ordersAccepted = 0;
    
    public OrderIntakeThread() {
        super(Constants.ORDER_THREAD + "1");
    }
    
    @Override
    public void run() {
        System.out.println("OrderIntake: Starting (Thread: " + getName() + ")");
        
        try {
            // Generate exactly 600 orders regardless of simulation running status
            while (ordersGenerated < Constants.TOTAL_ORDERS) {
                Order order = new Order(ordersGenerated + 1);
                
                // Simple rejection check
                if (random.nextDouble() > Constants.REJECTION_RATE) {
                    order.verify();
                    if (order.isVerified()) {
                        SharedResources.pickingQueue.put(order);
                        ordersAccepted++;
                        System.out.println("OrderIntake: Order #" + order.getOrderId() + 
                                         " accepted (Thread: " + getName() + ")");
                    }
                } else {
                        SharedResources.ordersRejected.incrementAndGet();
                        String[] intakeReasons = {"out-of-stock items", "payment verification failed", "invalid shipping address"};
                        String reason = intakeReasons[random.nextInt(intakeReasons.length)];
                        System.out.println("OrderIntake: Order #" + order.getOrderId() + 
                                         " rejected at intake (" + reason + ") (Thread: " + getName() + ")");
                }
                
                ordersGenerated++;
                
                // Maintain 500ms interval
                Thread.sleep(Constants.ORDER_INTERVAL_MS);
            }
            
            SharedResources.markIntakeComplete();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("OrderIntake: Completed - " + ordersAccepted + "/" + 
                          ordersGenerated + " orders accepted (Thread: " + getName() + ")");
    }
    
    public int getOrdersGenerated() { return ordersGenerated; }
    public int getOrdersAccepted() { return ordersAccepted; }
}