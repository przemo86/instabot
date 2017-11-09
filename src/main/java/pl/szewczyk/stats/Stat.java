package pl.szewczyk.stats;

/**
 * Created by przem on 09.11.2017.
 */
public class Stat {
    private String tag;
    private double hits;
    private double total;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getHits() {
        return hits;
    }

    public void setHits(double hits) {
        this.hits = hits;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
