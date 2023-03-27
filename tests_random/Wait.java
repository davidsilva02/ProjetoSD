public class Wait implements Runnable{
    Thread t;
    Object lock;

    public Wait(Object lock){
        super();
        this.lock=lock;
        t= new Thread(this,"t");
        t.start();
    }
    @Override
    public void run() {
       while(true){
        synchronized(lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("Acordaram-me");
       }
    }
    
}
