package cn.edu.bjut.inferencers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cn.edu.bjut.corpus.Corpus;
import cn.edu.bjut.estimators.LDAEstimator;
import cn.edu.bjut.parameters.LDAParameter;
import cn.edu.bjut.utils.CokusRandom;

/*
 * Gibbs sampling algorithm for Latent Dirichlet Allocation (LDA) model. 
 * 
 * References: 
 * [1] David M. Blei, Andrew Y. Ng, and Michael I. Jordan, 2003. Latent 
 * Dirichlet Allocation. Journal of Machine Learning Research, Vol. 3, 
 * No. Jan, pp. 993-1022. 
 * [2] Thomas L. Griffiths and Mark Steyvers, 2004. Finding Scientific 
 * Topics. Proceedings of the National Academy of Sciences of the United 
 * States of America, Vol. 101, No. Suppl, pp. 5228-5235. 
 * [3] Gregor Heinrich, 2009. Parameter Estimation for Text Analysis. Technical 
 * Report Version 2.9. vsonix GmbH and University of Leipzig. 
 * 
 * Author: XU, Shuo (pzczxs@gmail.com)
 * */
public class LDAInferencer implements IInferencer, IPerplexity {
	private Corpus corpus; 
	private LDAParameter param; 
	private LDAEstimator estimator; 
	
	private CokusRandom rand; 
	
	private int[][] nmk; // size: M x K
	private int[][] z;
	
	private double Kalpha; 
	
	public LDAInferencer(final String filebase, final long seed, final LDAEstimator estimator) {
		this.estimator = estimator; 
		this.param = estimator.getParameter();
		this.corpus = new Corpus(filebase + ".corpus"); 
		
		this.rand = new CokusRandom(seed); 
		this.Kalpha = param.getNTopics() * param.getAlpha(); 
	}

	@Override
	public void init() {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords();
		
		// allocate
		nmk = new int[M][K];
		z = new int[M][];

		// initialize
		for (int m = 0; m < M; m++) {
			z[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				final int k = this.rand.nextInt(K);
				z[m][n] = k;
				nmk[m][k]++;
			}
		}
	}

	@Override
	public void inference(final int niter) {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		final double alpha = param.getAlpha(); 
		
		final double[][] varphi = estimator.getVarphi(false);
		
		double[] pp = new double[K];
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				for (int n = 0; n < w[m].length; n++) {
					final int k = z[m][n];
					final int t = w[m][n];
					
					// decrement
					nmk[m][k]--;
					
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < pp.length; kk++) {
						pp[kk] = (nmk[m][kk] + alpha) * varphi[kk][t];
						psum += pp[kk];
					}
					
					// sample
					final int kk = rand.nextDiscrete(pp, psum); 
					
					// reassign and increment
					z[m][n] = kk;
					nmk[m][kk]++; 
				} // n
			} // m
		} // i
	}
	
	@Override
	public double ppx() {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		
		final double[][] varphi = estimator.getVarphi(false);
		
		double loglik = 0;
		int W = 0; 
		for (int m = 0; m < M; m++) {
			final int[] w = corpus.getDocWords(m); 
			final double[] vartheta = getVartheta(m, false);
			final int Nm = w.length; 
			
			for (int n = 0; n < Nm; n++) {
				double sum = 0.0;
				for (int k = 0; k < K; k++) {
					sum += vartheta[k] * varphi[k][w[n]];
				}
				loglik += Math.log(sum);
			}
			
			W += Nm; 
		}
		
		return Math.exp(-loglik / W);
	}

	/*
	 * @flag = true: log
	 * */
	public double[][] getVartheta(final boolean flag) {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		final double alpha = param.getAlpha();

		double[][] vartheta = new double[M][K];
		for (int m = 0; m < M; m++) {
			final int Nm = corpus.getDocWords(m).length;
			for (int k = 0; k < K; k++) {
				vartheta[m][k] = (nmk[m][k] + alpha) / (Nm + Kalpha);
				
				if (flag) {
					vartheta[m][k] = Math.log(vartheta[m][k]); 
					if (Double.isNaN(vartheta[m][k])) {
						System.err.println("The vartheta[" +  m + "][" + k + "] is NaN.");
						System.exit(-1);
					}
				}
			}
		}

		return vartheta;
	}
	
	/*
	 * @flag = true: log
	 * */
	public double[] getVartheta(final int m, final boolean flag) {
		final int K = param.getNTopics();
		final double alpha = param.getAlpha();

		double[] vartheta = new double[K];
		final int Nm = corpus.getDocWords(m).length;
		for (int k = 0; k < K; k++) {
			vartheta[k] = (nmk[m][k] + alpha) / (Nm + Kalpha);
			
			if (flag) {
				vartheta[k] = Math.log(vartheta[k]); 
				if (Double.isNaN(vartheta[k])) {
					System.err.println("The vartheta[" +  m + "][" + k + "] is NaN.");
					System.exit(-1);
				}
			}
		}

		return vartheta;
	}
	
	/*
	 * @flag = true: log
	 * */
	public double getVartheta(final int m, final int k, final boolean flag) {
		final double alpha = param.getAlpha();
		final int Nm = corpus.getDocWords(m).length;

		double vartheta = (nmk[m][k] + alpha) / (Nm + Kalpha); 
		if (flag) {
			vartheta = Math.log(vartheta); 
			if (Double.isNaN(vartheta)) {
				System.err.println("The vartheta[" +  m + "][" + k + "] is NaN.");
				System.exit(-1);
			}
		}
		
		return vartheta;
	}

	@Override
	public void printAssign(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printAssign(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	@Override
	public void printAssign(final PrintWriter pw) {
		final int M = corpus.getNumDocs(); 
		
		for (int m = 0; m < M; m++) {
			final int[] w = corpus.getDocWords(m); 
			
			for (int n = 0; n < w.length; n++) {
				pw.print(w[n] + ":" + z[m][n] + " "); 
			}
			pw.println(); 
		}
	}
}
