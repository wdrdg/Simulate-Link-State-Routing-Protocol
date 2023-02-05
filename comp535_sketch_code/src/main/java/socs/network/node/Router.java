package socs.network.node;

import socs.network.message.SOSPFPacket;
import socs.network.util.Configuration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Router {

  protected LinkStateDatabase lsd;

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];
  short processPort = 30000;
  ServerSocket serverSocket = null;

  Socket server = null;
  String processIP = "localhost";
//  RequestHandler requestHandler = null;


  public Router(Configuration config) throws IOException {
    System.out.println("begin");
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    initPort();
    rd.processPortNumber = processPort;
    rd.processIPAddress = processIP;
    lsd = new LinkStateDatabase(rd);
    System.out.println(serverSocket);


    new Thread(new Runnable() {
      public void run() {
        try {
          requestHandler();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
//    requestHandler = new RequestHandler(serverSocket);
//    RequestHandler.start();


  }

  public void initPort(){
    while(serverSocket==null){
      try{
        serverSocket = new ServerSocket(processPort);
      } catch (IOException e) {
        processPort ++;
      }
    }
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) throws IOException {

    //TODO: 1.check whether connect with itself. 2.check whether the target router already attached.

    //endTODO
    System.out.println("Your attach request has been");
    //pack the message
    SOSPFPacket message = new SOSPFPacket();
    message.srcProcessIP = processIP;
    message.srcProcessPort = processPort;
    message.srcIP = rd.simulatedIPAddress;
    message.dstIP = simulatedIP;
    message.sospfType = 0;

    //send message
    sendMessage(message, processIP, processPort);






  }

  private void sendMessage(SOSPFPacket message, String processIP, short processPort) throws IOException {
    Socket client = new Socket(processIP, processPort);
    ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
    os.writeObject(message);

  }


  /**
   * process request from the remote router. 
   * For example: when router2 tries to attach router1. Router1 can decide whether it will accept this request. 
   * The intuition is that if router2 is an unknown/anomaly router, it is always safe to reject the attached request from router2.
   */
  private void requestHandler() throws IOException {

    while(true){
      server = serverSocket.accept();
      System.out.println("aaa");
      new Thread(new Runnable() {
        public void run() {
          try {
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            SOSPFPacket receviedMessage = (SOSPFPacket) in.readObject();
            if (receviedMessage != null){
              System.out.println(receviedMessage);
              analysisMessage(receviedMessage);
            }else{
              return;
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
          }
        }
      }).start();
    }

  }

  private void analysisMessage(SOSPFPacket message){

    //TODO: handle different kind of message

  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {

  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }

  /**
   * update the weight of an attached link
   */
  private void updateWeight(String processIP, short processPort,
                             String simulatedIP, short weight){

  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

//  static class RequestHandler extends Thread{
//    private Socket server = null;
//    RequestHandler(ServerSocket serverSocket) throws IOException {
//      server = serverSocket.accept();
//    }
//
//    public void run() {
//      try{
//        while (true) {
//          RequestListener requestListener = new RequestListener(server);
//          requestListener.start();
//        }
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    }
//  }
//
//  static class RequestListener extends Thread{
//    Socket Server = null;
//
//    RequestListener(Socket server){
//      Server = server;
//    }
//
//    public void run() {
//      try {
//        ObjectInputStream in = new ObjectInputStream(Server.getInputStream());
//        SOSPFPacket message = (SOSPFPacket) in.readObject();
//        if (message != null){
//          return;
//        }else{
//          //TODO: handle the message.
//        }
//      } catch (IOException e) {
//        throw new RuntimeException(e);
//      } catch (ClassNotFoundException e) {
//        throw new RuntimeException(e);
//      }
//    }
//  }

}

