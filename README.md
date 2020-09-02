Exponential Backoff Lock uses an atomic value for
indicating that some thread has engaged the lock
and is executing its critical section (CS).

Each thread that wants to enter CS waits until
the atomic lock is disengaged. Then it tries
to reengage it, but as several threads might
be trying the same, it may or may not succeed.
So it checks if it succeeded (with the same
atomic operation), and if so it done. If not
it backs off for a random interval which is
doubled every retry (hence exponential), and
retries it all over again.

Once the thread is done with CS, it simply
disengages the lock.

As all thread wait (spin) for the lock to
disengage at the same time, but backoff if
they fail in reengaging the lock, contention
is reduced. However, this does not provide
first-come-first-served fairness. But, as
it only uses a single atomic value per lock
it is suitable for medium-contention
memory-limited architectures.

Exponential back off is a well-known technique
used in Ethernet routing, presented in the
context of multiprocessor mutual exclusion by
[Anant Agarwal] and [Mathews Cherian].

[Anant Agarwal]: https://scholar.google.com/citations?hl=en&user=E6XXUFcAAAAJ
[Mathews Cherian]: https://dl.acm.org/profile/81100089786

```java
1. When thread wants to access critical
   section, it checks to see if lock is already
   engaged, and if so, waits (spins).
2. Once lock is disengaged it tries to reengage
   it. So do all other threads wanting to enter
   CS. Its a race between threads. So this
   thread checks to see it was the one who was
   successful, and if so its done.
3. If not, then it backs off (sleeps) for a
   random interval, and then retries again.
   Each retry the range of backoff interval
   is increased so reduce high-contention.
```

```java
1. When a thread is done with its critical
   section, it simply sets the "locked" state
   to false.
```

See [BackoffLock.java] for code, [Main.java] for test, and [repl.it] for output.

[BackoffLock.java]: https://repl.it/@wolfram77/backoff-lock#BackoffLock.java
[Main.java]: https://repl.it/@wolfram77/backoff-lock#Main.java
[repl.it]: https://backoff-lock.wolfram77.repl.run


### references

- [The Art of Multiprocessor Programming :: Maurice Herlihy, Nir Shavit](https://dl.acm.org/doi/book/10.5555/2385452)
- [Adaptive backoff synchronization techniques :: Anant Agarwal, Mathews Cherian](https://ieeexplore.ieee.org/document/714578)
