package cn.edu.bjut.inferencers;

import java.io.File;
import java.io.PrintWriter;

/**
 * Interface for simple Gibbs query sampling implementations
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 */
public interface IInferencer {

    /**
     * initialize Markov chain for querying
     */
    public void init();

    /**
     * run Gibbs sampler for querying
     * 
     * @param niter number of Gibbs iterations
     */
    public void inference(final int niter);
    
    public void printAssign(final File f); 
    
    public void printAssign(final PrintWriter pw);
}
