package phillipTestPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
  private Node[][] nodes;
  private Node[][] parents;
  public Graph() {
	  nodes = new Node[11][11];
	  parents = new Node[11][11];
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
			  if(nodes[i][j].isAvailable()) {
				  if(Math.abs(nodes[i][j].getElevation() - nodes[i][j+1].getElevation()) <= 3 
						  && nodes[i][j+1].isAvailable()) {
					  nodes[i][j].addNeighbor(nodes[i][j+1]);
					  nodes[i][j+1].addNeighbor(nodes[i][j]);
				  }
				  if(Math.abs(nodes[i][j].getElevation() - nodes[i+1][j+1].getElevation()) <= 3
						  && nodes[i+1][j+1].isAvailable()) {
					  nodes[i][j].addNeighbor(nodes[i+1][j+1]);
					  nodes[i+1][j+1].addNeighbor(nodes[i][j]);
				  }
				  if(Math.abs(nodes[i][j].getElevation() - nodes[i+1][j].getElevation()) <= 3
						  && nodes[i+1][j].isAvailable()) {
					  nodes[i][j].addNeighbor(nodes[i+1][j]);
					  nodes[i+1][j].addNeighbor(nodes[i][j]);
				  }
			  }
		  }
		  if(nodes[i][10].isAvailable() && nodes[i+1][10].isAvailable()) {
			  if(Math.abs(nodes[i][10].getElevation() - nodes[i+1][10].getElevation()) <= 3) {
				  nodes[i][10].addNeighbor(nodes[i+1][10]);
				  nodes[i+1][10].addNeighbor(nodes[i][10]);
			  }
		  }
		  if(nodes[10][i].isAvailable() && nodes[10][i+1].isAvailable()) {
			  if(Math.abs(nodes[10][i].getElevation() - nodes[10][i].getElevation()) <= 3) {
				  nodes[10][i].addNeighbor(nodes[10][i+1]);
				  nodes[10][i+1].addNeighbor(nodes[10][i]);
			  }
		  }
	  }
  }
  public void initiateBFS() {
	 boolean[][] visitedNodes = new boolean[11][11];
	parents = new Node[11][11];
    List<Node> temp = new ArrayList<Node>();
    visitedNodes[5][5] = true;
    Node start = nodes[5][5];
    temp.add(start);
    parents[5][5] = null;
    start.updateShortest(0);

    while (temp.size() > 0) {
      Node currentNode = temp.get(0);
      List<Node> neighbors = currentNode.getNeighbors();

      for (int i = 0; i < neighbors.size(); i++) {
        Node neighbor = neighbors.get(i);
        int neighborX = neighbor.getX();
        int neighborY = neighbor.getY();

        // a node can only be visited once if it has more than one parents
        boolean visited = visitedNodes[neighborX][neighborY];
        if (visited) {
          continue;
        } else {
          temp.add(neighbor);
          visitedNodes[neighborX][neighborY] = true;
          // parents map can be used to get the path
          parents[neighborX][neighborY] = currentNode;
        }
      }

      temp.remove(0);
    }
    return;
  }
  public int getNextStepFlag() {
	  //TODO: finish flag method
	  return -1;
  }
  public int getNextStepGreedy() {
	  int maxX = 0;
	  int maxY = 0;
	  for(int i = 0; i< 11; i++) {
		  for(int j = 0; j<11; j++) {
			  if(nodes[maxX][maxY].getOptimum() < nodes[i][j].getOptimum()) {
				  maxX = i;
				  maxY = j;
			  }
		  }
	  }
	  Node currentNode = nodes[maxX][maxY];
	  if(maxX == 5 && maxY ==5) {
		  return 18;
	  }
	  if(currentNode.getOptimum() == -1)
		  return 19;
	  if(parents[currentNode.getX()][currentNode.getY()].getX() == 5 && parents[currentNode.getX()][currentNode.getY()].getY() == 5)
		  return (currentNode.getX()*3+currentNode.getY()-16);
	  while(parents[currentNode.getX()][currentNode.getY()].getX() != 5 || parents[currentNode.getX()][currentNode.getY()].getY() != 5) {
		  currentNode = parents[currentNode.getX()][currentNode.getY()];
	  }
	  return (currentNode.getX()*3+currentNode.getY()-6);
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
  public double getOptimum() {
	  if(this.shortest == Integer.MAX_VALUE || !this.available)
		  return -1;
	  if(this.shortest == 0) {
		  return this.soup/2.0;
	  }
	  return this.soup*1.0/(this.shortest);
  }
  public int getSoup() {
	  return this.soup;
  }
  public void updateAvailability(boolean availability) {
	  this.available = availability;
  }
  public boolean isAvailable() {
	  return available;
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
	  this.soup = newSoup;
	  this.elevation = newElevation;
	  this.available = true;
	  this.shortest = Integer.MAX_VALUE;
  }
  public void addNeighbor(Node neighbor) {
    neighbors.add(neighbor);
  }

  public List<Node> getNeighbors() {
    return neighbors;
  }
}