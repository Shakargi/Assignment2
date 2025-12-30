package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        
        workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = 0.5 + Math.random(); // Example fatigue factor between 0.5 and 1.5
            workers[i] = new TiredThread(i, fatigueFactor);
            workers[i].start(); // Start the worker thread
            idleMinHeap.add(workers[i]); // Initially, all workers are idle
        }
    }

    public void submit(Runnable task) {
        
        try {
            TiredThread worker = idleMinHeap.take(); // Get the least fatigued idle worker
            inFlight.incrementAndGet(); // Increment the count of in-flight tasks
            Runnable taskWrapper = () -> {
                try {
                    task.run(); // Execute the task
                } finally {
                    inFlight.decrementAndGet(); // Decrement the count of in-flight tasks
                    idleMinHeap.offer(worker); // Mark the worker as idle again
                    synchronized (this) {
                        notifyAll(); // Notify any waiting threads that a worker is now idle
                    }
                }
            };
            worker.newTask(taskWrapper);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        
        for (Runnable task : tasks) {
            submit(task);
        }

        // Barrier
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    wait(); // Wait until notified that a worker is idle
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
    
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
    }

    public synchronized String getWorkerReport() {

        StringBuilder report = new StringBuilder();
        report.append("Worker Report:\n");
        for (TiredThread worker : workers) {
            report.append("Worker ID: ").append(worker.getWorkerId())
                  .append(", Fatigue Factor: ").append(worker.getFatigue())
                  .append(", Time Used (ns): ").append(worker.getTimeUsed())
                  .append(", Time Idle (ns): ").append(worker.getTimeIdle())
                  .append("ms\n");
        }
        return report.toString();
    }
}
