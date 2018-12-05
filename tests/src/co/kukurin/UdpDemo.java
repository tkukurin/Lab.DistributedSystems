package co.kukurin;

import co.kukurin.common.Run;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Basic demo of UDP send/receive.
 */
public class UdpDemo {
  private static final int PORT = 9191;
  private static final int MAX = 5;

  public static void main(String[] args) throws IOException {
    InetAddress address = InetAddress.getLocalHost();
    System.out.printf("Listening on\n%s:%s\n\n", address.getHostAddress(), PORT);

    byte[] buffer = new byte[1024];
    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

    byte[] message = "hello".getBytes();
    DatagramPacket sendPacket = new DatagramPacket(message, message.length, address, PORT);

    Thread serverThread = new Thread(new Run(() -> {
      System.out.println("Started server.");
      try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
        for (int i = 0; i < MAX; i++) {
          serverSocket.receive(receivePacket);
          System.out.println("Received packet.");
          System.out.println(new String(receivePacket.getData(), 0, receivePacket.getLength()));
        }
      }
    }));
    serverThread.start();

    Thread clientThread = new Thread(new Run(() -> {
      System.out.println("Started client.");
      try (DatagramSocket clientSocket = new DatagramSocket()) {
        for (int i = 0; i < MAX; i++) {
          clientSocket.send(sendPacket);
          System.out.println("Sent packet.");
        }
      }
    }));
    clientThread.start();
  }

}
