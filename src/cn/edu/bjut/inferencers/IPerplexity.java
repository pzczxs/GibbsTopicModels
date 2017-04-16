package cn.edu.bjut.inferencers;

/**
 * ISimplePpx interface for simple perplexity calculations
 * 
 * @author XU, Shuo (pzczxs@gmail.com)
 */
public interface IPerplexity {

    /**
     * @return the perplexity of the last query sample
     */
    public double ppx();

}
