package net.luminously.Puzzle;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.base.Preconditions;

/**
 * Class representing an n-sized board of a fifteen puzzle. Board stores the
 * following information: Board state, current position, moves taken.
 */
public class Board {
  public static enum Direction {
    NORTH, EAST, SOUTH, WEST
  };
  
  private int[][] board;
  private StringBuilder moves;
  private Point empty;
  private Direction lastPosition;

  /**
   * Creates an instance of the Board object. Can only be instantiated
   * through Board.parse.
   * 
   * @param board An NxN int array containing a single -1 index representing
   *              the empty square.
   * @param position A Point object representing the position of the empty
   *                 square in the board.
   * @param lastPosition Direction of last position
   */
  private Board(int[][] board, Point position, StringBuilder moves, Direction lastPosition) {
    Preconditions.checkNotNull(board, "board must not be null");
    Preconditions.checkNotNull(position, "position must not be null");
    Preconditions.checkNotNull(moves, "moves must not be null");
    
    this.board = board;
    this.empty = position;
    this.lastPosition = lastPosition;
    this.moves = moves;
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
   * Returns the last move made.
   * @return Direction enum representing last move
   */
  public Direction getLastPosition() {
    return lastPosition;
  }

  /**
   * Returns a copy of the list of previous moves.
   * @return Copy of StringBuilder object
   */
  public StringBuilder getMoves() {
    return new StringBuilder(this.moves);
  }
  
  /**
   * Returns a copy of the Point representing the position of the empty square.
   * @return Point object with coordinates of the empty square
   */
  public Point getPoint() {
	  return new Point(empty);
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
	  
	  return new Board(newBoard, newEmpty, getMoves().append(directionToChar(d)), oppositeDirection(d));
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
    
    return new Board(newBoard, empty, new StringBuilder(), null);
  }
}
