/**
 * Simplified Box entity
 */
public class Box {
    private final Order order;
    private String trackingId;
    private boolean verified;
    private boolean scanned;
    private String destination;
    
    public Box(Order order) {
        this.order = order;
        this.verified = false;
        this.scanned = false;
    }
    
    public void verify() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.verified = true;
    }
    
    public boolean isReady() {
        return verified && trackingId != null && scanned;
    }
    
    public void assignLabel(String destination) {
        this.trackingId = "A" + String.format("%03d", order.getOrderId());
        this.destination = destination;
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void scan() {
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.scanned = true;
    }
    
    
    
    public String getZone() {
        if (destination == null) return "ZONE-1";
        return "ZONE-" + ((Math.abs(destination.hashCode()) % 3) + 1);
    }
    
    // Getters
    public Order getOrder() { return order; }
    public String getTrackingId() { return trackingId; }
    public boolean isVerified() { return verified; }
    public boolean isScanned() { return scanned; }
    public String getDestination() { return destination; }
    
    @Override
    public String toString() {
        return "Box[Order #" + order.getOrderId() + ", Tracking:" + trackingId + 
               ", Zone:" + getZone() + "]";
    }
}