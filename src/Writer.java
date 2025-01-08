import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Writer implements Runnable {
    private final ReentrantReadWriteLock lock;
    private final ReentrantReadWriteLock writePriorityLock;
    private final Integer id;
    private final JTextArea outputArea;
    private boolean hasPriorityLock = false; // 标志是否持有优先锁

    public Writer(ReentrantReadWriteLock lock, ReentrantReadWriteLock writePriorityLock, JTextArea outputArea, Integer id) {
        this.lock = lock;
        this.writePriorityLock = writePriorityLock;
        this.id = id;
        this.outputArea = outputArea;

    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!hasPriorityLock) {
                // 写进程想访问资源，上优先锁
                SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " locks the priority lock.\n"));
                writePriorityLock.writeLock().lock();
                hasPriorityLock = true; // 更新状态
            }
            if (!lock.isWriteLocked() && lock.getReadLockCount() < 1) {

                    SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " locks the resource.\n"));
                    lock.writeLock().lock();

                    if (hasPriorityLock) {
                        SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " unlocks the priority lock.\n"));
                        writePriorityLock.writeLock().unlock();
                        hasPriorityLock = false; // 更新状态
                    }

                    try {
                        SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " is writing...\n"));
                        Thread.sleep(200); // 模拟写入操作
                        SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " finished writing.\n"));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        SwingUtilities.invokeLater(() -> outputArea.append("Writer process: Writer process " + id + " unlocks the resource.\n"));
                        lock.writeLock().unlock();
                    }
                    try {
                        Thread.sleep(1800); // 增加写者在操作后休眠时间
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
            } else {
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
