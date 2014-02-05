package net.luminously.Puzzle;

import java.io.Writer;
import java.util.EnumSet;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.luminously.Puzzle.Board.Direction;

import com.google.common.collect.Queues;

/**
 * Attempts to solve the N-Puzzle game using a somewhat parallelized A* search.
 */
public class AStarSearchStrategy implements SearchStrategy {
  private static final EnumSet<Direction> DIRECTIONS = EnumSet.allOf(Direction.class);
  private long startTime;
  private Queue<Board> open;
  //private Set<Board> closed;
  
  public AStarSearchStrategy() {
    this.startTime = System.currentTimeMillis();
    open = Queues.newPriorityBlockingQueue();
    //closed = Sets.newHashSet();
  }
  
  @Override
  public void search(Board board, Writer out) {
    ExecutorService executor = Executors.newFixedThreadPool(6);
    
    open.add(board);
    while (true) {
      evalBoard(executor);
    }
  }
  
  /**
   * Checks the queue for new Boards. Delegates intensive work to worker threads.
   * @param executor
   */
  private void evalBoard(ExecutorService executor) {
    if (open.isEmpty()) return;
    
    Board top = open.remove();
    
    if (top.isComplete()) {
      int elapsed = (int)((System.currentTimeMillis() - startTime) / 1000);
      String time = "\n" + elapsed + " seconds\n";
      
      // Write solution to file
      // try {
      System.out.println(top.getMoves().append(time).toString());
      // out.write(top.getMoves().append(time).toString());
      // } catch (IOException e) {
      // Un-R/W-able file is a RuntimeException
      // Throwables.propagate(e);
      // }
    }
    
    for (Direction d: DIRECTIONS) {
      executor.submit(new BoardSearchWorker(top, d, open));
    }
  }
  
  /**
   * A worker thread which performs intensive Board operations.
   */
  class BoardSearchWorker implements Runnable {
    private Board board;
    private Direction direction;
    private Queue<Board> blockingQueue;
    
    public BoardSearchWorker(Board board, Direction d, Queue<Board> q) {
      this.board = board;
      this.direction = d;
      this.blockingQueue = q;
    }

    @Override
    public void run() {
      if (direction == board.getLastPosition() || !board.canMove(direction)) return;
      blockingQueue.add(board.move(direction));
    }
    
  }
}
