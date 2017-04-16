package cn.edu.bjut.utils;

import java.util.Stack;

public class Util {
	public static boolean containCJKChar(final String str) {
		if (str == null) {
			return false; 
		}
		
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i); 
			
			if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
				return true; 
			}
		}
		
		return false; 
	}
	
	public static int countOccurrenceOf(String str, String findStr) {
		int count = 0; 
		for (int lastIndex = 0; lastIndex != -1; ) {
			lastIndex = str.indexOf(findStr, lastIndex); 
			
			if (lastIndex != -1) {
				count++; 
				lastIndex += findStr.length(); 
			}
		}
		
		return count; 
	}
	
	public static int countOccurrenceOf(String str, char c) {
		int count = 0; 
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c) {
				count++; 
			}
		}
		
		return count; 
	}
	
	public static boolean isMatch(String str) {
		char [] charArray = str.toCharArray(); 
		Stack<Character> stack = new Stack<Character>(); 
		for (char c: charArray) {
			if (c == '(' || c == '[' || c == '}') {
				stack.push(c); 
			}
			
			if (c == ')' || c == ']' || c == '}') {
				if (stack.isEmpty()) {
					return false; 
				}
				
				Character cc = stack.pop(); 
				
				switch(c) {
				case ')': 
					if (cc != '(') {
						return false; 
					}
					break; 
				case ']': 
					if (cc != '[') {
						return false; 
					}
					break; 
				case '}': 
					if (cc != '{') {
						return false; 
					}
					break; 
				}
			}
		}
		
		return stack.isEmpty()? true: false; 
	}
}
