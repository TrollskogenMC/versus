package com.github.hornta.versus.versus;

public class Cooldown {
  private final long duration;
  private final long startTime;
  private final CooldownType cooldownType;

  public Cooldown(long duration, CooldownType cooldownType) {
    this.duration = duration;
    startTime = System.currentTimeMillis();
    this.cooldownType = cooldownType;
  }

  public boolean isActive() {
    return getTimeLeft() > 0L;
  }

  public long getDuration() {
    return duration;
  }

  public long getStartTime() {
    return startTime;
  }

  public CooldownType getType() {
    return cooldownType;
  }

  public long getTimeLeft() {
    return getDuration() - (System.currentTimeMillis() - getStartTime());
  }

  public long getSecondsLeft() {
    return (long)Math.ceil((double)(getTimeLeft() / 1000L));
  }
}

