public class ReplicaShutdownThread extends Thread {
    Process[] processes;

    public ReplicaShutdownThread(Process... processes) {
        this.processes = processes;
    }

    @Override
    public void run() {
        for (Process p : processes)
            p.destroy();
    }
}
