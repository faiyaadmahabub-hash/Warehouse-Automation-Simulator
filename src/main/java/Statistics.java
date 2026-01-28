import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-safe statistics collection for SwiftCart simulation
 */
public class Statistics {
    
    // Order and processing statistics
    private final AtomicInteger ordersReceived = new AtomicInteger(0);
    private final AtomicInteger ordersProcessed = new AtomicInteger(0);
    private final AtomicInteger boxesPacked = new AtomicInteger(0);
    private final AtomicInteger containersShipped = new AtomicInteger(0);
    private final AtomicInteger trucksDispatched = new AtomicInteger(0);
    
    // Rejection Statistics by Stage
    private final AtomicInteger ordersRejectedAtIntake = new AtomicInteger(0);
    private final AtomicInteger ordersRejectedAtPicking = new AtomicInteger(0);
    private final AtomicInteger ordersRejectedAtPacking = new AtomicInteger(0);
    private final AtomicInteger ordersRejectedAtLabelling = new AtomicInteger(0);
    private final AtomicInteger totalRejectedOrders = new AtomicInteger(0);
    
    // AGV Breakdown Statistics
    private final AtomicInteger agvBreakdownCount = new AtomicInteger(0);
    private final AtomicLong totalAGVDowntime = new AtomicLong(0);
    private final List<Long> agvDowntimes = Collections.synchronizedList(new ArrayList<>());
    
    // Capacity Constraint Statistics
    private final AtomicInteger capacityConstraintEvents = new AtomicInteger(0);
    private final AtomicLong totalCapacityWaitTime = new AtomicLong(0);
    private final List<Long> capacityWaitTimes = Collections.synchronizedList(new ArrayList<>());
    
    // Truck timing statistics
    private final List<Long> truckLoadingTimes = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> truckWaitTimes = Collections.synchronizedList(new ArrayList<>());
    
    // Simulation timing
    private final AtomicLong simulationStartTime = new AtomicLong(0);
    private final AtomicLong simulationEndTime = new AtomicLong(0);
    
    public void startSimulation() {
        simulationStartTime.set(System.currentTimeMillis());
    }
    
    public void endSimulation() {
        simulationEndTime.set(System.currentTimeMillis());
    }
    
    // Order tracking methods
    public void incrementOrdersReceived() { ordersReceived.incrementAndGet(); }
    public void incrementOrdersProcessed() { ordersProcessed.incrementAndGet(); }
    public void incrementBoxesPacked() { boxesPacked.incrementAndGet(); }
    public void incrementContainersShipped() { containersShipped.incrementAndGet(); }
    public void incrementTrucksDispatched() { trucksDispatched.incrementAndGet(); }
    
    // Rejection tracking methods
    public void incrementOrdersRejectedAtIntake() {
        ordersRejectedAtIntake.incrementAndGet();
        totalRejectedOrders.incrementAndGet();
    }
    
    public void incrementOrdersRejectedAtPicking() {
        ordersRejectedAtPicking.incrementAndGet();
        totalRejectedOrders.incrementAndGet();
    }
    
    public void incrementOrdersRejectedAtPacking() {
        ordersRejectedAtPacking.incrementAndGet();
        totalRejectedOrders.incrementAndGet();
    }
    
    public void incrementOrdersRejectedAtLabelling() {
        ordersRejectedAtLabelling.incrementAndGet();
        totalRejectedOrders.incrementAndGet();
    }
    
    // AGV breakdown tracking methods
    public void recordAGVBreakdown() {
        agvBreakdownCount.incrementAndGet();
    }
    
    public void addAGVDowntime(long downtimeMs) {
        totalAGVDowntime.addAndGet(downtimeMs);
        agvDowntimes.add(downtimeMs);
    }
    
    // Truck timing methods
    public void addTruckLoadingTime(long loadingTimeMs) {
        truckLoadingTimes.add(loadingTimeMs);
    }
    
    public void addTruckWaitTime(long waitTimeMs) {
        truckWaitTimes.add(waitTimeMs);
    }
    
    public double getAverageLoadingTime() {
        if (truckLoadingTimes.isEmpty()) return 0.0;
        
        long total = 0;
        synchronized (truckLoadingTimes) {
            for (Long time : truckLoadingTimes) {
                total += time;
            }
        }
        return (double) total / truckLoadingTimes.size();
    }
    
    public void printFinalReport() {
        System.out.println("\n" + StringUtils.repeat("=", 80));
        System.out.println(StringUtils.center("SWIFTCART FINAL SIMULATION REPORT", 80));
        System.out.println(StringUtils.center("Basic + Additional Requirements", 80));
        System.out.println(StringUtils.repeat("=", 80));

        // System clearance status
        System.out.println("SYSTEM CLEARANCE STATUS:");
        System.out.println("  Orders Received:      " + ordersReceived.get());
        System.out.println("  Boxes Packed:         " + boxesPacked.get());
        System.out.println("  Orders Processed:     " + ordersProcessed.get());
        System.out.println("  Containers Created:   " + containersShipped.get());
        System.out.println("  Containers Loaded:    " + containersShipped.get());
        System.out.println("  Trucks Dispatched:    " + trucksDispatched.get());

        // Rejection statistics
        System.out.println("\nREJECTION STATISTICS:");
        System.out.println("  Total Rejected Orders: " + totalRejectedOrders.get());
        System.out.println("  Order Success Rate:     " + String.format("%.1f%%", getOrderSuccessRate()));
        System.out.println("  Rejection Breakdown:    " + getRejectionBreakdown());

        // AGV breakdown statistics
        System.out.println("\nAGV BREAKDOWN STATISTICS:");
        if (agvBreakdownCount.get() == 0) {
            System.out.println("  No AGV breakdowns recorded");
        } else {
            System.out.println("  Total Breakdowns:       " + agvBreakdownCount.get());
            System.out.println("  Total Downtime:         " + String.format("%.2f seconds", totalAGVDowntime.get() / 1000.0));
        }

        // Capacity constraint statistics
        System.out.println("\nCAPACITY CONSTRAINT STATISTICS:");
        if (capacityConstraintEvents.get() == 0) {
            System.out.println("  No capacity constraints encountered");
        } else {
            System.out.println("  Capacity Constraint Events: " + capacityConstraintEvents.get());
            System.out.println("  Total Capacity Wait Time:   " + String.format("%.2f seconds", totalCapacityWaitTime.get() / 1000.0));
        }

        // Truck timing statistics
        System.out.println("\nTRUCK LOADING STATISTICS:");
        if (truckLoadingTimes.isEmpty()) {
            System.out.println("  No truck loading data recorded");
        } else {
            System.out.printf("  Minimum Loading Time: %d ms%n", getMinLoadingTime());
            System.out.printf("  Maximum Loading Time: %d ms%n", getMaxLoadingTime());
            System.out.printf("  Average Loading Time: %.2f ms%n", getAverageLoadingTime());
        }

        System.out.println("\nTRUCK WAIT STATISTICS:");
        if (truckWaitTimes.isEmpty()) {
            System.out.println("  No truck wait data recorded");
        } else {
            System.out.printf("  Minimum Wait Time:    %d ms%n", getMinWaitTime());
            System.out.printf("  Maximum Wait Time:    %d ms%n", getMaxWaitTime());
            System.out.printf("  Average Wait Time:    %.2f ms%n", getAverageWaitTime());
        }

        System.out.println(StringUtils.repeat("=", 80));
        System.out.println(StringUtils.center("SIMULATION COMPLETED", 80));
        System.out.println(StringUtils.repeat("=", 80));
    }
    
    
    // Capacity constraint tracking methods
    public void recordCapacityConstraintEvent() {
        capacityConstraintEvents.incrementAndGet();
    }
    
    public void addCapacityWaitTime(long waitTimeMs) {
        totalCapacityWaitTime.addAndGet(waitTimeMs);
        capacityWaitTimes.add(waitTimeMs);
    }
    
    
    

    

    
    // Calculate truck statistics
    public long getMinLoadingTime() {
        if (truckLoadingTimes.isEmpty()) return 0;
        return Collections.min(truckLoadingTimes);
    }
    
    public long getMaxLoadingTime() {
        if (truckLoadingTimes.isEmpty()) return 0;
        return Collections.max(truckLoadingTimes);
    }
    
    public long getMinWaitTime() {
        if (truckWaitTimes.isEmpty()) return 0;
        return Collections.min(truckWaitTimes);
    }
    
    public long getMaxWaitTime() {
        if (truckWaitTimes.isEmpty()) return 0;
        return Collections.max(truckWaitTimes);
    }
    
    public double getAverageWaitTime() {
        if (truckWaitTimes.isEmpty()) return 0.0;
        
        long total = 0;
        synchronized (truckWaitTimes) {
            for (Long time : truckWaitTimes) {
                total += time;
            }
        }
        return (double) total / truckWaitTimes.size();
    }
    

    
    // Calculate simulation duration
    public long getSimulationDuration() {
        long endTime = simulationEndTime.get();
        if (endTime == 0) {
            endTime = System.currentTimeMillis();
        }
        return endTime - simulationStartTime.get();
    }
    
    // Calculate success rate
    public double getOrderSuccessRate() {
        int received = ordersReceived.get();
        if (received == 0) return 0.0;
        return (double) ordersProcessed.get() / received * 100.0;
    }
    
    // Get rejection breakdown
    public String getRejectionBreakdown() {
        int total = totalRejectedOrders.get();
        if (total == 0) return "No rejections";
        
        return String.format("Intake:%d(%.1f%%), Picking:%d(%.1f%%), Packing:%d(%.1f%%), Labelling:%d(%.1f%%)",
                ordersRejectedAtIntake.get(), (double) ordersRejectedAtIntake.get() / total * 100.0,
                ordersRejectedAtPicking.get(), (double) ordersRejectedAtPicking.get() / total * 100.0,
                ordersRejectedAtPacking.get(), (double) ordersRejectedAtPacking.get() / total * 100.0,
                ordersRejectedAtLabelling.get(), (double) ordersRejectedAtLabelling.get() / total * 100.0);
    }
    
    // Getters
    public int getOrdersReceived() { return ordersReceived.get(); }
    public int getOrdersProcessed() { return ordersProcessed.get(); }
    public int getBoxesPacked() { return boxesPacked.get(); }
    public int getContainersShipped() { return containersShipped.get(); }
    public int getTrucksDispatched() { return trucksDispatched.get(); }
    public int getTotalRejectedOrders() { return totalRejectedOrders.get(); }
    public int getOrdersRejectedAtIntake() { return ordersRejectedAtIntake.get(); }
    public int getOrdersRejectedAtPicking() { return ordersRejectedAtPicking.get(); }
    public int getOrdersRejectedAtPacking() { return ordersRejectedAtPacking.get(); }
    public int getOrdersRejectedAtLabelling() { return ordersRejectedAtLabelling.get(); }
    public int getAGVBreakdownCount() { return agvBreakdownCount.get(); }
    public long getTotalAGVDowntime() { return totalAGVDowntime.get(); }
    public int getCapacityConstraintEvents() { return capacityConstraintEvents.get(); }
    public long getTotalCapacityWaitTime() { return totalCapacityWaitTime.get(); }
    
    /**
     * Print detailed final report when all trucks have departed
     */

}