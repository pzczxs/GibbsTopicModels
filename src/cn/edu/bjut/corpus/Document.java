package cn.edu.bjut.corpus;

import java.util.List;
import java.util.Vector;

public class Document {

	private int[] words;
	
	public Document() {
		this.words = new int[0]; 
	}
	
	public Document(final int length) {
		this.words = new int[length]; 
	}
	
	public Document(final int[] words) {
		final int length = words.length;
		this.words = new int[length]; 
		
		System.arraycopy(words, 0, this.words, 0, length); 
	}
	
	public Document(final Vector<Integer> words) {
		this(words.size()); 
		
		for (int n = 0; n < words.size(); n++) {
			this.words[n] = words.get(n);
		}
	}
	
	public Document(final List<Integer> words) {
		this(words.size()); 
		
		for (int n = 0; n < words.size(); n++) {
			this.words[n] = words.get(n); 
		}
	}
	
	public Document(final Document d) {
		final int length = d.words.length; 
		this.words = new int[length];
		
		System.arraycopy(d.words, 0, this.words, 0, length); 
	}
	
	public int[] getWords() {
		return this.words; 
	}
	
	public int getWord(final int n) {
		if (n < 0 || n >= words.length) {
			throw new IndexOutOfBoundsException("n: " + n); 
		}
		
		return this.words[n]; 
	}
}
