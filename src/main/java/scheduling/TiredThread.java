package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {};

    private final int id;
    private final double fatigueFactor;

    private final AtomicBoolean alive = new AtomicBoolean(true);

    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false);

    private final AtomicLong timeUsed = new AtomicLong(0); 
    private final AtomicLong timeIdle = new AtomicLong(0); 
    private final AtomicLong idleStartTime = new AtomicLong(0);

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    public void newTask(Runnable task) {
        try {
            this.handoff.add(task);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Worker is not ready to accept a new task.");
        }
    }

    public void shutdown() {
        try {
            this.handoff.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        try {
            while (alive.get()) {
                Runnable task = handoff.take();

                if (task == POISON_PILL) {
                    alive.set(false);
                    break;
                }

                long currentTime = System.nanoTime();
                long idleDuration = currentTime - idleStartTime.get();
                timeIdle.addAndGet(idleDuration);

                busy.set(true);
                long startTime = System.nanoTime();
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    long endTime = System.nanoTime();
                    long executionDuration = endTime - startTime;
                    timeUsed.addAndGet(executionDuration);

                    busy.set(false);
                    idleStartTime.set(System.nanoTime());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int compareTo(TiredThread o) {
        return Double.compare(this.getFatigue(), o.getFatigue());
    }
}
