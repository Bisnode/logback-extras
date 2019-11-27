package com.bisnode.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of Logback filter that filters (i.e stops logging) duplicate messages for a cooldown period once the
 * configured threshold has been met. Resumes logging again after the duration of the cooldown period but will trigger
 * again every time the cooldown initiation threshold is reached.
 *
 * Caveats: Messages must be exact duplicates to be counted - similarity is not recognized.
 */
@SuppressWarnings("unused")
public class DuplicateMessageCooldownFilter extends TurboFilter {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ConcurrentMap<String, AtomicInteger> messageCounter = new ConcurrentHashMap<>(1024);

    private int cooldownInitiationThreshold = 20;
    private int cooldownDurationSeconds = 30;
    private int acceptableDuplicateFrequencyPerSecond = 1;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void start() {
        messageCounter = new ConcurrentHashMap<>(1024);
        if (acceptableDuplicateFrequencyPerSecond > 0) {
            scheduler.scheduleAtFixedRate(() -> messageCounter.forEach((message, count) -> {
                int currentCount = count.get();
                if (currentCount > cooldownInitiationThreshold) {
                    // This is scheduled for reset already, do nothing
                } else if (currentCount <= 0) {
                    // Remove from cache to not keep sparse messages around
                    messageCounter.remove(message);
                } else {
                    // Decreasing count for message by configured acceptable frequency
                    count.updateAndGet(i -> i - acceptableDuplicateFrequencyPerSecond);
                }
            }), 1L, 1L, TimeUnit.SECONDS);
        }
        super.start();
    }

    @Override
    public void stop() {
        messageCounter.clear();
        super.stop();
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String message, Object[] params, Throwable t) {
        if (message == null) {
            return FilterReply.NEUTRAL;
        }
        AtomicReference<FilterReply> reply = new AtomicReference<>(FilterReply.NEUTRAL);
        messageCounter.putIfAbsent(message, new AtomicInteger(0));
        messageCounter.computeIfPresent(message, (key, count) -> {
            int currentCount = count.incrementAndGet();
            if (currentCount == cooldownInitiationThreshold) {
                // Count for key hit threshold of $count. Suppressing duplicates for $cooldownDurationSeconds.
                scheduler.schedule(() -> {
                    System.out.println("Clearing count for key " + key);
                    return messageCounter.remove(key);
                }, cooldownDurationSeconds, TimeUnit.SECONDS);
            }
            if (currentCount >= cooldownInitiationThreshold) {
                reply.set(FilterReply.DENY);
            }

            return count;
        });

        return reply.get();
    }

    /**
     * Get the configured number of duplicate messages that should trigger a cooldown period.
     *
     * @return Number of duplicate messages that should trigger a cooldown
     */
    public int getCooldownInitiationThreshold() {
        return cooldownInitiationThreshold;
    }

    /**
     * Set the configured number of duplicate messages that should trigger a cooldown period.
     *
     * @param cooldownInitiationThreshold Number of messages required to trigger cooldown
     */
    public void setCooldownInitiationThreshold(int cooldownInitiationThreshold) {
        this.cooldownInitiationThreshold = cooldownInitiationThreshold;
    }

    /**
     * Get the configured duration of the cooldown period, i.e. how long duplicate messages should be suppressed.
     *
     * @return The configured duration of the cooldown period
     */
    public int getCooldownDurationSeconds() {
        return cooldownDurationSeconds;
    }

    /**
     * Set the configured duration of the cooldown period, i.e. how long duplicate messages should be suppressed.
     *
     * @param cooldownDurationSeconds Number of seconds to suppress duplicate log message
     */
    public void setCooldownDurationSeconds(int cooldownDurationSeconds) {
        this.cooldownDurationSeconds = cooldownDurationSeconds;
    }

    /**
     * Get the configured acceptable duplicate frequency per second. This number will be subtracted from the duplicate
     * counter each second.
     *
     * @return The configured duration of the cooldown period
     */
    public int getAcceptableDuplicateFrequencyPerSecond() {
        return acceptableDuplicateFrequencyPerSecond;
    }

    /**
     * Set the configured acceptable duplicate frequency per second. This number will be subtracted from the duplicate
     * counter each second.
     *
     * @param acceptableDuplicateFrequencyPerSecond Amount of duplicate messages to accept per seconnd
     */
    public void setAcceptableDuplicateFrequencyPerSecond(int acceptableDuplicateFrequencyPerSecond) {
        this.acceptableDuplicateFrequencyPerSecond = acceptableDuplicateFrequencyPerSecond;
    }

}
