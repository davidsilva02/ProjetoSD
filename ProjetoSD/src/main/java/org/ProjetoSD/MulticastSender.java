package org.ProjetoSD;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;

/**
 * Thread que retira classe JSOUPData da fila de mensagens e envia 
 * a classe para os barrels por Multicast
 */
public class MulticastSender implements Runnable {
    RMI searchModule;
    MulticastSocket socket;
    InetAddress group;
    Thread t;
    BlockingQueue<JSOUPData> l;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    AtomicInteger number_barrels;
    Object lock_changes;
    ReentrantLock lockFile;
    Integer countIterations = 0;

    public MulticastSender(String name, BlockingQueue<JSOUPData> l, AtomicInteger number_barrels, Object lock_changes) {

        super();

        this.l = l;
        lockFile = new ReentrantLock();

        // Get the reference to the server to future RMI calls
        try {
            this.searchModule = (RMI) Naming.lookup("rmi://localhost:3366/server");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
            System.exit(0);
        }

        try {
            this.socket = new MulticastSocket();
            this.group = InetAddress.getByName(MULTICAST_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.number_barrels = number_barrels;
        this.lock_changes = lock_changes;

        this.t = new Thread(this, name);
        t.start();
    }

    @Override
    public void run() {
        // primeira vez saca os barrels que tens
        // int number_of_barrels;
        try {
            number_barrels.set(searchModule.numberBarrels());
        } catch (RemoteException e) {
            number_barrels.set(-1);
            e.printStackTrace();
        }

        // caso o numero de barrels seja 0 por nao receber acks envia novamente o pacote
        // quando existirem barrels
        Boolean flag = true;
        JSOUPData j = null;
        while (true) {
            try {
                while (number_barrels.get() == 0 || number_barrels.get() == -1) {
                    synchronized (lock_changes) {
                        lock_changes.wait();
                    }
                }
                if (flag) {
                    j = l.take();
                    l.remove(j);
                }
                if (countIterations == 25) {
                new Thread(() -> {
                        lockFile.lock();
                        FileOps.writeToDisk(new File("./DW/l.bin"), (this.l));
                        lockFile.unlock();
                    }).start();
                    countIterations = 0;
                }

                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutputStream out;
                try {
                    out = new ObjectOutputStream(byteOut);
                    out.writeObject(j);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte beforeCompress[] = byteOut.toByteArray();

                // compressing object
                Deflater compressor = new Deflater(Deflater.BEST_SPEED);
                compressor.setInput(beforeCompress);
                compressor.finish();

                byte class_send[] = new byte[beforeCompress.length];
                int tamanho_envio = compressor.deflate(class_send);

                while (true) {

                    // enviar tamanho da classe que vamos mandar
                    // se 0 vamos enviar tamanho
                    byte buf[] = Integer.toString(beforeCompress.length).getBytes();

                    byte opt[] = { 0 };
                    byte[] send = new byte[buf.length + 1];

                    // copia para o primeiro byte a opcao,de seguida copia o buf para os restantes
                    // bytes
                    System.arraycopy(opt, 0, send, 0, 1);
                    System.arraycopy(buf, 0, send, 1, buf.length);

                    //

                    DatagramPacket packet = new DatagramPacket(send, send.length, group, PORT);
                    try {
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    HashSet<Integer> hashs = new HashSet<>();
                    // buscar barrels
                    for (int i = 0; i < number_barrels.get(); i++) {
                        byte buffe[] = new byte[20];
                        DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                        try {
                            socket.setSoTimeout(3000);
                            try {
                                socket.receive(rec);
                                String num = new String(rec.getData(), 0, rec.getLength());
                                hashs.add(Integer.parseInt(num));
                            } catch (SocketTimeoutException e) {
                                // e.printStackTrace();
                            } catch (IOException e) {
                                // e.printStackTrace();
                            }
                        } catch (SocketException e) {
                            // e.printStackTrace();
                        }
                    }

                    if (hashs.size() != number_barrels.get()) {
                        int barrels_faltam = number_barrels.get() - hashs.size();
                        // System.out.printf("FALTAM %d CONFIRMACOES", barrels_faltam);
                        // confirmação de barrels que faltam
                        int numero_tentativas = 1;
                        for (int i = 0; i < numero_tentativas; i++) {
                            // enviar de novo novamente
                            packet = new DatagramPacket(send, send.length, group, PORT);
                            try {
                                socket.send(packet);
                            } catch (IOException e) {
                            }

                            // esperamos pelas respostas dos barrels que faltam
                            for (int h = 0; h < barrels_faltam; h++) {
                                byte buffe[] = new byte[20];
                                DatagramPacket rec = new DatagramPacket(buffe, buffe.length);

                                try {
                                    socket.setSoTimeout(3000);
                                    try {
                                        socket.receive(rec);
                                        String num = new String(rec.getData(), 0, rec.getLength());
                                        hashs.add(Integer.parseInt(num));
                                    } catch (SocketTimeoutException e) {
                                        // e.printStackTrace();
                                    } catch (IOException e) {
                                        // e.printStackTrace();
                                    }
                                } catch (SocketException e) {
                                    // e.printStackTrace();
                                }
                            }
                        }

                        if (hashs.size() != number_barrels.get()) {
                            // enviamos para o server as respostas que recebemos
                            try {
                                searchModule.updateBarrels(hashs);
                                number_barrels.set(number_barrels.get() - (number_barrels.get() - hashs.size()));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (number_barrels.get() == 0) {
                        flag = false;
                        break;
                    }

                    System.out.printf("TODOS %d RECEBERAM O TAMANHO %d \n", number_barrels.get(), tamanho_envio);

                    // enviar JSOUPData com opcao 1
                    opt[0] = 1;
                    send = new byte[tamanho_envio + 1];
                    // copia para o primeiro byte a opcao,de seguida copia o buf para os restantes
                    // bytes
                    System.arraycopy(opt, 0, send, 0, 1);
                    System.arraycopy(class_send, 0, send, 1, tamanho_envio);
                    packet = new DatagramPacket(send, send.length, group, PORT);
                    try {
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    hashs = new HashSet<>();
                    for (int i = 0; i < number_barrels.get(); i++) {
                        byte buffe[] = new byte[20];
                        DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                        try {
                            socket.setSoTimeout(3000);
                            try {
                                socket.receive(rec);
                                String num = new String(rec.getData(), 0, rec.getLength());
                                hashs.add(Integer.parseInt(num));
                            } catch (SocketTimeoutException e) {
                                // e.printStackTrace();
                            } catch (IOException e) {
                                // e.printStackTrace();
                            }
                        } catch (SocketException e) {
                            // e.printStackTrace();
                        }
                    }

                    if (hashs.size() != number_barrels.get()) {
                        int barrels_faltam = number_barrels.get() - hashs.size();
                        // System.out.printf("FALTAM %d CONFIRMACOES", barrels_faltam);
                        // confirmação de barrels que faltam
                        int numero_tentativas = 1;
                        for (int i = 0; i < numero_tentativas; i++) {
                            // enviar de novo novamente
                            packet = new DatagramPacket(send, send.length, group, PORT);
                            try {
                                socket.send(packet);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // esperamos pelas respostas dos barrels que faltam
                            for (int h = 0; h < barrels_faltam; h++) {
                                byte buffe[] = new byte[20];
                                DatagramPacket rec = new DatagramPacket(buffe, buffe.length);
                                try {
                                    socket.setSoTimeout(3000);
                                    try {
                                        socket.receive(rec);
                                        String num = new String(rec.getData(), 0, rec.getLength());
                                        hashs.add(Integer.parseInt(num));
                                    } catch (SocketTimeoutException e) {
                                        // e.printStackTrace();
                                    } catch (IOException e) {
                                        // e.printStackTrace();
                                    }
                                } catch (SocketException e) {
                                    // e.printStackTrace();
                                }
                            }
                        }

                        if (hashs.size() != number_barrels.get()) {
                            // enviamos para o server as respostas que recebemos
                            try {
                                searchModule.updateBarrels(hashs);
                                number_barrels.set(number_barrels.get() - (number_barrels.get() - hashs.size()));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    if (number_barrels.get() == 0) {
                        flag = false;
                        break;
                    }

                    System.out.printf("TODOS OS %d BARRELS RECEBERAM A CLASSE\n", number_barrels.get());

                    flag = true;
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countIterations++;
        }
    }

}
