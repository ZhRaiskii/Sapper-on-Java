public class Statistics {
    private int user_id;
    private int statistic_5x5;
    private int statistic_8x8;
    private int statistic_16x16;

    public Statistics(int user_id, int statistic_5x5, int statistic_8x8, int statistic_16x16) {
        this.user_id = user_id;
        this.statistic_5x5 = statistic_5x5;
        this.statistic_8x8 = statistic_8x8;
        this.statistic_16x16 = statistic_16x16;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getStatistic_5x5() {
        return statistic_5x5;
    }

    public void setStatistic_5x5(int statistic_5x5) {
        this.statistic_5x5 = statistic_5x5;
    }

    public int getStatistic_8x8() {
        return statistic_8x8;
    }

    public void setStatistic_8x8(int statistic_8x8) {
        this.statistic_8x8 = statistic_8x8;
    }

    public int getStatistic_16x16() {
        return statistic_16x16;
    }

    public void setStatistic_16x16(int statistic_16x16) {
        this.statistic_16x16 = statistic_16x16;
    }
}
