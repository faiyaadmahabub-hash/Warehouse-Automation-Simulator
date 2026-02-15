# SwiftCart E-commerce Centre - Concurrent Programming Simulation

**Author:** Faiyad Mahabub (TP077983)  
**Course:** CT074-3-2-CCP - Concurrent Programming  
**Lecturer:** DR. KUAN YIK JUNN

## Overview

SwiftCart E-commerce Centre is a multi-threaded Java application that demonstrates advanced concurrent programming concepts through modeling a fully automated warehouse fulfillment system. The simulation processes 600 orders at 500-millisecond intervals through six interconnected stages using a producer-consumer pipeline architecture.

## System Architecture

### Processing Pipeline

The system implements a producer-consumer pipeline with the following stages:

1. **Order Intake** - Receives and validates incoming orders
2. **Picking Station** - Retrieves items from warehouse inventory (4 concurrent pickers)
3. **Packing Station** - Packages orders into boxes
4. **Labelling Station** - Assigns destinations and tracking IDs
5. **Sorting Area** - Routes packages to appropriate containers
6. **Loading Bay & Transport** - Loads containers onto trucks (2 loading bays, 3 AGVs)

### Key Concurrency Features

- **Thread-Safe Data Transfer:** BlockingQueue implementation for safe inter-stage communication
- **Resource Management:** Semaphore-based control for limited resources (pickers, AGVs, loading bays)
- **Race Condition Prevention:** Atomic operations for shared counters and statistics
- **Deadlock Avoidance:** Timeout-based resource acquisition
- **Equipment Failure Simulation:** Probabilistic breakdown and recovery modeling
- **Comprehensive Statistics:** Thread-safe performance tracking across all operations

## Technical Implementation

### Core Concurrency Concepts

#### 1. Producer-Consumer Pattern
- BlockingQueue for thread-safe order passing between stages
- Decoupled timing between order generation and processing

#### 2. Worker Thread Pool
- Multiple concurrent pickers (4 threads) for parallel processing
- Timeout-based polling for graceful shutdown

#### 3. Resource Constraints
- **Bounded Capacity:** Limited queue sizes and container capacities
- **Semaphore Limits:** 4 concurrent pickers, 3 AGVs, 2 loading bays
- **Backpressure Handling:** Prevention of system overload during peak processing

#### 4. Failure Handling
- **Probabilistic Rejection:** Quality control simulation at each stage
- **AGV Breakdowns:** Random equipment failures with automatic recovery
- **Daemon Thread Recovery:** Non-blocking repair operations

#### 5. Thread Safety Mechanisms
- Atomic counters for race-free statistics tracking
- Synchronized collections for data aggregation
- Defensive initialization and state validation

## System Assumptions

**Truck Operation Model:** The implementation assumes single-container-per-trip operation rather than accumulating multiple containers. This design prioritizes continuous material flow and reduces loading bay congestion, implementing a just-in-time delivery model for steady system throughput.

## Key Components

### Main Classes

- **SwiftCartMain.java** - Application entry point and system orchestration
- **SharedResources.java** - Centralized resource and queue management
- **Statistics.java** - Thread-safe performance metrics tracking

### Thread Classes

- **OrderIntakeThread.java** - Order generation and validation
- **PickingStationThread.java** - Inventory retrieval with worker pool
- **PackingStationThread.java** - Single-threaded box creation
- **LabellingStationThread.java** - Destination assignment and tracking
- **SortingAreaThread.java** - Package routing to containers
- **LoaderThread.java** - AGV operations for container movement
- **TruckThread.java** - Container loading and transport

### Entity Classes

- **Order.java** - Immutable order representation
- **Box.java** - Package entity with quality flags
- **Container.java** - Capacity-enforced storage unit
- **RejectedOrder.java** - Defective order tracking

### Support Classes

- **RejectHandler.java** - Background rejection processing
- **AGVFailureSimulator.java** - Equipment breakdown simulation
- **Constants.java** - System configuration parameters
- **StringUtils.java** - Thread-safe utility methods

## Safety Features

### Concurrency Safety
- **Atomic Operations:** Lock-free counter updates
- **Synchronized Blocks:** Explicit synchronization for complex operations
- **Try-Finally Blocks:** Guaranteed resource release
- **Interrupt Handling:** Graceful thread termination

### Resource Safety
- **Capacity Enforcement:** Proactive size checking before operations
- **Timeout Operations:** Non-blocking resource acquisition with deadlock prevention
- **Semaphore Control:** Prevent resource oversubscription

## Performance Monitoring

The system tracks comprehensive statistics including:

- Total orders processed and rejected (by stage)
- Average processing times per stage
- AGV breakdown frequency and downtime
- Loading bay utilization
- Container fill rates
- Overall system throughput

## Configuration

Key configurable parameters (Constants.java):

- **Processing Times:** Variable delays for each stage
- **Resource Limits:** Number of pickers, AGVs, loading bays
- **Capacity Constraints:** Queue sizes, container capacities
- **Failure Rates:** Rejection probabilities, breakdown frequencies
- **System Scale:** Number of orders, processing intervals

## Learning Outcomes

This project demonstrates mastery of:

1. **Thread Lifecycle Management** - Creation, coordination, and graceful shutdown
2. **Synchronization Mechanisms** - Locks, semaphores, atomic operations
3. **Concurrent Data Structures** - BlockingQueue, synchronized collections
4. **Resource Management** - Limited resource allocation and deadlock avoidance
5. **Producer-Consumer Patterns** - Pipeline architectures with backpressure
6. **Failure Handling** - Probabilistic modeling and automatic recovery
7. **Performance Monitoring** - Thread-safe statistics in concurrent systems

## Requirements Fulfilled

### Basic Requirements ✓
- Multi-stage order processing pipeline
- Concurrent worker threads with resource limits
- Thread-safe data transfer between stages
- Proper state validation and quality control

### Additional Requirements ✓
- Defective order handling with rejection tracking
- Capacity constraints with overflow prevention
- Autonomous loader simulation with failures
- Concurrent activity coordination
- Comprehensive statistics reporting

## Technical Highlights

- **Scalable Architecture:** Easy to adjust resource counts and processing parameters
- **Realistic Simulation:** Probabilistic delays and failures mimic real-world operations
- **Debug Support:** Thread naming for clear identification in logs
- **Clean Separation:** Well-defined interfaces between processing stages
- **Robust Error Handling:** Interrupt-aware processing with proper cleanup

---

*This project represents a comprehensive implementation of concurrent programming principles applied to a real-world warehouse automation scenario, demonstrating both theoretical understanding and practical application of multi-threaded Java programming.*
