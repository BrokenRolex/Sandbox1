package script

import org.apache.log4j.Logger

import java.nio.channels.FileLock

/**
 * Provides simple file locking.
 * <p>
 * On *nix systems, file locking is 'advisory' meaning that access to a file is not blocked
 * but a lock on a file can be detected.
 * <p>
 * This class is used to ensure that two separate processes cannot
 * use the same resource simultaneously as long as all processes
 * that attempt to use a file first try and get a lock on it.
 * <p>
 * Example:
 * <pre>
 * Lock lock = new Lock().acquireHiddenForBin()
 * 
 * // You're script code here. Until the lock
 * // is released at the end, other processes will not
 * // be able to acquire a lock ...
 * 
 * lock.release()
 * </pre>
 */
@groovy.util.logging.Log4j
class Lock {

    private FileLock lock
    private File lockFile
    private static final String MASSLOAD_LOCK_NAME = ".massload.lock"

    /**
     * Wait for a lock to become available on the script lock file.<br/>
     * Similar to {@link #acquireHiddenForBin()} except it waits for the lock
     * to become available.
     * @param wait Maximum seconds to wait. (default = 3600)
     * @param sleep Number of seconds to wait between checks. (default = 60)
     * @return Lock object
     */
    Lock waitAcquireHiddenForBin (int wait = 3600, int sleep = 60) {
        if (Env.scriptFile == null) {
            throw new Exception("cannot acquire lock, Env.scriptFile is not available")
        }
        String lockName = ".${Env.scriptFile.name}.lock"
        return waitAcquire(new File(Env.scriptFile.parentFile, lockName), wait, sleep)
    }

    /**
     * @deprecated misspelled method, use acquire
     * Wait for a lock to become available on the script lock file.<br/>
     * Similar to {@link #acquireHiddenForBin()} except it waits for the lock
     * to become available.
     * @param wait Maximum seconds to wait. (default = 3600)
     * @param sleep Number of seconds to wait between checks. (default = 60)
     * @return Lock object
     */
    @Deprecated
    Lock waitAquireHiddenForBin (int wait = 3600, int sleep = 60) {
        log.warn "waitAquireHiddenForBin(int, int) is deprecated"
        return waitAquireHiddenForBin(wait, sleep)
    }

    /**
     * Wait for a lock on a file to become available.
     * @param file to create a lock on
     * @param wait Maximum seconds to wait. (default = 3600)
     * @param sleep Number of seconds to sleep between checks. (default = 60)
     * @return Lock object
     */
    Lock waitAcquire (File file, int wait = 3600, int sleep = 60) {
        if (wait > 0) {
            sleep = [[1, sleep].max(), wait - 1].min() // 0 < sleep < wait
            log.debug "attempting to acquire lock on [$file] wait seconds [$wait] sleep seconds [$sleep]"
            Long startTime = System.currentTimeMillis()
            while (true) {
                try {
                    return acquire(file)
                }
                catch (e) {
                    // ignore this exception, it means that we could not acquire the lock
                    log.debug e.message
                }
                System.sleep(1000L * sleep)
                Integer elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000L) as Integer
                if (elapsedSeconds > wait) {
                    throw new Exception("cannot acquire lock on [$file] after waiting [$elapsedSeconds] seconds")
                }
            }
        }
        return acquire(file)
    }

    /**
     * @deprecated misspelled method, use acquire
     * Wait for a lock on a file to become available.
     * @param file to create a lock on
     * @param wait Maximum seconds to wait. (default = 3600)
     * @param sleep Number of seconds to sleep between checks. (default = 60)
     * @return Lock object
     */
    @Deprecated
    Lock waitAquire (File file, int wait = 3600, int sleep = 60) {
        log.warn "waitAquire(File, int, int) is deprecated"
        return waitAcquire(file, wait, sleep)
    }

    /**
     * Get a lock on the script.<p>
     * This is typically called at the begining of a script to ensure
     * that it cannot be run simultaneously. To accomplish this,
     * a lock is created on a hidden file <i>.scriptName.lock</i>.
     * So, if your script is <i>/some/dir/run</i> then a lock would
     * be acquired on the file <i>/some/dir/.run.lock</i>
     * @return Lock object
     */
    Lock acquireHiddenForBin () {
        if (Env.scriptFile == null) {
            throw new Exception("cannot acquire lock, Env.scriptFile not available")
        }
        return acquireHiddenForFile(Env.scriptFile)
    }

    /**
     * @deprecated misspelled method, use acquire
     * Get a lock on the script.<p>
     * This is typically called at the begining of a script to ensure
     * that it cannot be run simultaneously. To accomplish this,
     * a lock is created on a hidden file <i>.scriptName.lock</i>.
     * So, if your script is <i>/some/dir/run</i> then a lock would
     * be acquired on the file <i>/some/dir/.run.lock</i>
     * @return Lock object
     */
    @Deprecated
    Lock aquireHiddenForBin () {
        log.warn "aquireHiddenForBin() is deprecated"
        return acquireHiddenForBin()
    }

    /**
     * Acquire a lock for a file.<p>
     * This is done be creating a hidden file in the same directory as
     * the file called ".file.lock". 
     * @param file to acquire lock for
     * @return Lock object
     */
    Lock acquireHiddenForFile (File file) {
        if (file == null || !file.isFile()) {
            throw new Exception("file [${file}] does not exist")
        }
        return acquire(new File(file.parentFile, ".${file.name}.lock"))
    }

    /**
     * @deprecated misspelled method, use acquire
     * Acquire a lock for a file.<p>
     * This is done be creating a hidden file in the same directory as
     * the file called ".file.lock". 
     * @param file to acquire lock for
     * @return Lock object
     */
    @Deprecated
    Lock aquireHiddenForFile (File file) {
        log.warn "aquireHiddenForBin(File) is deprecated"
        return acquireHiddenForFile(file)
    }

    /**
     * Attempt to acquire a lock on a file.
     * @param file to lock
     * @return Lock object
     */
    Lock acquire (File file) {
        lockFile = file
        log.debug "attempting to acquire a lock on file [$lockFile]"
        lock = new RandomAccessFile(lockFile, "rw").channel.tryLock()
        if (lock == null) {
            throw new Exception("cannot acquire lock on [$lockFile]")
        }
        log.debug "acquired a lock on file [$lockFile]"
        return this
    }

    /**
     * @deprecated misspelled method, use acquire
     * Attempt to acquire a lock on a file.
     * @param file to lock
     * @return Lock object
     */
    @Deprecated
    Lock aquire (File file) {
        log.warn "aquire(File) is deprecated"
        return acquire(file)
    }
    
    /**
     * @deprecated 
     * Many processes that update a WCS database use the massload process.
     * These processes may need to synchronize so that they do not hang due
     * to conflicting database locks. This method will wait to aquie a lock
     * on the file USER_HOME/.massload.lock file.
     * @param maxwaitSeconds max time to wait to aquire lock
     * @param sleepSeconds time to wait between checks to see if lock is available
     * @return this
     */
    @Deprecated
    Lock waitAquireForMassload (int maxwaitSeconds, int sleepSeconds) {
        return waitAquire(new File(Env.homeDir, MASSLOAD_LOCK_NAME), maxwaitSeconds, sleepSeconds)
    }

    /**
     * @deprecated 
     * Many processes that update a WCS database use the massload process. <br/>
     * These processes may need to synchronize so that they do not hang due
     * to conflicting database locks.<br/>
     * This method will wait to aquie a lock on the file USER_HOME/.massload.lock file.<br/>
     * Wait for 1 hour checking every minute for the lock to become available.
     * @return Lock object
     */
    @Deprecated
    Lock waitAquireForMassload () {
        log.warn "waitAquireForMassload() is deprecated"
        return waitAquireForMassload (3600, 60)
    }

    /**
     * Release the lock
     */
    void release () {
        log.debug "attempting to release a lock on file [$lockFile]"
        if (lock == null) {
            log.warn 'no lock set to release'
            return
        }
        try {
            lock.release()
        }
        catch (e) {
            log.warn e.message
        }
        finally {
            lock = null
            log.debug "released lock on file [$lockFile]"
        }
    }
}