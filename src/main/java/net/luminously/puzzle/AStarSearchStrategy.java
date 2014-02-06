package net.luminously.puzzle;

import java.io.Writer;
import java.util.EnumSet;
import java.util.PriorityQueue;
import java.util.Set;

import net.luminously.puzzle.Board.Direction;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
/**
 * Naive implementation of N-Puzzle search strategy. Uses breadth first
 * search to find solution.
 */
public class AStarSearchStrategy extends AbstractSearchStrategy {
  private static final EnumSet<Direction> DIRECTIONS = EnumSet.allOf(Direction.class);
  
  private PriorityQueue<Board> open;
  private Set<Board> closed;
  
  public AStarSearchStrategy() {
    open = Queues.newPriorityQueue();
    closed = Sets.newHashSet();
  }

  @Override
  public void search(Board board, Writer out) {
    open = Queues.newPriorityQueue();
    open.add(board);
    
    while (!open.isEmpty()) {
      Board top = open.remove();
      
      if (top.isComplete()) {
        write(out, top.getMoves().toString());
        return;
      }
      
      closed.add(top);
      
      for (Direction d: DIRECTIONS) {
        Board successor;
        if (d == top.getLastPosition()
            || !top.canMove(d)
            || closed.contains(successor = top.move(d))
            ) continue;
        
        if (closed.contains(successor)) continue;
        
        if (open.contains(successor)) {
          // Hack to remove longer path when 2 of same state in open
          // Because two boards of the same configuration are "equal()",
          // Board.compareTo will cause the longer path-ed Board to float
          // above and get removed by remove()
          open.add(successor);
          open.remove(successor);
        } else {
          open.add(successor);
        }
        
      }
    }
  }

}
