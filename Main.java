import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static void main(String[] args) {

        //initialize blocking queue for runway and gateways
        BlockingQueue<Runway> runways = new ArrayBlockingQueue<>(1);
        BlockingQueue<Gate> gates = new ArrayBlockingQueue<>(2);

        //generate 6 plane threads
        Plane[] plane = new Plane[6];

        //generate 1 ATC as a monitor, passing runway and gateway blocking queue as parameter
        ATC atc = new ATC(runways, gates);

        //generate 1 thread for bad weather, passing atc as parameter
        BadWeather bw = new BadWeather(atc);
        bw.start();

        //randomly generate the number of emergency plane
        Random random = new Random();
        int emergencyplane = random.nextInt(6 - 1) + 1;

        //start all the 6 planes' thread including the emergency plane
        for (int i = 0; i < 6; i++) {
            if (i == emergencyplane) {
                //set flag to true if equal to emergency plane number
                plane[i] = new Plane(i + 1, atc, true);
                plane[i].start();
            } else {
                //set flag to false for normal plane
                plane[i] = new Plane(i + 1, atc, false);
                plane[i].start();
            }

        }
        //make sure all child process of thread completed before printing the statistic
        for (int i = 0; i < 6; i++) {
            try {
                plane[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //print statistic
        atc.print_status_n_statistic();

    }
}

