package ua.kpi.parallels.services;

import ua.kpi.parallels.FileCrawler;
import ua.kpi.parallels.Indexer;
import ua.kpi.parallels.InvertedIndex;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelService implements IndexService {

    public static final File POISON = new File("");
    private final static int BOUND = 1000;
    private final int N_CONSUMERS;

    static final File root = new File("D:\\Nika\\Документы\\data_for_indexing\\aclImdb");

    public ParallelService(int N_CONSUMERS) {
        this.N_CONSUMERS = N_CONSUMERS;
    }

    @Override
    public void buildIndex(InvertedIndex index) {
        BlockingQueue<File> queue = new ArrayBlockingQueue<>(BOUND);
        FileFilter filter = f -> {
            if (!f.isDirectory()) {
                return f.getName().endsWith("txt");
            }
            return true;
        };
        Thread producer = new Thread(new FileCrawler(queue, filter, root));
        Thread[] consumers = new Thread[N_CONSUMERS];

        producer.start();
        for (int i = 0; i < N_CONSUMERS; i++) {
            consumers[i] = new Thread(new Indexer(queue, index));
            consumers[i].start();
        }

        try {
            producer.join();
            for (Thread consumer : consumers) {
                consumer.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}