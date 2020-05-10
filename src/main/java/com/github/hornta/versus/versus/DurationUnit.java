package com.github.hornta.versus.versus;

public class DurationUnit {
  private final long amount;
  private final String singularis;
  private final String pluralis;

  public DurationUnit(long amount, String singularis, String pluralis) {
    this.amount = amount;
    this.singularis = singularis;
    this.pluralis = pluralis;
  }

  public long getAmount() {
    return amount;
  }

  public String getNumerus() {
    if(amount == 1) {
      return singularis;
    }

    return pluralis;
  }
}
