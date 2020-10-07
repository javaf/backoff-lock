import java.util.concurrent.atomic.*;

// Exponential Backoff Lock uses an atomic value for
// indicating that some thread has engaged the lock
// and is executing its critical section (CS).
// 
// Each thread that wants to enter CS waits until
// the atomic lock is disengaged. Then it tries
// to reengage it, but as several threads might
// be trying the same, it may or may not succeed.
// So it checks if it succeeded (with the same
// atomic operation), and if so it done. If not
// it backsoff for a random interval which is
// doubled every retry (hence exponential), and
// retries it all over again.
// 
// Once the thread is done with CS, it simply
// disengages the lock.
// 
// As all thread wait (spin) for the lock to
// disengage at the same time, but backoff if
// they fail in reengaging the lock, contention
// is reduced. However, this does not provide
// first-come-first-served fairness. But, as
// it only uses a single atomic value per lock
// it is suitable for medium-contention
// memory-limited architectures.

class BackoffLock extends AbstractLock {
  AtomicBoolean locked;
  static final long MIN_WAIT = 1;
  static final long MAX_WAIT = 100;
  // queue: indicates who has the token
  // size:  max allowed threads (size of queue)
  // tail:  points to end of queue
  // slot:  points where each thread stands in queue

  public BackoffLock() {
    locked = new AtomicBoolean(false);
  }

  // 1. When thread wants to access critical
  //    section, it checks to see if lock is already
  //    engaged, and if so, waits (spins).
  // 2. Once lock is disengaged it tries to reengage
  //    it. So do all other threads wanting to enter
  //    CS. Its a race between threads. So this
  //    thread checks to see it was the one who was
  //    successful, and if so its done.
  // 3. If not, then it backsoff (sleeps) for a
  //    random interval, and then retries again.
  //    Each retry the range of backoff interval
  //    is increased so reduce high-contention.
  @Override
  public void lock() {
    long W = MIN_WAIT;
    while(true) {                         // 1
      while(locked.get()) Thread.yield(); // 1
      if(!locked.getAndSet(true)) return; // 2
      long w = (long) (Math.random() *   // 3
        (MAX_WAIT-MIN_WAIT) + MIN_WAIT); // 3
      W = Math.min(2*W, MAX_WAIT);       // 3
      try { Thread.sleep(w); }           // 3
      catch(InterruptedException e) {}   // 3
    }
  }

  // 1. When a thread is done with its critical
  //    section, it simply sets the "locked" state
  //    to false.
  @Override
  public void unlock() {
    locked.set(false); // 1
  }
}
