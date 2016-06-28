package jeston.org.mobilegrammar;

/**
 * Created by Jeston on 24.06.2016.
 */
public enum GroupId {
    mainGroup(0), group1(1), group2(2), group3(3), group4(4);
    private final long value;

    GroupId(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
