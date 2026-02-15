/**
 * Simplified Configuration constants for SwiftCart simulation
 */
public class Constants {
    
    // Basic Requirements
    public static final int ORDER_INTERVAL_MS = 500;
    public static final int TOTAL_ORDERS = 600;
    public static final int MAX_CONCURRENT_PICKERS = 4;
    
    public static final int BOXES_PER_BATCH = 6;
    
    public static final int BOXES_PER_CONTAINER = 30;
    public static final int MAX_CONTAINERS_AT_BAY = 5;

    public static final int MAX_AGVS = 3;
    public static final int MAX_LOADING_BAYS = 2;
    public static final int CONTAINERS_PER_TRUCK = 18;
    public static final int SIMULATION_DURATION_MS = 5 * 60 * 1000;
    
    // Additional Requirements
    public static final double REJECTION_RATE = 0.10; // Simplified single rate
    
    
    public static final double AGV_BREAKDOWN_PROBABILITY = 0.05;
    public static final int AGV_RECOVERY_TIME = 3000; 


// Fixed recovery time
    
    // Processing Times - Simplified ranges
    public static final String PACKER_THREAD = "Packer-";
    public static final int PROCESSING_TIME_MIN = 100;
    public static final int PROCESSING_TIME_MAX = 500;
    
    // Thread naming
    public static final String ORDER_THREAD = "OrderThread-";
    public static final String PICKER_THREAD = "Picker-";
    
    public static final String LABELLER_THREAD = "Labeller-";
    public static final String SORTER_THREAD = "Sorter-";
    public static final String LOADER_THREAD = "Loader-";
    public static final String TRUCK_THREAD = "Truck-";
    
    
    
    
}