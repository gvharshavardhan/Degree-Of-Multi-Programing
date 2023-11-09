import java.util.*;
import java.util.concurrent.*;

class SystemMonitor {
    private Queue<Process> readyQueue;
    private int cpuCapacity;
    private int totalMemory;
    private ScheduledExecutorService executor;

    public SystemMonitor(int cpuCapacity, int totalMemory) {
        this.readyQueue = new LinkedBlockingQueue<>();
        this.cpuCapacity = cpuCapacity;
        this.totalMemory = totalMemory;
        this.executor = Executors.newScheduledThreadPool(cpuCapacity);
    }

    public void addProcess(Process process) {
        readyQueue.offer(process);
    }

    public void run() {
        // Execute processes using thread pool
        for (int i = 0; i < cpuCapacity; i++) {
            executor.execute(this::executeProcesses);
        }

        // Monitor degree of multiprogramming every 1 second
        executor.scheduleAtFixedRate(this::monitorDegreeOfMultiprogramming, 0, 1, TimeUnit.SECONDS);

        // Stop monitoring after a period (e.g., 10 seconds)
        executor.schedule(this::stopMonitoring, 10, TimeUnit.SECONDS);
    }

    private void executeProcesses() {
        while (!readyQueue.isEmpty()) {
            Process process = readyQueue.poll();
            executor.execute(() -> {
                process.execute();
                readyQueue.offer(process); // Add the process back to the queue
            });
        }
    }

    private void monitorDegreeOfMultiprogramming() {
        double degreeOfMultiprogramming = calculateDegreeOfMultiprogramming();
        System.out.println("Degree of Multiprogramming: " + degreeOfMultiprogramming);
    }

    public double calculateAverageCPUUtilization() {
        return 1.0 - (double) readyQueue.size() / cpuCapacity;
    }

    public double calculateMemoryUtilization() {
        int memoryRequired = readyQueue.stream().mapToInt(Process::getMemoryUsage).sum();
        return (double) memoryRequired / totalMemory;
    }

    public double calculateDegreeOfMultiprogramming() {
        double avgCPUUtilization = calculateAverageCPUUtilization();
        double memoryUtilization = calculateMemoryUtilization();

        return Math.min(avgCPUUtilization, memoryUtilization);
    }

    public void stopMonitoring() {
        executor.shutdownNow();
    }
}

class Process {
    private int memoryUsage;
    private int executionTime;

    public Process(int memoryUsage, int executionTime) {
        this.memoryUsage = memoryUsage;
        this.executionTime = executionTime;
    }

    public int getMemoryUsage() {
        return memoryUsage;
    }

    public void execute() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + executionTime;

        while (System.currentTimeMillis() < endTime) {
            // Simulate CPU utilization with some computational work
            int a = 10; // Example computational work
            for (int i = 0; i < 100000; i++) {
                a = (a * i + 5) / 3;
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Random random = new Random();

        int cpuCapacity = random.nextInt(10) + 1; // Random CPU capacity between 1 and 10
        int totalMemory = (random.nextInt(5) + 1) * 100; // Random total memory between 100 and 500 MB

        SystemMonitor systemMonitor = new SystemMonitor(cpuCapacity, totalMemory);

        int numProcesses = random.nextInt(5) + 1; // Random number of processes between 1 and 5

        for (int i = 1; i <= numProcesses; i++) {
            int memoryUsage = (random.nextInt(4) + 1) * 100; // Random memory usage between 100 and 400 MB
            int executionTime = (random.nextInt(5) + 1) * 1000; // Random execution time between 1000 and 5000 ms
            Process process = new Process(memoryUsage, executionTime);
            systemMonitor.addProcess(process);
        }

        // Start process execution and monitoring
        systemMonitor.run();
    }
}
