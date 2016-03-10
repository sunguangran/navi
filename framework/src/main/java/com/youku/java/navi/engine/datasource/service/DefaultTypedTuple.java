package com.youku.java.navi.engine.datasource.service;

import com.youku.java.navi.engine.core.INaviCache;


public class DefaultTypedTuple<V> implements INaviCache.TypedTuple<V> {

    private final Double score;
    private final V value;

    /**
     * Constructs a new <code>DefaultTypedTuple</code> instance.
     *
     * @param value
     * @param score
     */
    public DefaultTypedTuple(V value, Double score) {
        this.score = score;
        this.value = value;
    }


    public Double getScore() {
        return score;
    }


    public V getValue() {
        return value;
    }


    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((score == null) ? 0 : score.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }


    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof DefaultTypedTuple))
            return false;
        @SuppressWarnings("rawtypes")
        DefaultTypedTuple other = (DefaultTypedTuple) obj;
        if (score == null) {
            if (other.score != null)
                return false;
        } else if (!score.equals(other.score))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }


    public int compareTo(Double o) {
        Double d = (score == null ? Double.valueOf(0) : score);
        Double a = (o == null ? Double.valueOf(0) : o);
        return d.compareTo(a);
    }

}
