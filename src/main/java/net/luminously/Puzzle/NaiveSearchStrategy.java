package net.luminously.Puzzle;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;

import com.google.common.base.Throwables;

import net.luminously.Puzzle.Board.Direction;
/**
 * Naive implementation of N-Puzzle search strategy. Uses breadth first
 * search to find solution.
 */
public class NaiveSearchStrategy implements SearchStrategy {
  private static final EnumSet<Direction> DIRECTIONS = EnumSet.allOf(Direction.class);
  private long startTime;
  
  public NaiveSearchStrategy() {
    this.startTime = System.currentTimeMillis();
  }

  @Override
  public void search(Board board, Writer out) {
    boolean finished = false;
    Queue<Board> queue = new LinkedList<Board>();
    queue.add(board);
    
    // If initially complete
    if (board.isComplete()) {
      try {
        out.write("\n0 seconds\n");
      } catch (IOException e) {
        // Un-R/W-able file is a RuntimeException
        Throwables.propagate(e);
      }
    }
    
    // Breadth-first Search
    while (!finished) {
      Board top = queue.remove();
      
      for (Direction d: DIRECTIONS) {
        if (d != top.getLastPosition() && top.canMove(d)) {
          Board newBoard = top.move(d);
          if (newBoard.isComplete()) {
            int elapsed = (int)((System.currentTimeMillis() - startTime) / 1000);
            String time = "\n" + elapsed + " seconds\n";
            
            // Write solution to file
            try {
              out.write(newBoard.getMoves().append(time).toString());
            } catch (IOException e) {
              // Un-R/W-able file is a RuntimeException
              Throwables.propagate(e);
            }
          }
          queue.add(newBoard);
        }
      }
    }
  }

}
