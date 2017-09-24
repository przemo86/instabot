package pl.szewczyk.projects;

/**
 * Created by przem on 12.09.2017.
 */
public enum FrequencyEnum {
    _10(10),
    _30(30),
    _45(45),
    _60(60),
    _150(150),
    _300(300);

    private int frequency;

    public int getFrequency() {
        return frequency;
    }

    FrequencyEnum(int frequency) {
        this.frequency = frequency;
    }
}
