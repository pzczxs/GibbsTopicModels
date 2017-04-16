package cn.edu.bjut.utils;

public class IntegerPair extends Pair<Integer, Integer> implements Comparable<IntegerPair> {
	
	public IntegerPair(final Integer first, final Integer second) {
		super(first, second); 
	}
	
	public IntegerPair(final int first, final int second) {
		super(Integer.valueOf(first), Integer.valueOf(second)); 
	}
	
	//
	@Override
	public int compareTo(final IntegerPair o2) {
		if (first > o2.getFirst()) {
			return 1; 
		} else if (first < o2.getFirst()) {
			return -1; 
		} else {
			if (second > o2.getSecond()) {
				return 1;
			} else if (second == o2.getSecond()) {
				return 0;
			} else { 
				return -1;
			}
		}
	}
}
