package cn.edu.bjut.estimators;

import java.io.File;
import java.io.PrintWriter;

/**
 * Interface for simple Gibbs sampling implementations
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 */
public interface IEstimator {
    /**
     * initialize Markov chain
     */
    public void init();

    /**
     * run Gibbs sampler
     * 
     * @param niter 
     * 		number of Gibbs iterations
     */
    public void estimate(final int niter);
    
    public void printAssign(final File f); 
    
    public void printAssign(final PrintWriter pw); 
}
