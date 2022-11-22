import java.io.File;
import java.io.IOException;

public class ReplicaManager {
    public static void main(String[] args) {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "replica3.jar");
        File f = new File("ReplicaJars");
        System.out.println(f.exists());
        pb.directory(f);

        Process replica3;
        try {
            replica3 = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
