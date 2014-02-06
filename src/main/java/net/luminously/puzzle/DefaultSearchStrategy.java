package net.luminously.puzzle;

import java.io.Writer;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;

import net.luminously.puzzle.Board.Direction;
/**
 * Naive implementation of N-Puzzle search strategy. Uses breadth first
 * search to find solution.
 */
public class DefaultSearchStrategy extends AbstractSearchStrategy {
  private static final EnumSet<Direction> DIRECTIONS = EnumSet.allOf(Direction.class);
  
  public DefaultSearchStrategy() {}

  @Override
  public void search(Board board, Writer out) {
    Queue<Board> queue = new LinkedList<Board>();
    queue.add(board);
    
    // Breadth-first Search
    while (!queue.isEmpty()) {
      Board top = queue.remove();
      
      if (top.isComplete()) {
        write(out, top.getMoves().toString());
        break;
      }
      
      for (Direction d: DIRECTIONS) {
        if (d != top.getLastPosition() && top.canMove(d)) {
          Board newBoard = top.move(d);
          queue.add(newBoard);
        }
      }
    }
  }

}
