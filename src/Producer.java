import javax.swing.JTextArea;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Producer implements Runnable {
    private final Lock[] locks;
    private final Condition[] conditions;
    private final boolean[] bufferProduced;
    private final JTextArea outputArea;
    private final int bufferSize;
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
            for (int i = 0; i < bufferSize; i = (i + 1) % bufferSize) {
                locks[i].lock();
                try {
                    while (bufferProduced[i]) { // 如果该位置已经被生产，等待消费
                        outputArea.append("Producer " + id + " waiting, buffer of position " + i + " is full...\n");
                        conditions[i].await();
                    }
                    // 生产数据
                    bufferProduced[i] = true;
//                    outputArea.append("Producer " + id + " is producing in position " + i + "\n");
                    Thread.sleep(350);

                    outputArea.append("Producer " + id + " produced in position " + i + "\n");
                    conditions[i].signalAll(); // 唤醒消费者
                } finally {
                    locks[i].unlock();
                }
                Thread.sleep(700);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
