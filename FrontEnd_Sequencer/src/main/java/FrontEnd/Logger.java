package FrontEnd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Logger {

    List<String> logs;

    public Logger() {
        logs = new ArrayList<String>();
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        logs.add("Timestamp: " + timeStamp);
    }

    public void addToLogs(String message) {
        logs.add(message);
        System.out.println(message);
    }

    public void flush() {
        try {
            String LOGFilePath = System.getProperty("user.dir") + "/FrontEndLogs.txt";
            File myObj = new File(LOGFilePath);

            System.out.println(myObj.getAbsolutePath());

            myObj.createNewFile();
            PrintStream myWriter = new PrintStream(new FileOutputStream(myObj, true));

            myWriter.println("=========================================");
            for (String message : logs) {
                myWriter.println(message);
            }
            myWriter.println("=========================================");

            myWriter.flush();
            myWriter.close();
        }

        catch (IOException e) {
            System.out.println("Unable to Write to Log File");
            e.printStackTrace();
        }
    }
}
