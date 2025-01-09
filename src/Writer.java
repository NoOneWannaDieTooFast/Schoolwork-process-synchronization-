import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Writer implements Runnable {
    // 读写锁，用于写者进程的锁定和解锁
    // Read-write lock used for locking and unlocking writer processes
    private final ReentrantReadWriteLock lock;

    // 写优先锁，用于在有写者等待时优先处理
    // Write-priority lock used to prioritize writers when they are waiting
    private final ReentrantReadWriteLock writePriorityLock;

    // 写者的唯一标识符
    // Unique identifier for the writer
    private final Integer id;

    // 用于显示输出的文本区域
    // Text area used to display output
    private final JTextArea outputArea;

    // 标志是否持有优先锁
    // Flag indicating whether the priority lock is held
    private boolean hasPriorityLock = false;

    public Writer(ReentrantReadWriteLock lock, ReentrantReadWriteLock writePriorityLock, JTextArea outputArea, Integer id) {
        this.lock = lock;
        this.writePriorityLock = writePriorityLock;
        this.id = id;
        this.outputArea = outputArea;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { // 循环直到线程被中断 // Loop until the thread is interrupted
            if (!hasPriorityLock) {
                // 写进程想访问资源，上优先锁
                // Writer process wants to access resources, locks the priority lock
                SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " locks the priority lock.\n"));
                writePriorityLock.writeLock().lock();
                hasPriorityLock = true; // 更新状态 // Update status
            }
            // 检查是否可以锁定写锁
            // Check if the write lock can be locked
            if (!lock.isWriteLocked() && lock.getReadLockCount() < 1) {
                SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " locks the resource.\n"));
                lock.writeLock().lock(); // 加锁写锁 // Lock the write lock

                if (hasPriorityLock) {
                    SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " unlocks the priority lock.\n"));
                    writePriorityLock.writeLock().unlock(); // 解锁优先锁 // Unlock the priority lock
                    hasPriorityLock = false; // 更新状态 // Update status
                }

                try {
                    SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " is writing...\n"));
                    Thread.sleep(200); // 模拟写入操作 // Simulate writing operation
                    SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " finished writing.\n"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态 // Restore interrupt status
                } finally {
                    SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " unlocks the resource.\n"));
                    lock.writeLock().unlock(); // 解锁写锁 // Unlock the write lock
                }
                try {
                    Thread.sleep(1800); // 增加写者在操作后休眠时间 // Increase sleep time after operation
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // 如果读锁或其他写锁被激活，写者进程等待
                // If read locks or another write lock are active, writer process waits
                SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " waiting, read locks or another write lock are active...\n"));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
