package net.luminously.puzzle;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.luminously.puzzle.Board.Direction;

import com.google.common.base.Throwables;
import com.google.common.collect.Queues;

/**
 * Attempts to solve the N-Puzzle game using a somewhat parallelized A* search.
 */
public class ParallelAStarSearchStrategy implements SearchStrategy {
  private static final EnumSet<Direction> DIRECTIONS = EnumSet.allOf(Direction.class);
  private static final int threads = Runtime.getRuntime().availableProcessors() * 2;

  private final long startTime;
  private Queue<Board> open;
  
  public ParallelAStarSearchStrategy() {
    this.startTime = System.currentTimeMillis();
    open = Queues.newPriorityBlockingQueue();
  }
  
  @Override
  public void search(Board board, Writer out) {
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    
    open.add(board);
    while (true) {
      evalBoard(out, executor);
    }
  }
  
  /**
   * Checks the queue for new Boards. Delegates intensive work to worker threads.
   * @param executor
   */
  private void evalBoard(Writer out, ExecutorService executor) {
    if (open.isEmpty()) return;
    
    Board top = open.remove();
    
    if (top.isComplete()) {
      int elapsed = (int)((System.currentTimeMillis() - startTime) / 1000);
      String time = "\n" + elapsed + " seconds\n";
      
      // Write solution to file
      try {
        out.write(top.getMoves().append(time).toString());
        out.flush();
      } catch (IOException e) {
        // Un-R/W-able file is a RuntimeException
        Throwables.propagate(e);
      }
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
    
    public BoardSearchWorker(Board board, Direction direction, Queue<Board> blockingQueue) {
      this.board = board;
      this.direction = direction;
      this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
      if (direction == board.getLastPosition() || !board.canMove(direction)) return;
      blockingQueue.add(board.move(direction));
    }
    
  }
}
