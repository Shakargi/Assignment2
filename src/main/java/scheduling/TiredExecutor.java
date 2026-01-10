package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = 0.5 + Math.random(); 
            TiredThread worker = new TiredThread(i, fatigueFactor);
            workers[i] = worker;
            
            worker.start();
            
            idleMinHeap.add(worker);
        }
    }

    public void submit(Runnable task) {
        try {
            TiredThread worker = idleMinHeap.take();
            
            inFlight.incrementAndGet();

            Runnable taskWrapper = () -> {
                try {
                    task.run();
                } finally {
                    inFlight.decrementAndGet();
                    
                    idleMinHeap.offer(worker);
                    
                    synchronized (this) {
                        notifyAll();
                    }
                }
            };

            worker.newTask(taskWrapper);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks) {
            submit(task);
        }

        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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
        StringBuilder sb = new StringBuilder();
        sb.append("Worker Report:\n");
        for (TiredThread worker : workers) {
            sb.append("Worker ID: ").append(worker.getWorkerId())
              .append(", Fatigue: ").append(worker.getFatigue())
              .append(", Time Used: ").append(worker.getTimeUsed())
              .append(", Time Idle: ").append(worker.getTimeIdle())
              .append("\n");
        }
        return sb.toString();
    }

    
}
