package socs.network.node;

import java.util.HashMap;
import java.util.LinkedList;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

public class LinkStateDatabase {

  //linkID => LSAInstance
  HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  public synchronized boolean updateLSA(LSA lsa, String simulatedIP){
    if (_store.get(simulatedIP)==null){
      _store.put(simulatedIP, lsa);
      return true;
    }else {
      LSA currentLsa = _store.get(simulatedIP);
      if (currentLsa.lsaSeqNumber < lsa.lsaSeqNumber) {
        _store.put(simulatedIP, lsa);
        return true;
      }
    }
    return false;
  }

  Boolean exist(LinkedList<LinkDescription> l, String ip){
    for (LinkDescription ld: l){
      if (ld.linkID == ip){
        return true;
      }
    }
    return false;
  }

  Integer getDistance(LinkedList<LinkDescription> l, String ip){
    for (LinkDescription ld: l){
      if (ld.linkID == ip){
        return ld.tosMetrics;
      }
    }
    return null;
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  String getShortestPath(String destinationIP) {
    //TODO: fill the implementation here
    HashMap<String, Integer> distance = new HashMap<String, Integer>();
    HashMap<String, Boolean> shortest = new HashMap<String, Boolean>();
    HashMap<String, String> parent = new HashMap<String, String>();
    int size = _store.size();
    String min_ip = null;
    parent.put(rd.simulatedIPAddress,null);

    for (String ip: _store.keySet()){
      distance.put(ip, Integer.MAX_VALUE);
      shortest.put(ip, false);
    }
    for (LinkDescription ld: _store.get(rd.simulatedIPAddress).links){
      distance.put(ld.linkID, ld.tosMetrics);
      parent.put(ld.linkID, rd.simulatedIPAddress);
    }

    for (int i=0; i<size-1; i++){
      // get the index to explore
      Integer min = Integer.MAX_VALUE;
      for (String ip2: distance.keySet()){
        if (!shortest.get(ip2) && distance.get(ip2)<min){
          min = distance.get(ip2);
          min_ip = ip2;
        }
      }
      shortest.put(min_ip, true);

      // update the shortest distance
      for (String ip2: distance.keySet()){
        if (!shortest.get(ip2) && exist(_store.get(min_ip).links, ip2) &&
        distance.get(min_ip) != Integer.MAX_VALUE &&
        distance.get(min_ip) + getDistance(_store.get(min_ip).links, ip2) < distance.get(ip2) ){

          distance.put(ip2,  distance.get(min_ip) + getDistance(_store.get(min_ip).links, ip2));
          parent.put(ip2, min_ip);

        }
      }
      
    }
    String p = parent.get(destinationIP);
    String path = parent.get(destinationIP) + "->(" + Integer.toString(getDistance(_store.get(p).links, destinationIP)) + ") " + destinationIP;
   
    while (parent.get(p)!=null){
      path = parent.get(p) + "->(" + Integer.toString(getDistance(_store.get(parent.get(p)).links, p)) + ") " + path;
      p = parent.get(p);
    }

    return path;
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa: _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                append(ld.tosMetrics).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
