import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Reader implements Runnable {
    private final ReentrantReadWriteLock lock;
    private final ReentrantReadWriteLock writePriorityLock;
    private final JTextArea outputArea;
    private final Integer id;

    public Reader(ReentrantReadWriteLock lock, ReentrantReadWriteLock writePriorityLock, JTextArea outputArea, Integer id) {
        this.lock = lock;
        this.writePriorityLock = writePriorityLock;
        this.outputArea = outputArea;
        this.id = id;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (!lock.isWriteLocked()) {
                // 如果写优先锁未被上锁
                if (!writePriorityLock.isWriteLocked()){
                    try {
                        // 有序加锁
                        synchronized (Reader.class) {
                            if (lock.getReadLockCount() == 0){
                                SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " locks the resource.\n"));
                            }
                            lock.readLock().lock();
                            SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " is reading...\n"));
                            SwingUtilities.invokeLater(() -> outputArea.append("Currently " + lock.getReadLockCount() + " readers are accessing the resource.\n"));

                            Thread.sleep(200); // 模拟读取操作
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        // 有序解锁
                        synchronized (Reader.class) {
                            SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " finished reading.\n"));
                            lock.readLock().unlock();
                            if (lock.getReadLockCount() <= 0){
                                SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " unlocks the resource.\n"));
                            }
                        }

                        // 进程运行完，延迟片刻后继续循环
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    SwingUtilities.invokeLater(() -> outputArea.append("Reader process: Reader process " + id + " waiting, write priority lock is active...\n"));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }


            } else {
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
