package cn.edu.bjut.utils;

public class IntegerDoublePair extends Pair<Integer, Double> implements Comparable<IntegerDoublePair> {
	
	public IntegerDoublePair(final Integer first, final Double second) {
		super(first, second); 
	}
	
	public IntegerDoublePair(final int first, final double second) {
		super(Integer.valueOf(first), Double.valueOf(second)); 
	}
	
	@Override
	public int compareTo(final IntegerDoublePair o2) {
		if (second > o2.getSecond()) {
			return -1;
		} else if (second < o2.getSecond()) {
			return 1;
		} else { 
			return 0;
		}
	}
}
