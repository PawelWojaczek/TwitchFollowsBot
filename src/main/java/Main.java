import Worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static Logger LOG = LoggerFactory.getLogger(Worker.class);

    public static void main(String[] args) {
        try {
            Worker worker = new Worker();
            worker.start();
        } catch (Exception e) {
            LOG.error("UNEXPECTED ERROR OCCURED. BOT STOPPED. EXCEPTION: ", e);
        }
        new Scanner(System.in).nextLine();
    }
}
