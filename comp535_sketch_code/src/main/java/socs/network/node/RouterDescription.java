package socs.network.node;

public class RouterDescription {
  //used to socket communication
  String processIPAddress; // own process ip
  short processPortNumber; // own port number
  //used to identify the router in the simulated network space
  String simulatedIPAddress;
  //status of the router
  RouterStatus status;

  @Override
    public boolean equals(Object o) {
 
        // If the object is compared with itself then return true 
        RouterDescription rd = (RouterDescription) o;
        if (rd.processIPAddress.equals(this.processIPAddress) && rd.processIPAddress.equals(this.processIPAddress) && rd.processPortNumber == this.processPortNumber) {
            return true;
        }
        else{
            return false;
        }
      }
}
