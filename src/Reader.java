import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Reader implements Runnable {
    // 读写锁，用于读者进程的锁定和解锁
    // Read-write lock used for locking and unlocking reader processes
    private final ReentrantReadWriteLock lock;

    // 写优先锁，用于在有写者等待时优先处理
    // Write-priority lock used to prioritize writers when they are waiting
    private final ReentrantReadWriteLock writePriorityLock;

    // 用于显示输出的文本区域
    // Text area used to display output
    private final JTextArea outputArea;

    // 读者的唯一标识符
    // Unique identifier for the reader
    private final Integer id;

    public Reader(ReentrantReadWriteLock lock, ReentrantReadWriteLock writePriorityLock, JTextArea outputArea, Integer id) {
        this.lock = lock;
        this.writePriorityLock = writePriorityLock;
        this.outputArea = outputArea;
        this.id = id;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // 循环直到线程被中断 // Loop until the thread is interrupted
            if (!lock.isWriteLocked()) { // 如果写锁未被上锁 // If the write lock is not locked
                // 如果写优先锁未被上锁
                // If the write-priority lock is not locked
                if (!writePriorityLock.isWriteLocked()) {
                    try {
                        // 有序加锁
                        // Synchronized locking
                        synchronized (Reader.class) {
                            if (lock.getReadLockCount() == 0) {
                                SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " locks the resource.\n"));
                            }
                            lock.readLock().lock(); // 加锁读锁 // Lock the read lock
                            SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " is reading...\n"));
                            SwingUtilities.invokeLater(() -> outputArea.append("Currently " + lock.getReadLockCount() + " readers are accessing the resource.\n"));

                            Thread.sleep(200); // 模拟读取操作 // Simulate reading operation
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // 恢复中断状态 // Restore interrupt status
                    } finally {
                        // 有序解锁
                        // Synchronized unlocking
                        synchronized (Reader.class) {
                            SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " finished reading.\n"));
                            lock.readLock().unlock(); // 解锁读锁 // Unlock the read lock
                            if (lock.getReadLockCount() <= 0) {
                                SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " unlocks the resource.\n"));
                            }
                        }

                        // 进程运行完，延迟片刻后继续循环
                        // After the process runs, delay for a moment before continuing the loop
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    // 如果写优先锁被激活，读者进程等待
                    // If the write-priority lock is active, reader process waits
                    SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " waiting, write priority lock is active...\n"));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            } else {
                // 如果写锁被激活，读者进程等待
                // If the write lock is active, reader process waits
                SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " waiting, write lock is active...\n"));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
