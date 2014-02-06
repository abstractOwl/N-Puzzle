package net.luminously.puzzle;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.google.common.base.Preconditions;

/**
 * Class representing an n-sized board of a fifteen puzzle. Board stores the
 * following information: Board state, current position, moves taken.
 */
public class Board implements Comparable<Board> {
  public static enum Direction {
    NORTH, EAST, SOUTH, WEST
  };
  
  private final int[][] board;
  private final int estimatedCost;
  private Board prevBoard;
  private final Point empty;
  private final Direction lastPosition;

  /**
   * Creates an instance of the Board object. Can only be instantiated
   * through Board.parse.
   * 
   * @param board An NxN int array containing a single -1 index representing
   *              the empty square.
   * @param position A Point object representing the position of the empty
   *                 square in the board.
   * @param prevBoard Board which lead to this Board
   * @param lastPosition Direction of last position
   */
  private Board(int[][] board, Point position, Board prevBoard, Direction lastPosition) {
    Preconditions.checkNotNull(board, "board must not be null");
    Preconditions.checkNotNull(position, "position must not be null");
    
    this.board = board;
    this.empty = position;
    this.lastPosition = lastPosition;
    this.prevBoard = prevBoard;
    
    // Cache for comparison: This value is read at least once per instance, therefore lazy load is not optimal
    // Consider making this optional in the future if other algorithms do not use
    estimatedCost = calculateHeuristic();
  }
  
  /**
   * Estimates the cost to finish along this path with the Manhattan Distance.
   * @return Estimated Integer cost
   */
  private int calculateHeuristic() {
    int size = board.length;
    int estimate = 0;
    
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        Point p = calculateHeuristicHelper(board[x][y]);
        estimate += Math.abs(x - p.x) + Math.abs(y - p.y);
      }
    }
    return getPathCost() + estimate;
  }
  
  /**
   * Returns the expected coordinates of a given value
   * @return Point object with the expected coordinates
   */
  private Point calculateHeuristicHelper(int n) {
    int size = board.length;
    if (n == -1) return new Point(size - 1, size - 1); // special case for X
    return new Point(n % size, n / size);
  }
  
  /**
   * Returns the position of the empty square resulting from moving one unit in
   * the specified direction. Does not bound-check, use `canMove()` instead.
   * @param d Direction to move
   * @return New Point object
   */
  private Point getNewPosition(Direction d) {
    Preconditions.checkNotNull(d, "direction must not be null");
    
    Point newPoint = new Point(empty);
    
    int displacement = (d == Direction.EAST  || d == Direction.SOUTH) ? 1 : -1;
    
    if (d == Direction.NORTH || d == Direction.SOUTH) {
      newPoint.move(newPoint.x, newPoint.y + displacement);
    } else {
      newPoint.move(newPoint.x + displacement, newPoint.y);
    }
    
    return newPoint;
  }
  
  /**
   * Returns true if moving in the specified direction is a valid operation.
   * @param d Direction to move
   * @return A new Board with the result of the operation.
   */
  public boolean canMove(Direction d) {
    Preconditions.checkNotNull(d, "direction must not be null");
    
    // Calculate displacement and direction
    int displacement = (d == Direction.EAST  || d == Direction.SOUTH) ? 1       : -1;
    int position     = (d == Direction.NORTH || d == Direction.SOUTH) ? empty.y : empty.x;
    
    // Calculate position
    position += displacement;
    // Check bounds
    return (displacement < 0) ? position >= 0 : position < board.length;
  }
  
  /**
   * Converts Direction enum to char for output.
   * @return char representation of enum
   */
  private char directionToChar(Direction d) {
    char out = 'x';
    switch (d) {
    case NORTH: out = 'u'; break;
    case SOUTH: out = 'd'; break;
    case EAST:  out = 'r'; break;
    case WEST:  out = 'l'; break;
    }
    return out;
  }
  
  /**
   * Returns the opposite of specified Direction 
   * @param d Some Direction
   * @return Opposite of d
   */
  private Direction oppositeDirection(Direction d) {
    switch (d) {
    case NORTH: return Direction.SOUTH;
    case SOUTH: return Direction.NORTH;
    case EAST:  return Direction.WEST;
    case WEST:  return Direction.EAST;
    default: throw new NullPointerException("d must not be null");
    }
  }
  
  @Override
  public int hashCode() {
    return board.hashCode();
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (other == this) return true;
    if (!(other instanceof Board)) return false;
    
    return Arrays.deepEquals(board, ((Board)other).board);
  }

  @Override
  public int compareTo(Board other) {
    // See Comparable#compareTo spec for more information
    int cost = getTotalCost() - other.getTotalCost();
    return ((cost == 0)
        // If equal, make the longer path float up so we can remove 
        ? other.getPathCost() - getPathCost()
        : getTotalCost() - other.getTotalCost());
  }
  
  /**
   * Returns a copy of the current Board state.
   * 
   * @return 3-D int array representing board state
   */
  public int[][] getBoard() {
	  int size = board.length;
	  int[][] tmp = new int[size][size];
	  for (int i = 0; i < size; i++) {
		  // Copy to prevent mutability
		  System.arraycopy(board[i], 0, tmp[i], 0, size);
	  }
	  return tmp;
  }
  
  /**
   * Returns the estimated cost from this state to the finish.
   * @return estimated Integer cost
   */
  public int getEstimatedCost() {
    return estimatedCost;
  }
  
  /**
   * Returns the last move made.
   * @return Direction enum representing last move
   */
  public Direction getLastPosition() {
    return lastPosition;
  }

  /**
   * Recursively build move path.
   * @return String
   */
  public StringBuilder getMoves() {
    if (prevBoard == null) { return new StringBuilder(); } // Base case
    return prevBoard.getMoves().append(directionToChar(oppositeDirection(getLastPosition())));
  }
  
  /**
   * Recursively calculates cost up to this point.
   * @return Integer cost
   */
  public int getPathCost() {
    if (prevBoard == null) return 0;
    return prevBoard.getPathCost() + 1;
  }
  
  /**
   * Returns a copy of the Point representing the position of the empty square.
   * @return Point object with coordinates of the empty square
   */
  public Point getPoint() {
	  return new Point(empty);
  }
  
  /**
   * Returns the total cost of this path. Calculated by adding path cost and estimated cost.
   * @return Integer cost
   */
  public int getTotalCost() {
    return getPathCost() + getEstimatedCost();
  }
  
  /**
   * Returns true if board is in finished state.
   * @return boolean determined by completeness of puzzle
   */
  public boolean isComplete() {
    int size = board.length;
    
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        if (x == size - 1 && y == size - 1 && board[x][y] == -1) return true;
        if (board[x][y] != y * size + x + 1) return false; // Bail early
      }
    }
    return false;
  }
  
  /**
   * Returns a copy of the Board representing a move in the specified direction
   * @param d Direction of the move
   * @return A new Board
   */
  public Board move(Direction d) {
    Preconditions.checkArgument(canMove(d), "Cannot move in specified direction");
	  
	  Point newEmpty = getNewPosition(d);
	  
	  int[][] newBoard = getBoard();
	  int tmp = newBoard[empty.x][empty.y];
	  newBoard[empty.x][empty.y] = newBoard[newEmpty.x][newEmpty.y];
	  newBoard[newEmpty.x][newEmpty.y] = tmp;
	  
	  return new Board(newBoard, newEmpty, this, oppositeDirection(d));
  }
  
  /**
   * Returns a copy of this Board with a different preceding Board.
   * @return Specified preceding Board
   */
  public void setPredecessor(Board preceding) {
    prevBoard = preceding;
  }
  
  /**
   * Given a filename, returns an instance of the Board object. The
   * input file must conform to the following characteristics:
   * 
   * <ul>
   * 	<li>Rows must be delimited by a newline sequence</li>
   * 	<li>Columns must be delimited by whitespace character(s)</li>
   * 	<li>Rows and columns must be of the same size</li>
   * 	<li>File must not contain leading newlines</li>
   * 	<li>File may contain trailing newlines</li>
   * </ul>
   * 
   * @param filename
   * @return
   * @throws IOException
   */
  public static Board parse(String filename) throws IOException {
    Preconditions.checkNotNull(filename, "filename cannot be null");
    
    String line;
    StringBuilder boardBuilder = new StringBuilder();
    
    // Read input file
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    while ((line = reader.readLine()) != null) {
      boardBuilder.append(line);
      boardBuilder.append('\n');
    }
    reader.close();
    
    // Convert to board representation
    String[] rows = boardBuilder.toString().split("\n");
    int size = rows.length;
    
    int[][] newBoard = new int[size][size];
    Point empty = null;
    
    for (int i = 0; i < size; i++) {
      String[] cols = rows[i].split(" ");
      
      Preconditions.checkArgument(cols.length == size, "cols and rows must be of the same size");
      
      for (int j = 0; j < size; j++) {
        if (cols[j].equalsIgnoreCase("x")) {
          // Found empty square
          Preconditions.checkArgument(empty == null, "Found multiple start positions");
          
          empty = new Point(j, i); // Switch params because array transposed later
          newBoard[i][j] = -1;
        } else {
          newBoard[i][j] = Integer.parseInt(cols[j], 10);
        }
      }
    }
    
    // Transpose array to make it more intuitive
    for (int i = 1; i < size; i++) {
      for (int j = 0; j < i; j++) {
        int tmp = newBoard[i][j];
        newBoard[i][j] = newBoard[j][i];
        newBoard[j][i] = tmp;
      }
    }
    
    Preconditions.checkNotNull(empty, "Input must contain one empty square");
    
    return new Board(newBoard, empty, null, null);
  }
}
