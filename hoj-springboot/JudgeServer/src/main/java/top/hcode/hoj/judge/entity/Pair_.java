package top.hcode.hoj.judge.entity;

/**
 * @Author Himit_ZH
 * @Date 2022/8/29
 */
public class Pair_<T1, T2> {

    private T1 key;
    private T2 value;

    public Pair_(T1 key, T2 value) {
        this.key = key;
        this.value = value;
    }

    public T1 getKey() {
        return key;
    }

    public void setKey(T1 key) {
        this.key = key;
    }

    public T2 getValue() {
        return value;
    }

    public void setValue(T2 value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject)
            return true;
        if (anObject instanceof Pair_<?, ?>) {
            Pair_<?, ?> that = (Pair_<?, ?>) anObject;
            return this.key.equals(that.key) && this.value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 666 + value.hashCode();
    }
}
