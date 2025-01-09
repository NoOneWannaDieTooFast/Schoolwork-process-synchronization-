import javax.swing.JTextArea;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Producer implements Runnable {
    // 锁数组，用于同步各个缓冲区位置
    // Lock array used to synchronize each buffer position
    private final Lock[] locks;

    // 条件变量数组
    // Condition variable array
    private final Condition[] conditions;

    // 标记缓冲区位置是否有生产的数据
    // Indicates whether each buffer position has produced data
    private final boolean[] bufferProduced;

    // 用于显示输出的文本区域
    // Text area used to display output
    private final JTextArea outputArea;

    // 缓冲区大小
    // Size of the buffer
    private final int bufferSize;

    // 生产者的唯一标识符
    // Unique identifier for the producer
    private final int id;

    public Producer(Lock[] locks, Condition[] conditions, boolean[] bufferProduced, JTextArea outputArea, int bufferSize, int id) {
        this.locks = locks;
        this.conditions = conditions;
        this.bufferProduced = bufferProduced;
        this.outputArea = outputArea;
        this.bufferSize = bufferSize;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            // 遍历缓冲区的每个位置，进行生产操作
            // Iterate over each position in the buffer to perform production
            for (int i = 0; i < bufferSize; i = (i + 1) % bufferSize) {
                // 初步检查，减少锁定时间
                // Preliminary check to reduce lock holding time
                if (!bufferProduced[i]) {
                    locks[i].lock(); // 锁定当前缓冲区位置 // Lock the current buffer position
                    try {
                        // 如果当前缓冲区位置已生产数据，则等待消费
                        // Wait for consumption if the current buffer position has already produced data
                        while (bufferProduced[i]) {
                            outputArea.append("Producer " + id + " waiting, buffer of position " + i + " is full...\n");
                            conditions[i].await(); // 等待消费者信号 // Wait for consumer signal
                        }
                        // 生产数据
                        // Produce data
                        bufferProduced[i] = true;
                        outputArea.append("Producer " + id + " produced in position " + i + "\n");
                        conditions[i].signalAll(); // 唤醒消费者 // Wake up consumers
                    } finally {
                        locks[i].unlock(); // 解锁当前缓冲区位置 // Unlock the current buffer position
                    }
                    Thread.sleep(700); // 模拟生产后处理时间 // Simulate processing time after production
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态 // Restore interrupt status
        }
    }
}
