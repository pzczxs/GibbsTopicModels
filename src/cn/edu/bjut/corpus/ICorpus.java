package cn.edu.bjut.corpus;

public interface ICorpus {

    int getNumTerms();
    
    int getNumDocs();
    
    int getNumWords();
    
    int getNumWords(final int m); 

    int[][] getDocWords();
    
    int[] getDocWords(final int m);
}
