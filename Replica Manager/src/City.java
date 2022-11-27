import java.io.File;
import java.io.IOException;

public enum City {
    MONTREAL(ReplicaManager.MONTREAL_REPLICA_PORT, "MTL", "Montreal"),
    TORONTO(ReplicaManager.TORONTO_REPLICA_PORT, "TOR", "Toronto"),
    VANCOUVER(ReplicaManager.VANCOUVER_REPLICA_PORT, "VAN", "Vancouver"),
    ;
    public final int replicaPort;
    public final String prefix;
    public final String name;
    public Process process;

    City(int replicaPort, String prefix, String name) {
        this.replicaPort = replicaPort;
        this.prefix = prefix;
        this.name = name;
    }

    public void startProcess(int implementationNumber) {
        boolean wait = false;
        if (process != null) {
            process.destroy();
            wait = true;
        }

        process = createReplica(name, implementationNumber);

        if (wait) {
            System.out.println("restarting a replica please wait... (1 second)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // making sure process is killed when the RM exits
        Runtime.getRuntime().addShutdownHook(new ReplicaShutdownThread(process));
    }

    private static Process createReplica(String cityName, int implementationNumber) {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "replica.jar",  cityName);
        pb.redirectErrorStream(true);
        File log = new File("logs/" + cityName + ".log");
        pb.redirectOutput(log);
        File f = new File("replica_implementations/impl" + String.valueOf(implementationNumber));
        pb.directory(f);
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  process;
    }

}
