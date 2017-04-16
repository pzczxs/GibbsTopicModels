package cn.edu.bjut.estimators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import cn.edu.bjut.corpus.Corpus;
import cn.edu.bjut.corpus.CorpusResolver;
import cn.edu.bjut.parameters.LDAParameter;
import cn.edu.bjut.utils.CokusRandom;
import cn.edu.bjut.utils.IntegerDoublePair;

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
public class LDAEstimator implements IEstimator {
	private String filebase; 
	private Corpus corpus;
	private LDAParameter param;
	private int V;

	private CokusRandom rand;

	private int[][] nmk; // size: M x K
	private int[][] nkt; // size: K x V
	private int[] nk; // size: K

	private int[][] z;

	private double Kalpha; // K*alpha
	private double Vbeta; // V*beta
	
	public LDAEstimator(final String filebase, final long seed, final LDAParameter param) {
		this.filebase = filebase;

		this.corpus = new Corpus(filebase + ".corpus");
		this.param = param;
		this.V = corpus.getNumTerms();

		this.Kalpha = param.getNTopics() * param.getAlpha();
		this.Vbeta = V * param.getBeta();

		this.rand = new CokusRandom(seed);
	}
	
	public LDAEstimator(final String filebase, final long seed) {
		this(filebase, seed, new LDAParameter());
	}
	
	public LDAParameter getParameter() {
		return this.param; 
	}

	@Override
	public void init() {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		
		// allocate
		nmk = new int[M][K];
		nkt = new int[K][V];
		nk = new int[K];
		z = new int[M][];
		// initialize
		for (int m = 0; m < M; m++) {
			z[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				final int k = rand.nextInt(K);
				z[m][n] = k;
				nmk[m][k]++;
				nkt[k][w[m][n]]++;
				nk[k]++;
			}
		}
	}

	@Override
	public void estimate(final int niter) {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		final double alpha = param.getAlpha(); 
		final double beta = param.getBeta(); 
		
		double[] pp = new double[K];
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				for (int n = 0; n < w[m].length; n++) {
					final int k = z[m][n];
					final int t = w[m][n];
					
					// decrement
					nmk[m][k]--;
					nkt[k][t]--;
					nk[k]--;
					
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < K; kk++) {
						pp[kk] = (nmk[m][kk] + alpha) * //
								(nkt[kk][t] + beta) / (nk[kk] + Vbeta);
						psum += pp[kk];
					}
					
					// sample
					final int kk = rand.nextDiscrete(pp, psum); 
					
					// reassign and increment
					this.z[m][n] = kk;
					this.nmk[m][kk]++;
					this.nkt[kk][t]++;
					this.nk[kk]++;
				} // n
			} // m
		} // i
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

	/*
	 * @flag = true: log
	 * */
	public double[][] getVarphi(final boolean flag) {
		final int K = param.getNTopics(); 
		final double beta = param.getBeta(); 
		
		double[][] varphi = new double[K][V]; 
		for (int k = 0; k < K; k++) {
			for (int v = 0; v < V; v++) {
				varphi[k][v] = (nkt[k][v] + beta) / (nk[k] + Vbeta); 
				
				if (flag) {
					varphi[k][v] = Math.log(varphi[k][v]); 
					if (Double.isNaN(varphi[k][v])) {
						System.err.println("The varphi[" +  k + "][" + v + "] is NaN.");
						System.exit(-1);
					}
				}
			}
		}
		
		return varphi; 
	}
	
	/*
	 * @flag = true: log
	 * */
	public double getVarphi(final int k, final int v, final boolean flag) {
		final double beta = param.getBeta(); 
		
		double varphi = (nkt[k][v] + beta) / (nk[k] + Vbeta); 
		if (flag) {
			varphi = Math.log(varphi); 
			if (Double.isNaN(varphi)) {
				System.err.println("The varphi[" +  k + "][" + v + "] is NaN.");
				System.exit(-1);
			}
		}
		
		return varphi; 
	}
	
	public void printTopWords(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTopWords(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printTopWords(final PrintWriter pw) {
		final CorpusResolver cr = new CorpusResolver(filebase);
		final int K = param.getNTopics();
		
		for (int k = 0; k < K; k++) {
			IntegerDoublePair[] unigrams = new IntegerDoublePair[V];
			for (int v = 0; v < V; v++) {
				unigrams[v] = new IntegerDoublePair(v, (double)nkt[k][v]); 
			}
			Arrays.sort(unigrams); 
			
			pw.write("Topic " + k + "th:\n"); 
			for (int i = 0; i < Math.min(param.getTwords(), V); i++) {
				pw.write("\t" + cr.getTerm(unigrams[i].getFirst()) + "\t" + unigrams[i].getSecond() / nk[k] + "\t"); 
				
				pw.write("\n"); 
			}
		}
	}
	
	public void printTopDocs(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTopDocs(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	} 
	
	public void printTopDocs(final PrintWriter pw) {
		final CorpusResolver cr = new CorpusResolver(filebase); 
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		
		for (int k = 0; k < K; k++) {
			IntegerDoublePair[] docs = new IntegerDoublePair[M];
			for (int m = 0; m < M; m++) {
				docs[m] = new IntegerDoublePair(m, (double)nmk[m][k] / w[m].length); 
			}
			Arrays.sort(docs); 
			
			pw.write("Topic " + k + "th:\n"); 
			for (int i = 0; i < Math.min(M, param.getTdocs()); i++) {
				pw.write("\t" + cr.getDoc(docs[i].getFirst()) + "\t" + docs[i].getSecond() + "\n");
			}
		}
	}
	
	public void printVartheta(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printVartheta(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	public void printVartheta(final PrintWriter pw) {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		final double alpha = param.getAlpha();
		
		for (int m = 0; m < M; m++) {
			final int Nm = corpus.getDocWords(m).length;
			for (int k = 0; k < K; k++) {
				if (nmk[m][k] != 0) {
					final double vartheta = (nmk[m][k] + alpha) / (Nm + Kalpha);
					pw.println(m + " " + k + " " + nmk[m][k] + " " + Nm + " " + vartheta); 
				}
			}
		}
	} 
	
	public void printVarphi(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printVarphi(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	public void printVarphi(final PrintWriter pw) {
		final int K = param.getNTopics(); 
		final double beta = param.getBeta(); 
		
		for (int k = 0; k < K; k++) {
			for (int v = 0; v < V; v++) {
				if (nkt[k][v] != 0) {
					final double varphi = (nkt[k][v] + beta) / (nk[k] + Vbeta); 
					pw.println(k + " " + v + " " + nkt[k][v] + " " + nk[k] + " " + varphi); 
				}
			}
		}
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
