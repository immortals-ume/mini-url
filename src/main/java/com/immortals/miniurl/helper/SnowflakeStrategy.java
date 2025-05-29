package com.immortals.miniurl.helper;

import com.immortals.miniurl.factory.UrlShorteningStrategy;
import com.immortals.miniurl.utils.Base62Utils;
import org.springframework.stereotype.Component;

import static com.immortals.miniurl.constants.UrlConstants.*;

@Component
public class SnowflakeStrategy implements UrlShorteningStrategy {

    private final Long maxSequence;
    private Long lastTimestamp = -1L;
    private Long sequence = 0L;


    public SnowflakeStrategy() {

        Long maxMachineId = (1L << MACHINE_ID_BITS) - 1;
        this.maxSequence = (1L << SEQUENCE_BITS) - 1;

        if (MACHINE_ID_BITS < 0 || MACHINE_ID_BITS > maxMachineId) {
            throw new IllegalArgumentException(String.format(
                    "Machine ID must be between 0 and %d, but was %d", maxMachineId,MACHINE_ID_BITS));
        }
        if (EPOCH <= 0) {
            throw new IllegalArgumentException("Epoch must be set and positive");
        }
    }

    @Override
    public synchronized String generate(String originalUrl, String... params) {
        Long timestamp = currentTime();

        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate id for "
                    + (lastTimestamp - timestamp) + " milliseconds");
        }

        if (timestamp.equals(lastTimestamp)) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = waitNextMillis(timestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        long id = ((timestamp -EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS))
                | (MACHINE_ID << SEQUENCE_BITS)
                | sequence;

        return Base62Utils.encode(id);
    }

    private Long waitNextMillis(Long currentTimestamp) {
        Long timestamp = currentTime();
        while (timestamp <= currentTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    private Long currentTime() {
        return System.currentTimeMillis();
    }
}
