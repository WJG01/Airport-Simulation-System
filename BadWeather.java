import java.util.Random;

public class BadWeather extends Thread {
    private ATC atc;
    Random random = new Random();
    public BadWeather(ATC atc) {
        this.atc = atc;
    }

    public void run() {
        //generate probability of 1/5 for bad weather to happen
        boolean badweather = (random.nextInt(5 - 1) + 1) == 1;
        if (true) {
            try {
                Thread.sleep(2000);
                //lock runway if bad weather happen
                lockrunway();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void lockrunway() {
        atc.lockrunway();
    }

}
