import java.util.Random;

public class Plane extends Thread {
    Random random = new Random();
    ATC atc;
    //use to identify whether the plane is an emergency landing plane
    boolean flag;
    private final int flightNum;
    private long startTime;


    //Constructor . getting values from main
    public Plane(int flightNum, ATC atc, boolean flag) {
        this.flightNum = flightNum;
        this.atc = atc;
        this.flag = flag;


    }

    public void run() {
        approach_airport();

    }

    public void approach_airport() {
        if (flag == false) {
            try {
                //planes arrive in 0,1,2,3 seconds
                Thread.sleep(random.nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println();
            //print current time and plane request to land
            System.out.println(java.time.LocalTime.now() + " Plane " + flightNum + " : REQUEST FOR LANDING");
            //start timer once a plane approach the airport
            startTime = System.nanoTime();
            atc.landing(this);

        } else {
            System.out.println();
            //plane request for emergency landing
            System.out.println(java.time.LocalTime.now() + " Plane " + flightNum + " : REQUEST FOR EMERGENCY LANDING");
            startTime = System.nanoTime();
            atc.landing(this);
        }
    }


    public int getplaneno() {
        return flightNum;
    }


    // return the start time of the timer
    public long get_startTime() {
        return startTime;
    }

    public void notifyComplete() {
        requestTakeOff();
    }

    private void requestTakeOff() {
        System.out.println();
        System.out.println("Plane " + this.getplaneno() + ": READY! REQUEST FOR DEPARTING\n");
        try {
            atc.takeoff(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
