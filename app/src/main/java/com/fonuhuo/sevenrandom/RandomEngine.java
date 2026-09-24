package com.fonuhuo.sevenrandom;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public final class RandomEngine {
    public static final String[] PALACES = {"大安", "留连", "速喜", "赤口", "小吉", "空亡"};

    private final Random random;

    public RandomEngine() {
        this(new SecureRandom());
    }

    RandomEngine(Random random) {
        this.random = random;
    }

    public int[] generateThree() {
        return new int[]{nextNumber(), nextNumber(), nextNumber()};
    }

    public RollResult generateResult() {
        int[] numbers = generateThree();
        return new RollResult(numbers, calculatePalaces(numbers));
    }

    public RollingSession newSession() {
        return new RollingSession(this);
    }

    private int nextNumber() {
        return random.nextInt(999) + 1;
    }

    public String[] calculatePalaces(int[] numbers) {
        if (numbers == null || numbers.length != 3) {
            throw new IllegalArgumentException("Exactly three numbers are required");
        }
        int first = Math.floorMod(numbers[0] - 1, 6);
        int second = Math.floorMod(first + numbers[1] - 1, 6);
        int third = Math.floorMod(second + numbers[2] - 1, 6);
        return new String[]{PALACES[first], PALACES[second], PALACES[third]};
    }

    private void mixTouchSeed(long eventTime, float x, float y) {
        long seed = System.nanoTime()
                ^ (eventTime << 21)
                ^ ((long) Float.floatToIntBits(x) << 32)
                ^ (Float.floatToIntBits(y) & 0xffffffffL);
        random.setSeed(seed);
    }

    public static final class RollingSession {
        private final RandomEngine engine;
        private boolean stopped;

        RollingSession(RandomEngine engine) {
            this.engine = engine;
        }

        public int[] preview() {
            if (stopped) throw new IllegalStateException("Session already stopped");
            return engine.generateThree();
        }

        public RollResult stop(long eventTime, float x, float y) {
            if (stopped) throw new IllegalStateException("Session already stopped");
            stopped = true;
            engine.mixTouchSeed(eventTime, x, y);
            return engine.generateResult();
        }
    }

    public static final class RollResult {
        private final int[] numbers;
        private final String[] palaces;

        RollResult(int[] numbers, String[] palaces) {
            this.numbers = Arrays.copyOf(numbers, numbers.length);
            this.palaces = Arrays.copyOf(palaces, palaces.length);
        }

        public int[] getNumbers() {
            return Arrays.copyOf(numbers, numbers.length);
        }

        public String[] getPalaces() {
            return Arrays.copyOf(palaces, palaces.length);
        }

        public String getFinalPalace() {
            return palaces[2];
        }
    }
}
