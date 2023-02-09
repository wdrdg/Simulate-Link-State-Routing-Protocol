package socs.network.node;

import socs.network.message.LinkDescription;
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
  
  volatile Boolean alreadyattach = false;
//  RequestHandler requestHandler = null;
  volatile int attachAgreement = -1;


  public Router(Configuration config) throws IOException {
    // System.out.println("begin");
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

    

    //endTODO
    if (processPort == rd.processPortNumber) {
      System.out.println("You are trying to attach to yourself! You cannot attach!");
      return;
    }
    System.out.println("Your attach request has been sent!");

    // check the ports space
    boolean is_full = true;
    boolean is_attach = false;
    for (int x=0; x<ports.length; x++){
      Link link = ports[x];
      if (link != null) {
        //check: 1.check whether connect with itself. 2.check whether the target router already attached.
        RouterDescription rd1 = link.router1;
        RouterDescription rd2 = link.router2;
        if ((rd.simulatedIPAddress.equals(rd1.simulatedIPAddress)&& simulatedIP.equals(rd2.simulatedIPAddress) ) || 
        (rd.simulatedIPAddress.equals(rd2.simulatedIPAddress) && simulatedIP.equals(rd1.simulatedIPAddress) ))
        {
          is_attach = true;
          break;
        }
        continue;
      }
      else{
        is_full = false;
        break;
      }
    }
    if (is_attach){
      System.out.println("Your have had such attach in the lists. You cannot attach!");
    }
    else if (is_full){
      System.out.println("Your ports are all occupied. You cannot attach!");
    }
    
    else{
      //pack the message
      SOSPFPacket message = new SOSPFPacket();
      message.srcProcessIP = rd.processIPAddress;
      message.srcProcessPort = rd.processPortNumber;
      message.srcIP = rd.simulatedIPAddress;
      message.dstIP = simulatedIP;
      message.attachRequest = 0;

      //send message
      sendMessage(message, processIP, processPort);
      // while(!alreadyattach){
      //   continue;
      // }
      // alreadyattach = false;
      // System.out.println("Attached");

    }
    

  }

  private void sendMessage(SOSPFPacket message, String processIP, short processPort) throws IOException {
    // System.out.println("Sending now!");
    // System.out.println(message.sospfType);
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
      //System.out.println("aaa");
      new Thread(new Runnable() {
        public void run() {
          try {
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            SOSPFPacket receviedMessage = (SOSPFPacket) in.readObject();
            if (receviedMessage != null){
              //System.out.println(receviedMessage);
              // check the ports space
              boolean is_full = true;
              for (int x=0; x<ports.length; x++){
                if (ports[x] != null) {continue;}
                else{
                  is_full = false;
                  break;
                } 
              }
              if (is_full){
                System.out.println("Your ports are all occupied. The attach request is rejected.");
              }
              else{
                analysisMessage(receviedMessage);
              }
            
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

  private void analysisMessage(SOSPFPacket message) throws IOException{
    //TODO: handle different kind of message
    String srcProcessIP = message.srcProcessIP;
    short srcProcessPort = message.srcProcessPort;
    String srcIP = message.srcIP;
    String dstIP = message.dstIP;
    short sospfType = message.sospfType;
    short attachRequest= message.attachRequest;

    // process attach
    if (attachRequest == 0) {
      // router receivie the message
      System.out.println("Received HELLO from "+srcIP+";"+"\nDo you accept this attach request? (Y/N)");
      while (attachAgreement==-1){
        continue;
      }
      int if_accpet = attachAgreement;
      attachAgreement = -1;
      
      // System.out.println("HI "+if_accpet);
      
      if (if_accpet==0){
        System.out.println("You reject this attach request;");
        message.srcProcessIP = null;
      }
      else if (if_accpet==1){ 
        // add a new link
        System.out.println("You accept the the attach request;");
        // rd.status = RouterStatus.INIT;
        RouterDescription rd2 = new RouterDescription();
        rd2.processIPAddress = srcProcessIP;
        rd2.processPortNumber = srcProcessPort;
        rd2.simulatedIPAddress = srcIP;

        Link l = new Link(rd, rd2);
        for (int x=0; x<ports.length; x++){
          if (ports[x] == null) {
            ports[x]=l;
            break;
          }
        }
        message.srcProcessIP = rd.processIPAddress;
        message.srcProcessPort = rd.processPortNumber;
        message.srcIP = dstIP;
        message.dstIP = srcIP;
      }
      // System.out.println("Hello World");
      message.attachRequest = 1;
      processIP = srcProcessIP;
      processPort = srcProcessPort;
      //send message
      sendMessage(message, processIP, processPort);
    }

    else if (attachRequest == 1) {
      if (message.srcProcessIP == null){
        System.out.println("Your attach has been rejected!");
      }
      // add a new link
      else{
        // rd.status = RouterStatus.INIT;
        RouterDescription rd2 = new RouterDescription(); 
        rd2.processIPAddress = srcProcessIP;
        rd2.processPortNumber = srcProcessPort;
        rd2.simulatedIPAddress = srcIP;
  
        Link l = new Link(rd, rd2);
        for (int x=0; x<ports.length; x++){
          if (ports[x] == null) {
            ports[x]=l;
            break;
          }
        }
        alreadyattach = true;
        System.out.println("Your attach has been accpeted!");
      }
      
    }

    // process start
    if (sospfType == 0){
      for (int x=0; x<ports.length; x++){
        Link link = ports[x];
        if (link != null) {
          RouterDescription rd1 = link.router1;
          RouterDescription rd2 = link.router2;
          message.srcProcessIP = rd.processIPAddress;
          message.srcProcessPort = rd.processPortNumber;
          message.srcIP = dstIP;
          message.dstIP = srcIP;
          if (rd.equals(rd1)){
            if (rd2.status == null ){
              System.out.println("Received HELLO from "+srcIP+";");
              rd2.status = RouterStatus.INIT;
              System.out.println("Set "+srcIP+" STATE to INIT;");
              sendMessage(message, srcProcessIP, srcProcessPort);
            }
            else if (rd2.status == RouterStatus.INIT ){
              System.out.println("Received HELLO from "+srcIP+";");
              rd2.status = RouterStatus.TWO_WAY;
              System.out.println("Set "+srcIP+" STATE to TWO_WAY;");
              sendMessage(message, srcProcessIP, srcProcessPort);
            }
          }
          else{
            if (rd1.status == null ){
              System.out.println("Received HELLO from "+srcIP+";");
              rd1.status = RouterStatus.INIT;
              System.out.println("Set "+srcIP+" STATE to INIT;");
              sendMessage(message, srcProcessIP, srcProcessPort);
            }
            else if (rd1.status == RouterStatus.INIT ){
              System.out.println("Received HELLO from "+srcIP+";");
              rd1.status = RouterStatus.TWO_WAY;
              System.out.println("Set "+srcIP+" STATE to TWO_WAY;");
              sendMessage(message, srcProcessIP, srcProcessPort);
            }
          }
          
        }
      }
    }

  }

  
  /**
   * broadcast Hello to neighbors
   */
  private void processStart() throws IOException {
    for (int x=0; x<ports.length; x++){
      boolean need_start = true;
      Link link = ports[x];
      String processIP;
      short processPort;
      if (link != null) {
        RouterDescription rd1 = link.router1;
        RouterDescription rd2 = link.router2;
        SOSPFPacket message = new SOSPFPacket();
        if (rd.equals(rd1)){
          // send hello to rd2
          if (rd2.status == RouterStatus.TWO_WAY){
            need_start = false;
            System.out.println(rd2.simulatedIPAddress + " has been set to TWO_WAY");
            continue;
          }
          rd2.status = RouterStatus.INIT;
          message.srcProcessIP = rd1.processIPAddress;
          message.srcProcessPort = rd1.processPortNumber;
          message.srcIP = rd1.simulatedIPAddress;
          message.dstIP = rd2.simulatedIPAddress;
          message.sospfType = 0;
          processIP = rd2.processIPAddress;
          processPort = rd2.processPortNumber;
          //send message
          
        }
        else{
          // send hello to rd1
          if (rd1.status == RouterStatus.TWO_WAY){
            need_start = false;
            System.out.println(rd1.simulatedIPAddress + " has been set to TWO_WAY");
            continue;
          }
          rd1.status = RouterStatus.INIT;
          message.srcProcessIP = rd2.processIPAddress;
          message.srcProcessPort = rd2.processPortNumber;
          message.srcIP = rd2.simulatedIPAddress;
          message.dstIP = rd1.simulatedIPAddress;
          message.sospfType = 0;
          processIP = rd1.processIPAddress;
          processPort = rd1.processPortNumber;
          //send message
        }
      if (need_start){
        sendMessage(message, processIP, processPort);
      }

      }   
    }
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
        }else if (command.equalsIgnoreCase("Y")) {
          //output neighbors
          attachAgreement=1;
        }else if (command.equalsIgnoreCase("N")) {
          //output neighbors
          attachAgreement=0;
        }
         else {
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


}

