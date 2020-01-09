package dennisTestPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
  private Node[][] nodes;

  public Graph() {
	  nodes = new Node[11][11];
	  for(int i = 0; i< 11; i++) {
		  for(int j = 0; j < 11; j++)
			  nodes[i][j] = new Node(i, j);
	  }
  }
  
  public void changeAvailability(int x, int y, boolean availability) {
	  	nodes[x][y].updateAvailability(availability);
  }
  public void updateNode(int x, int y, int elevation, int soup) {
    nodes[x][y].setStats(soup, elevation);
  }
  public void updateNeighbors() {
	  for(int i = 0; i < 11; i++) {
		  for(int j = 0; j < 11; j++)
			  nodes[i][j].resetNeighbors();
	  }
	  for(int i = 0; i< 10; i++) {
		  for(int j = 0; j< 10; j++) {
			  if(Math.abs(nodes[i][j].getElevation() - nodes[i][j+1].getElevation()) <= 3) {
				  nodes[i][j].addNeighbor(nodes[i][j+1]);
				  nodes[i][j+1].addNeighbor(nodes[i][j]);
			  }
			  if(Math.abs(nodes[i][j].getElevation() - nodes[i+1][j+1].getElevation()) <= 3) {
				  nodes[i][j].addNeighbor(nodes[i+1][j+1]);
				  nodes[i+1][j+1].addNeighbor(nodes[i][j]);
			  }
			  if(Math.abs(nodes[i][j].getElevation() - nodes[i+1][j].getElevation()) <= 3) {
				  nodes[i][j].addNeighbor(nodes[i+1][j]);
				  nodes[i+1][j].addNeighbor(nodes[i][j]);
			  }
		  }
		  if(Math.abs(nodes[i][11].getElevation() - nodes[i+1][11].getElevation()) <= 3) {
			  nodes[i][11].addNeighbor(nodes[i+1][11]);
			  nodes[i+1][11].addNeighbor(nodes[i][11]);
		  }
		  if(Math.abs(nodes[11][i].getElevation() - nodes[11][i].getElevation()) <= 3) {
			  nodes[11][i].addNeighbor(nodes[11][i+1]);
			  nodes[11][i+1].addNeighbor(nodes[11][i]);
		  }
	  }
  }
  //will be 1-8 for directions, and 0 for mine
  public int nextStep() {
	  return -1;
	  //TODO: finish BFS
    /**Map<String, String> parents = new HashMap<String, String>();
    List<Node> temp = new ArrayList<Node>();

    Node start = nodes[5][5];
    temp.add(start);
    parents.put(startNodeName, null);

    while (temp.size() > 0) {
      Node currentNode = temp.get(0);
      List<Node> neighbors = currentNode.getNeighbors();

      for (int i = 0; i < neighbors.size(); i++) {
        Node neighbor = neighbors.get(i);
        String nodeName = neighbor.getName();

        // a node can only be visited once if it has more than one parents
        boolean visited = parents.containsKey(nodeName);
        if (visited) {
          continue;
        } else {
          temp.add(neighbor);

          // parents map can be used to get the path
          parents.put(nodeName, currentNode.getName());

          // return the shortest path if end node is reached
          if (nodeName.equals(endNodeName)) {
            System.out.println(parents);
            return getPath(parents, endNodeName);
          }
        }
      }

      temp.remove(0);
    }

    return null;*/
  }
}

class Node {
  int x;
  int y;
  int shortest;
  int elevation;
  int soup;
  boolean available;
  List<Node> neighbors;
  
  public Node(int xStart, int yStart) {
    x = xStart;
    y = yStart;
    elevation = 0;
    shortest = Integer.MAX_VALUE;
    available = true;
    neighbors = new ArrayList<Node>();
  }
  public void resetNeighbors() {
	  this.neighbors = new ArrayList<Node>();
  }
  public void updateShortest(int newShort) {
	  this.shortest = newShort;
  }
  public int getSoup() {
	  return this.soup;
  }
  public void updateAvailability(boolean availability) {
	  this.available = availability;
  }

  public int getX() {
    return this.x;
  }
  public int getY() {
	    return this.y;
  }
  public int getElevation() {
	  return this.elevation;
  }
  public void setStats(int newSoup, int newElevation) {
	  soup = newSoup;
	  elevation = newElevation;
  }
  public void addNeighbor(Node neighbor) {
    neighbors.add(neighbor);
  }

  public List<Node> getNeighbors() {
    return neighbors;
  }
}