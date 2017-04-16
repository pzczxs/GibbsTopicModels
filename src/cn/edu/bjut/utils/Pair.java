package cn.edu.bjut.utils;

/**
 * Pair is a Class for holding mutable pairs of objects.
 *
 * <i>Implementation note:</i>
 * on a 32-bit JVM uses ~ 8 (this) + 4 (first) + 4 (second) = 16 bytes.
 * on a 64-bit JVM uses ~ 16 (this) + 8 (first) + 8 (second) = 32 bytes.
 *
 * Many applications use a lot of Pairs so it's good to keep this
 * number small.
 *
 * @author XU Shuo (pzczxs@gmail.com)
 */
public class Pair<T1, T2> {
    protected T1 first;
    protected T2 second;
    
    public Pair(T1 first, T2 second) {
    	this.first = first;
    	this.second = second;
    }
   
    @Override
	public int hashCode() {
    	int hashFirst = (this.first != null? this.first.hashCode(): 0);
    	int hashSecond = (this.second != null? this.second.hashCode(): 0);

    	return (hashFirst + hashSecond) * hashSecond + hashFirst; 
	}

    @Override
    public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof Pair)) {
			return false; 
		}
		
		if (this == obj) {
			return true; 
		}
		
		Pair<?, ?> other = (Pair<?, ?>) obj;

		return (first == null ? other.getFirst() == null : first.equals(other.getFirst())) 
				&& (second == null ? other.getSecond() == null : second.equals(other.getSecond()));
	}

    @Override
    public String toString() { 
           return "(" + this.first + ", " + this.second + ")"; 
    }

    public T1 getFirst() {
    	return this.first;
    }

    public void setFirst(T1 first) {
    	this.first = first;
    }

    public T2 getSecond() {
    	return this.second;
    }

    public void setSecond(T2 second) {
    	this.second = second;
    }
}
