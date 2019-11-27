# Logback extras
![](https://github.com/Bisnode/logback-extras/workflows/build/badge.svg)

Extras and helpers for Logback logging.

### Installation

#### Maven

```xml
<dependency>
    <groupId>com.bisnode.logging</groupId>
    <artifactId>logback-extras</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle
```groovy
implementation group: 'com.bisnode.logging', name: 'logback-extras', version: '1.0.0'
```

## Duplicate message cooldown filter

The duplicate message cooldown filter resembles the Logback included 
[DuplicateMessageFilter](http://logback.qos.ch/manual/filters.html#DuplicateMessageFilter) but instead of suppressing
duplicate messages _forever_ after hitting the configured threshold it does so only during the configured cooldown 
period, and after that resumes logging the duplicate until the threshold is hit once again.

When dealing with misconfigured or poorly implemented logging, this saves your logs from getting flooded with duplicate
messages, while still logging some of them in short bursts for a chance to be seen and possibly fixed.

This filter is similar to the "burst filter" [sometimes](https://jira.qos.ch/browse/LOGBACK-803) 
[requested](https://jira.qos.ch/browse/LOGBACK-223) for Logback and since some time back 
[available in Log4j](https://logging.apache.org/log4j/2.x/manual/filters.html#BurstFilter), but is more conservative in 
that it only filters duplicates as those are per definition noise, while meaningful logging may be suppressed by a 
more general burst filter.

### Usage

In your Logback XML configuration, add a `turboFilter` configuration pointing to the `DuplicateMessageCooldownFilter`:

```$xml
<configuration>

    <turboFilter class="com.bisnode.logging.DuplicateMessageCooldownFilter">
        <cooldownInitiationThreshold>10</cooldownInitiationThreshold>
        <cooldownDurationSeconds>60</cooldownDurationSeconds>
        <acceptableDuplicateFrequencyPerSecond>1</acceptableDuplicateFrequencyPerSecond>
    </turboFilter>

</configuration>
```

#### Configuration options

* `cooldownInitiationThreshold`: The number of duplicate messages that should trigger a cooldown period. Default 20.
* `cooldownDurationSeconds`: Duration of the cooldown period, i.e. how long duplicate messages should be suppressed.
  Default 30.
* `acceptableDuplicateFrequencyPerSecond`: Acceptable duplicate frequency per second. This number will be subtracted 
  from the duplicate counter every second. Default 1.

### Dependencies

This package does not introduce additional dependencies.


