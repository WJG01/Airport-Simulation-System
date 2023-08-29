import java.util.Random;
import java.util.concurrent.BlockingQueue;

class ATC {
    //initialize blocking queue for shared resources (runway and gateway)
    private BlockingQueue<Runway> runways = null;
    private BlockingQueue<Gate> gates = null;

    //creating 2 gates and 1 runways objects
    private Runway runway;
    Gate gate1 = new Gate(1);
    Gate gate2 = new Gate(2);
    Random random = new Random();

    //To clearly differentiate the value of assignedagate variable for each Thread (not to mix up)
    private ThreadLocal<Gate> assignedgate = new ThreadLocal<Gate>();

    volatile boolean badweather = false;

    public ATC(BlockingQueue<Runway> runways, BlockingQueue<Gate> gates) {
        this.runways = runways;
        this.gates = gates;
        //add one runway to runway blocking queue
        runways.add(new Runway());
        //add two gateways to gateway blocking queue
        gates.add(gate1);
        gates.add(gate2);
    }


    public void checkgate_weather(Plane plane) {
        if (badweather)
        {
            System.out.println("\nATC: Plane " + plane.getplaneno() + " PLEASE JOIN CIRCLE QUEUE & WAIT FOR INSTRUCTION DUE TO BAD WEATHER.\n");
        }else{
            while (gates.isEmpty()) {
                System.out.println("\nATC: Plane " + plane.getplaneno() + " PLEASE JOIN CIRCLE QUEUE & WAIT FOR INSTRUCTION DUE TO GATES ARE FULL.\n");
                return;
            }
        }

    }

    public void landing(Plane plane) {
        //check for available gateway
        checkgate_weather(plane);
        try {
            // try to acquire gateway, if full,  wait
            //assign acquried gateway to threadlocal
            assignedgate.set(gates.take());
            Gate chosengatevalue = assignedgate.get();
            System.out.println();
            // try to acquire runway, if full, wait
            runway = runways.take();
            //permission granted by ATC to land
            System.out.println(java.time.LocalTime.now() + " ATC : Plane " + plane.getplaneno() + " - CAN USE RUNWAY");
            //stop the timer once plane access runway
            long endTime = System.nanoTime();
            //calculate the waiting time of  a plane
            long waiting_time = ((endTime - plane.get_startTime()) / 1000000000);
            System.out.println("ATC : Plane " + plane.getplaneno() + ": HAVE WAITED FOR " + waiting_time + " SECONDS");
            calstatistic(waiting_time);
            //Dock at assigned gateway
            System.out.println("ATC : Plane " + plane.getplaneno() + ": PLEASE DOCK AT GATE " + chosengatevalue.getName());
            System.out.println("ATC: Plane " + plane.getplaneno() + " - USING RUNWAY");
            //Release runway once docked
            runways.add(runway);
            System.out.println("ATC: Plane " + plane.getplaneno() + " - LEAVING RUNWAY");
            //Begin process in gateway
            accessgate(plane, chosengatevalue);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void accessgate(Plane plane, Gate gate) throws InterruptedException {
        System.out.println("ATC : Plane " + plane.getplaneno() + " : DOCK SUCCESSFULLY AT GATE " + gate.getName() + "\n");
        //start disembark passengers
        passengerdisembark(plane);
    }

    public void passengerdisembark(Plane plane) {
        //random generate number of passenger onboard
        int passengerno = random.nextInt(50 - 30) + 30;
        System.out.println("\tPlane " + plane.getplaneno() + " : DISEMBARK " + passengerno + " PASSENGERS ");
        for (int i = passengerno; i > 0; i--) {
            try {
                Thread.sleep(random.nextInt(500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\tPlane " + plane.getplaneno() + " : UNLOAD PASSENGER " + i);


        }
        //start refilling supply and cleaning plane
        refill_supply_n_cleaning(plane);
    }

    public void refill_supply_n_cleaning(Plane plane) {
        System.out.println("\tPlane " + plane.getplaneno() + " : REFILL SUPPLIES AND CLEANING PLANE -- START");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\tPlane " + plane.getplaneno() + " : REFILL SUPPLIES AND CLEANING PLANE -- FINISHED");
        //start refueling plane
        refuel_plane(plane);
    }


    public void refuel_plane(Plane plane) {//share one vacuum(mechanic)
        //refuel plane exclusively
        synchronized (this) {
            System.out.println();
            System.out.println("\tPlane " + plane.getplaneno() + " : REFUELLING PLANE -- START");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\tPlane " + plane.getplaneno() + " : REFUELLING PLANE -- FINISHED");
            System.out.println();
        }
        //start embarking new passenger
        embark_passenger(plane);

    }

    int boarded = 0;
    public void embark_passenger(Plane plane) {
        //random generate number of new passenger embark plane
        int passengerno = random.nextInt(50 - 30) + 30;
        System.out.println("\tPlane " + plane.getplaneno() + " : EMBARK " + passengerno + " PASSENGERS ");
        for (int i = 1; i <= passengerno; i++) {
            try {
                Thread.sleep(random.nextInt(500));
                System.out.println("\tPlane " + plane.getplaneno() + " : LOAD PASSENGER " + i);
                boarded++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        plane.notifyComplete();
    }

    public void takeoff(Plane plane) throws InterruptedException {
        Gate chosengatevalue = assignedgate.get();
        //try to acquire runway to takeoff
        runway = runways.take();
        System.out.println("ATC : Plane " + plane.getplaneno() + "- LEAVING GATE " + chosengatevalue.getName());
        System.out.println("ATC : Plane " + plane.getplaneno() + "- USING RUNWAY");
        System.out.println("ATC : Plane " + plane.getplaneno() + "- LEAVING RUNWAY");
        System.out.println();
        //release runway once taken off
        runways.add(runway);
        //release gateway once taken off
        gates.add(chosengatevalue);
    }

    long max = 0;
    long min = 0;
    long total = 0;
    int count = 0;
    double avg = 0;


    public void calstatistic(long waiting_time) {
        //find max waiting time
        if (waiting_time > max)
            max = waiting_time;
        //find min waiting time
        if (min == 0)
            min = waiting_time;
        else if (waiting_time < min)
            min = waiting_time;
        //calculate total waiting time
        total += waiting_time;
        count++;
        //calculate average waiting time
        avg = total / count;
    }

    public void check_airport_status() {
        //check runway status
        if (!runways.isEmpty())
            System.out.println("RUNWAY CLEAR");
        else
            System.out.println("RUNWAY NOT CLEAR");
        //check gateway 1 status
        if (gates.contains(gate1))
            System.out.println("GATE 1 CLEAR");
        else
            System.out.println("GATE 1 NOT CLEAR");
        //check gateway 2 status
        if (gates.contains(gate2))
            System.out.println("GATE 2 CLEAR");
        else
            System.out.println("GATE 2 NOT CLEAR");
    }

    public void print_status_n_statistic() {
        //print statistic report
        String repeated = new String(new char[40]).replace("\0", "-");
        System.out.println(repeated);
        System.out.println("\t\t\tAIRPORT STATUS");
        System.out.println(repeated);
        check_airport_status();
        System.out.println(repeated);
        System.out.println("\t\t\tSTATISTICS");
        System.out.println(repeated);
        System.out.println("MAX WAITING TIME    \t: " + max);
        System.out.println("MIN WAITING TIME    \t: " + min);
        System.out.println("AVERAGE WAITING TIME \t: " + avg);
        System.out.println("NO OF PLANE   \t\t\t: " + count);
        System.out.println("NO OF PASSENGER BOARDED : " + boarded);
    }

    public void lockrunway() {
        try {
            //acquire runway to lock the runway
            runway = runways.take();
            badweather = true;
            System.out.println("\nATC: BAD WEATHER AT AP AIRPORT. RUNWAY IS CLOSED.");
            Thread.sleep(4000);
            System.out.println("ATC: BAD WEATHER IS OVER. RUNWAY REOPEN.\n");
            badweather = false;
            //release runway once bad weather over
            runways.add(runway);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
