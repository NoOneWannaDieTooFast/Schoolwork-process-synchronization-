import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainWindow extends JFrame {
    private JTextArea outputArea; // 用于显示输出的文本区域 // Text area for displaying output
    private JTextField producerField; // 生产者数量输入框 // Input field for the number of producers
    private JTextField consumerField; // 消费者数量输入框 // Input field for the number of consumers
    private JTextField bufferField; // 缓冲区大小输入框 // Input field for buffer size
    private JTextField seconds1Field; // 模拟一的秒数输入框 // Input field for seconds in simulation 1
    private JButton producerConsumerButton; // 启动生产者-消费者按钮 // Button to start producer-consumer simulation
    private JTextField seconds2Field; // 模拟二的秒数输入框 // Input field for seconds in simulation 2
    private JButton readerWriterButton; // 启动读者-写者按钮 // Button to start reader-writer simulation

    public MainWindow() {
        setTitle("Process Synchronization Simulation");
        // 以下都是界面设计
        // UI design below
        setSize(800, 500); // 调整窗口大小 // Set window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作 // Set close operation
        setLocationRelativeTo(null); // 窗口居中 // Center window

        outputArea = new JTextArea();
        outputArea.setEditable(false); // 设置输出区域不可编辑 // Make output area non-editable
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // 构件细节设置
        // Component details setup
        producerField = new JTextField(5);
        consumerField = new JTextField(5);
        bufferField = new JTextField(5);
        seconds1Field = new JTextField(5);
        producerConsumerButton = new JButton("Start Producer-Consumer");

        // 为按钮添加动作监听器
        // Add action listener to button
        producerConsumerButton.addActionListener(this::simulateProducerConsumer);

        // 通过获取用户指定的各种参数（生产者个数、消费者个数和缓冲区大小，以及模拟进行的时间（秒））来保证模拟正常进行
        // The simulation is ensured by obtaining various parameters specified by the user (number of producers, number of consumers, buffer size, and simulation time in seconds)
        JPanel producerConsumerPanel = new JPanel();
        producerConsumerPanel.setBorder(BorderFactory.createTitledBorder("Producer-Consumer Settings"));
        producerConsumerPanel.setLayout(new GridLayout(0, 2, 1, 1));
        producerConsumerPanel.add(new JLabel("Producers:"));
        producerConsumerPanel.add(producerField);
        producerConsumerPanel.add(new JLabel("Consumers:"));
        producerConsumerPanel.add(consumerField);
        producerConsumerPanel.add(new JLabel("Buffer Size:"));
        producerConsumerPanel.add(bufferField);
        producerConsumerPanel.add(new JLabel("Seconds of Simulation 1:"));
        producerConsumerPanel.add(seconds1Field);
        producerConsumerPanel.add(producerConsumerButton);

        seconds2Field = new JTextField(5);
        readerWriterButton = new JButton("Start Reader-Writer");
        readerWriterButton.addActionListener(this::simulateReaderWriter);

        // 读者-写者问题的模拟中有5个读者、2个写者进程同时在系统中运行，用户只需要指定模拟运行时间即可进行模拟
        // In the simulation of the reader-writer problem, there are 5 reader and 2 writer processes running simultaneously in the system. The user only needs to specify the simulation time.
        JPanel readerWriterPanel = new JPanel();
        readerWriterPanel.setBorder(BorderFactory.createTitledBorder("Reader-Writer Settings"));
        readerWriterPanel.setLayout(new GridLayout(0, 2, 1, 1));
        readerWriterPanel.add(new JLabel("Seconds of Simulation 2:"));
        readerWriterPanel.add(seconds2Field);
        readerWriterPanel.add(readerWriterButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 1, 10, 10));
        controlPanel.add(producerConsumerPanel);
        controlPanel.add(readerWriterPanel);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void simulateProducerConsumer(ActionEvent e) {
        int seconds = Integer.parseInt(seconds1Field.getText());
        outputArea.setText(""); // 清屏 clean the output

        try {
            int numProducers = Integer.parseInt(producerField.getText()); // 获取生产者数量 // Get number of producers
            int numConsumers = Integer.parseInt(consumerField.getText()); // 获取消费者数量 // Get number of consumers
            int bufferSize = Integer.parseInt(bufferField.getText()); // 获取缓冲区大小 // Get buffer size

            Lock[] locks = new ReentrantLock[bufferSize];
            Condition[] conditions = new Condition[bufferSize];
            boolean[] bufferProduced = new boolean[bufferSize];

            for (int i = 0; i < bufferSize; i++) {
                locks[i] = new ReentrantLock();
                conditions[i] = locks[i].newCondition();
                bufferProduced[i] = false; // 初始化为未生产状态 // Initialize as unproduced
            }

            List<Thread> producers = new ArrayList<>();
            List<Thread> consumers = new ArrayList<>();

            // 创建生产者线程 Create producer threads
            for (int i = 0; i < numProducers; i++) {
                producers.add(new Thread(new Producer(locks, conditions, bufferProduced, outputArea, bufferSize, i)));
            }
            // 创建消费者线程 Create consumer threads
            for (int i = 0; i < numConsumers; i++) {
                consumers.add(new Thread(new Consumer(locks, conditions, bufferProduced, outputArea, bufferSize, i)));
            }

            producers.forEach(Thread::start); // 启动所有生产者线程 // Start all producer threads
            consumers.forEach(Thread::start); // 启动所有消费者线程 // Start all consumer threads

            // 启动计时器线程 Start timer thread
            new Thread(() -> {
                try {
                    Thread.sleep(seconds * 1000); // 模拟运行指定时间 // Run simulation for specified time
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                // 中断所有线程 Interrupt all threads
                producers.forEach(Thread::interrupt);
                consumers.forEach(Thread::interrupt);
                try {
                    Thread.sleep(500); // 等待线程终止 // Wait for threads to terminate
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                SwingUtilities.invokeLater(() -> outputArea.append("Time is up! Stopping all processes.\n"));
                SwingUtilities.invokeLater(() -> outputArea.append("Simulation of Producer-Consumer is over.\n"));
            }).start();
        } catch (NumberFormatException ex) {
            outputArea.append("Please enter valid numbers for producers, consumers, and buffer size.\n");
        }
    }

    private void simulateReaderWriter(ActionEvent e) {
        outputArea.setText(""); // 清屏
        try {
            int seconds = Integer.parseInt(seconds2Field.getText());
            // 一个写者进程和读者进程的互斥锁，一个写优先锁，5个读者进程，2个写者进程
            // A mutual exclusion lock for writer and reader processes, a write-priority lock, 5 reader processes, 2 writer processes
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
            ReentrantReadWriteLock writePriorityLock = new ReentrantReadWriteLock(true);
            Thread writer1 = new Thread(new Writer(lock, writePriorityLock, outputArea, 1));
            Thread writer2 = new Thread(new Writer(lock, writePriorityLock, outputArea, 2));
            Thread reader1 = new Thread(new Reader(lock, writePriorityLock, outputArea, 1));
            Thread reader2 = new Thread(new Reader(lock, writePriorityLock, outputArea, 2));
            Thread reader3 = new Thread(new Reader(lock, writePriorityLock, outputArea, 3));
            Thread reader4 = new Thread(new Reader(lock, writePriorityLock, outputArea, 4));
            Thread reader5 = new Thread(new Reader(lock, writePriorityLock, outputArea, 5));

            writer1.start();
            // 因为使用线程模拟并不能完全还原系统中进程同步的情况，需要延缓短暂时间避免缓冲区第一次同一时刻被多个进程抢占，以出现显示错误
            // Since using threads to simulate cannot fully restore the synchronization of processes in the system, it is necessary to delay for a short period of time to avoid the buffer being occupied by multiple processes at the same time for the first time, resulting in display errors
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            writer2.start();
            reader1.start();
            reader2.start();
            reader3.start();
            reader4.start();
            reader5.start();

            new Thread(() -> {
                try {
                    Thread.sleep(seconds * 1000); // 运行指定时间 // Run for specified time
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                // 中断所有线程 Interrupt all threads
                writer1.interrupt();
                writer2.interrupt();
                reader1.interrupt();
                reader2.interrupt();
                reader3.interrupt();
                reader4.interrupt();
                reader5.interrupt();

                try {
                    Thread.sleep(500); // 等待线程终止 // Wait for threads to terminate
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                SwingUtilities.invokeLater(() -> outputArea.append("Time is up! Stopping all processes.\n"));
            }).start();
        } catch (NumberFormatException ex) {
            outputArea.append("Please enter a valid number of seconds.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow app = new MainWindow();
            app.setVisible(true); // 显示窗口 // Show window
        });
    }
}
