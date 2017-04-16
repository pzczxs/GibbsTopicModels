package cn.edu.bjut.estimators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import cn.edu.bjut.corpus.CorpusResolver;
import cn.edu.bjut.corpus.ILabelCorpus;
import cn.edu.bjut.corpus.LabelCorpus;
import cn.edu.bjut.parameters.ATParameter;
import cn.edu.bjut.utils.CokusRandom;
import cn.edu.bjut.utils.IntegerDoublePair;

/*
 * Gibbs sampling algorithm for Author-Topic (AT) model. 
 * 
 * References: 
 * [1] Michal Rosen-Zvi, Thomas Griffiths, Mark Steyvers, and Padhraic Smyth, 2004. The 
 * Author-Topic Model for Authors and Documents. Proceedings of the 20th International 
 * Conference on Uncertainty in Artificial Intelligence. pp. 487-494.
 * [2] Mark Steyvers, Padhraic Smyth, and Thomas Griffiths, 2004. Probabilistic Author-Topic
 * Models for Information Discovery. Proceedings of the 10th ACM SIGKDD International 
 * Conference on Knowledge Discovery and Data Mining. pp. 306-315. 
 * [3] Michal Rosen-Zvi, Chaitanya Chemudugunta, Thomas Griffiths, and Padhraic Smyth, and 
 * Mark Steyvers, 2010. Learning Author-Topic Models from Text Corpora. ACM Transactions 
 * on Information Systems, Vol. 28, No. 1, pp. 1-38. 
 * 
 * Author: XU, Shuo (pzczxs@gmail.com)
 * */
public class ATEstimator implements IEstimator {
	private String filebase; 
	private LabelCorpus corpus;
	private ATParameter param;
	private int V;
	private int A; 

	private CokusRandom rand;

	private int[][] nak; // size: A x K
	private int[] na; // size: A
	private int[][] nkt; // size: K x V
	private int[] nk; // size: K

	private int[][] z;
	private int[][] x; 

	private double Kalpha; // K*alpha
	private double Vbeta; // V*beta
	
	public ATEstimator(final String filebase, final long seed, final ATParameter param) {
		this.filebase = filebase;

		this.corpus = new LabelCorpus(filebase);
		this.param = param;
		this.V = corpus.getNumTerms();
		this.A = corpus.getLabelsV(ILabelCorpus.LAUTHORS); 

		this.Kalpha = param.getNTopics() * param.getAlpha();
		this.Vbeta = V * param.getBeta();

		this.rand = new CokusRandom(seed);
	}
	
	public ATEstimator(final String filebase, final long seed) {
		this(filebase, seed, new ATParameter());
	}
	
	public ATParameter getParameter() {
		return this.param; 
	}

	@Override
	public void init() {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		final int[][] a = corpus.getDocLabels(ILabelCorpus.LAUTHORS); 
		
		// allocate
		nak = new int[A][K];
		na = new int[A];
		nkt = new int[K][V];
		nk = new int[K];
		z = new int[M][];
		x = new int[M][];
		
		// initialize
		for (int m = 0; m < M; m++) {
			z[m] = new int[w[m].length];
			x[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				final int t = w[m][n];
				final int k = rand.nextInt(K);
				final int idx = rand.nextInt(a[m].length);
				final int i = a[m][idx];
				
				z[m][n] = k;
				x[m][n] = i;
				nak[i][k]++;
				na[i]++;
				nkt[k][t]++;
				nk[k]++;
			}
		}
	}

	@Override
	public void estimate(final int niter) {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		final int[][] a = corpus.getDocLabels(ILabelCorpus.LAUTHORS); 
		final double alpha = param.getAlpha(); 
		final double beta = param.getBeta(); 
		
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				double[] pp = new double[a[m].length * K]; 
				
				for (int n = 0; n < w[m].length; n++) {
					final int k = z[m][n];
					final int t = w[m][n];
					final int i = x[m][n]; 
					
					// decrement
					nak[i][k]--;
					na[i]--; 
					nkt[k][t]--;
					nk[k]--;
					
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < K; kk++) {
						final double tmp = (nkt[kk][t] + beta) / (nk[kk] + Vbeta);
						
						for (int ii = 0; ii < a[m].length; ii++) {
							final int idx = kk*a[m].length + ii; 
							final int aid = a[m][ii]; 
							pp[idx] = (nak[aid][kk] + alpha) / (na[aid] + Kalpha) * tmp; 
							psum += pp[idx];
						}
					}
					
					// sample
					final int idx = rand.nextDiscrete(pp, psum); 
					
					final int kk = idx / a[m].length; 
					final int ii = a[m][idx % a[m].length]; 
					
					// reassign and increment
					z[m][n] = kk;
					x[m][n] = ii; 
					nak[ii][kk]++;
					na[ii]++; 
					nkt[kk][t]++;
					nk[kk]++;
				} // n
			} // m
		} // iter
	}
	
	/*
	 * @flag = true: log
	 * */
	public double[][] getVartheta(final boolean flag) {
		final int K = param.getNTopics();
		final double alpha = param.getAlpha();

		double[][] vartheta = new double[A][K];
		for (int i = 0; i < A; i++) {
			for (int k = 0; k < K; k++) {
				vartheta[i][k] = (nak[i][k] + alpha) / (na[i] + Kalpha);
				
				if (flag) {
					vartheta[i][k] = Math.log(vartheta[i][k]); 
					if (Double.isNaN(vartheta[i][k])) {
						System.err.println("The vartheta[" +  i + "][" + k + "] is NaN.");
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
	public double[] getVartheta(final int i, final boolean flag) {
		final int K = param.getNTopics();
		final double alpha = param.getAlpha();

		double[] vartheta = new double[K];
		for (int k = 0; k < K; k++) {
			vartheta[k] = (nak[i][k] + alpha) / (na[i] + Kalpha);
			
			if (flag) {
				vartheta[k] = Math.log(vartheta[k]); 
				if (Double.isNaN(vartheta[k])) {
					System.err.println("The vartheta[" +  i + "][" + k + "] is NaN.");
					System.exit(-1);
				}
			}
		}

		return vartheta;
	}
	
	/*
	 * @flag = true: log
	 * */
	public double getVartheta(final int i, final int k, final boolean flag) {
		final double alpha = param.getAlpha();

		double vartheta = (nak[i][k] + alpha) / (na[i] + Kalpha); 
		if (flag) {
			vartheta = Math.log(vartheta); 
			if (Double.isNaN(vartheta)) {
				System.err.println("The vartheta[" +  i + "][" + k + "] is NaN.");
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
	
	public void printTopAuthors(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTopAuthors(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	} 
	
	public void printTopAuthors(final PrintWriter pw) {
		final CorpusResolver cr = new CorpusResolver(filebase); 
		final int K = param.getNTopics(); 
		
		for (int k = 0; k < K; k++) {
			IntegerDoublePair[] authors = new IntegerDoublePair[A];
			for (int i = 0; i < A; i++) {
				authors[i] = new IntegerDoublePair(i, (double)nak[i][k] / na[i]); 
			}
			Arrays.sort(authors); 
			
			pw.write("Topic " + k + "th:\n"); 
			for (int i = 0; i < Math.min(A, param.getTauthors()); i++) {
				pw.write("\t" + cr.getAuthor(authors[i].getFirst()) + "\t" + authors[i].getSecond() + "\n");
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
		final int K = param.getNTopics();
		final double alpha = param.getAlpha();
		
		for (int i = 0; i < A; i++) {
			for (int k = 0; k < K; k++) {
				if (nak[i][k] != 0) {
					final double vartheta = (nak[i][k] + alpha) / (na[i] + Kalpha);
					pw.println(i + " " + k + " " + nak[i][k] + " " + na[i] + " " + vartheta); 
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
	
	/*
	 * topic distribution for each document
	 * 
	public void printTheta(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTheta(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	
	 * topic distribution for each document
	 * 
	public void printTheta(final PrintWriter pw) {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final double alpha = param.getAlpha();
		
		for (int m = 0; m < M; m++) {
			int[] nmk = new int[K];
			final int[] w = corpus.getDocWords(m); 
			
			for (int n = 0; n < w.length; n++) {
				nmk[z[m][n]]++; 
			}
			
			for (int k = 0; k < K; k++) {
				if (nmk[k] != 0) {
					final double theta = (nmk[k] + alpha) / (w.length + Kalpha); 
					pw.println(m + " " + k + " " + nmk[k] + " " + w.length + " " + theta); 
				}
			}
		}
	}*/

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
				pw.print(w[n] + ":" + z[m][n] + ":" + x[m][n] + " "); 
			}
			pw.println(); 
		}
	}
}
