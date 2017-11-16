package pl.szewczyk.projects;

/**
 * Created by przem on 12.09.2017.
 */
public enum FrequencyEnum {
    _5(5),
    _10(10),
    _15(15),
    _20(20),
    _25(25),
    _30(30),
    _35(35),
    _40(40),
    _45(45),
    _50(50),
    _55(55),
    _60(60),
    _120(120);

    private int frequency;

    public int getFrequency() {
        return frequency;
    }

    FrequencyEnum(int frequency) {
        this.frequency = frequency;
    }
}
