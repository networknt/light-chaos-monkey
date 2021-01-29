package com.networknt.chaos;

public class MemoryAssaultConfig {
    public static final String CONFIG_NAME = "memory-assault";
    boolean enabled;
    int level;
    int memoryMillisecondsHoldFilledMemory;
    int memoryMillisecondsWaitNextIncrease;
    float memoryFillIncrementFraction;
    float memoryFillTargetFraction;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMemoryMillisecondsHoldFilledMemory() {
        return memoryMillisecondsHoldFilledMemory;
    }

    public void setMemoryMillisecondsHoldFilledMemory(int memoryMillisecondsHoldFilledMemory) {
        this.memoryMillisecondsHoldFilledMemory = memoryMillisecondsHoldFilledMemory;
    }

    public int getMemoryMillisecondsWaitNextIncrease() {
        return memoryMillisecondsWaitNextIncrease;
    }

    public void setMemoryMillisecondsWaitNextIncrease(int memoryMillisecondsWaitNextIncrease) {
        this.memoryMillisecondsWaitNextIncrease = memoryMillisecondsWaitNextIncrease;
    }

    public float getMemoryFillIncrementFraction() {
        return memoryFillIncrementFraction;
    }

    public void setMemoryFillIncrementFraction(float memoryFillIncrementFraction) {
        this.memoryFillIncrementFraction = memoryFillIncrementFraction;
    }

    public float getMemoryFillTargetFraction() {
        return memoryFillTargetFraction;
    }

    public void setMemoryFillTargetFraction(float memoryFillTargetFraction) {
        this.memoryFillTargetFraction = memoryFillTargetFraction;
    }
}
