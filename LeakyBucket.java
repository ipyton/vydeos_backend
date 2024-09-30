public class LeakyBucket {

    class Job {
        long timestamp;
        String content;

    }

    ConcurrentLinkedQueue<Job> queue = new ConcurrentLinkedQueue<Job>();
    long currentTime = 0;
    int qps = 0;
    LeakyBucket(int qps) {
        this.qps = qps;
    }

    public boolean put(Job job) {
        for (int i = 0; i < qps; i++) {

        }
    }


}
