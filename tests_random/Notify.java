public class Notify {
    public static void main(String[] args) {
        Object lock = new Object();
        new Wait(lock);
        
        while(true){

            synchronized(lock){
                lock.notify();
            }
            System.out.println("Notifiquei");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
