# Logback extras

They do not introduce additional dependencies.

### Installation

TBD

## Duplicate message cooldown filter

The duplicate message cooldown filter resembles the Logback included 
[DuplicateMessageFilter](http://logback.qos.ch/manual/filters.html#DuplicateMessageFilter) but instead of suppressing
duplicate messages _forever_ after hitting the configured threshold it does so only during the configured cooldown 
period, and after that resumes logging the duplicate until the threshold is hit once again.

When dealing with misconfigured or poorly implemented logging, this saves your logs from getting flooded with duplicate
messages, while still logging some of them in short bursts for a chance to be seen and possibly fixed.

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




