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
    private JTextArea outputArea;
    private JTextField producerField;
    private JTextField consumerField;
    private JTextField bufferField;
    private JTextField seconds1Field;
    private JButton producerConsumerButton;
    private JTextField seconds2Field;
    private JButton readerWriterButton;

    public MainWindow() {
        setTitle("Process Synchronization Simulation");
        setSize(800, 500); // 调整窗口大小
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        producerField = new JTextField(5);
        consumerField = new JTextField(5);
        bufferField = new JTextField(5);
        seconds1Field = new JTextField(5);
        producerConsumerButton = new JButton("Start Producer-Consumer");

        producerConsumerButton.addActionListener(this::simulateProducerConsumer);

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
        outputArea.setText(""); // 清屏

        try {
            int numProducers = Integer.parseInt(producerField.getText());
            int numConsumers = Integer.parseInt(consumerField.getText());
            int bufferSize = Integer.parseInt(bufferField.getText());

            Lock[] locks = new ReentrantLock[bufferSize];
            Condition[] conditions = new Condition[bufferSize];
            boolean[] bufferProduced = new boolean[bufferSize];

            for (int i = 0; i < bufferSize; i++) {
                locks[i] = new ReentrantLock();
                conditions[i] = locks[i].newCondition();
                bufferProduced[i] = false; // 初始化为未生产状态
            }

            List<Thread> producers = new ArrayList<>();
            List<Thread> consumers = new ArrayList<>();

            for (int i = 0; i < numProducers; i++) {
                producers.add(new Thread(new Producer(locks, conditions, bufferProduced, outputArea, bufferSize, i)));
            }
            for (int i = 0; i < numConsumers; i++) {
                consumers.add(new Thread(new Consumer(locks, conditions, bufferProduced, outputArea, bufferSize, i)));
            }

            producers.forEach(Thread::start);
            consumers.forEach(Thread::start);

            new Thread(() -> {
                try {
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                producers.forEach(Thread::interrupt);
                consumers.forEach(Thread::interrupt);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                SwingUtilities.invokeLater(() -> outputArea.append("Time is up! Stopping all processes.\n"));
                SwingUtilities.invokeLater(() -> outputArea.append("Simulation of Producer-Consumer is over.\n"));
            }).start();

//            new Thread(() -> {
//                try {
//                    for (Thread producer : producers) {
//                        producer.join();
//                    }
//                    for (Thread consumer : consumers) {
//                        consumer.join();
//                    }
//                    SwingUtilities.invokeLater(() -> outputArea.append("Simulation of Producer-Consumer is over.\n"));
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                    SwingUtilities.invokeLater(() -> outputArea.append("Simulation interrupted.\n"));
//                }
//            }).start();

        } catch (NumberFormatException ex) {
            outputArea.append("Please enter valid numbers for producers, consumers, and buffer size.\n");
        }
    }


    private void simulateReaderWriter(ActionEvent e) {
        outputArea.setText(""); // 清屏
        try {
            int seconds = Integer.parseInt(seconds2Field.getText());
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
                    Thread.sleep(seconds * 1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                writer1.interrupt();
                writer2.interrupt();
                reader1.interrupt();
                reader2.interrupt();
                reader3.interrupt();
                reader4.interrupt();
                reader5.interrupt();

                try {
                    Thread.sleep(500);
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
            app.setVisible(true);
        });
    }
}
