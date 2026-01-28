/**
 * Simplified Order entity
 */
public class Order {
    private final int orderId;
    private boolean verified;
    private long timestamp;
    
    public Order(int orderId) {
        this.orderId = orderId;
        this.verified = false;
        this.timestamp = System.currentTimeMillis();
    }
    
    public void verify() {
        // Simulate verification time
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.verified = true;
    }
    
    public boolean isVerified() { return verified; }
    public int getOrderId() { return orderId; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "Order #" + orderId + " [Verified:" + verified + "]";
    }
}