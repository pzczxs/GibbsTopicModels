package cn.edu.bjut.utils;

public class Relation<T> {
    private T first;
    private T second;
    private boolean symmetric;
    
    public Relation(T first, T second) {
    	this(first, second, true); 
    }

    public Relation(T first, T second, boolean symmetric) {
    	this.first = first;
    	this.second = second;
    	this.symmetric = symmetric; 
    }
   
    @Override
	public int hashCode() {
    	int hashFirst = (first != null? first.hashCode(): 0);
    	int hashSecond = (second != null? second.hashCode(): 0);

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

    @Override
    public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof Relation)) {
			return false; 
		}
		
		if (this == obj) {
			return true; 
		}
		
		Relation<?> other = (Relation<?>) obj;
		
		if (this.symmetric != other.isSymmetric()) {
			return false; 
		}

		if (this.symmetric) {
			return (this.first.equals(other.getFirst())) && (this.second.equals(other.getSecond())) 
				|| (this.first.equals(other.getSecond())) && (this.second.equals(other.getFirst()));
		} else {
			return (this.first.equals(other.getFirst())) && (this.second.equals(other.getSecond()));
		}
	}

    @Override
    public String toString() { 
           return "(" + this.first + "," + this.second + ")"; 
    }

    public T getFirst() {
    	return this.first;
    }

    public void setFirst(T first) {
    	this.first = first;
    }

    public T getSecond() {
    	return this.second;
    }

    public void setSecond(T second) {
    	this.second = second;
    }

	public boolean isSymmetric() {
		return this.symmetric;
	}

	public void setSymmetric(boolean symmetric) {
		this.symmetric = symmetric;
	}
}
