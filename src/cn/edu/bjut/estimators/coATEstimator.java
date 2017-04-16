package cn.edu.bjut.estimators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import cn.edu.bjut.corpus.CorpusResolver;
import cn.edu.bjut.corpus.LabelCorpus;
import cn.edu.bjut.parameters.coATParameter;
import cn.edu.bjut.utils.CokusRandom;
import cn.edu.bjut.utils.IntegerDoublePair;
import cn.edu.bjut.utils.Relation;

/*
 * Gibbs sampling algorithm for co-Author-Topic (coAT) model. 
 * 
 * References: 
 * [1] Xin An, Shuo Xu, Yali Wen, and Mingxing Hu, 2014. A Shared Interest Discovery Model 
 * for Coauthor Relationship in SNS. International Journal of Distributed Sensor Networks, 
 * Vol. 2014, No. 820715, pp. 1-9. 
 * 
 * Author: XU, Shuo (pzczxs@gmail.com)
 * */
public class coATEstimator implements IEstimator {
	private String filebase;
	private LabelCorpus corpus;
	private coATParameter param;
	private int V;
	private int R;

	private CokusRandom rand;

	private int[][] nrk; // size: R x K
	private int[] nr; // size: R
	private int[][] nkt; // size: K x V
	private int[] nk; // size: K

	private int[][] z;
	private int[][] xy;

	private double Kalpha; // K*alpha
	private double Vbeta; // V*beta

	public coATEstimator(final String filebase, final long seed, final coATParameter param) {
		this.filebase = filebase;

		this.corpus = new LabelCorpus(filebase);
		this.param = param;
		this.V = corpus.getNumTerms();
		this.R = corpus.getCoauthorV();

		this.Kalpha = param.getNTopics() * param.getAlpha();
		this.Vbeta = V * param.getBeta();

		this.rand = new CokusRandom(seed);
	}

	public coATEstimator(final String filebase, final long seed) {
		this(filebase, seed, new coATParameter());
	}

	public coATParameter getParameter() {
		return this.param;
	}

	@Override
	public void init() {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		final int[][] w = corpus.getDocWords();
		final int[][] coauthor = corpus.getDocCoauthor();

		// allocate
		nrk = new int[R][K];
		nr = new int[R];
		nkt = new int[K][V];
		nk = new int[K];
		z = new int[M][];
		xy = new int[M][];
		// initialize
		for (int m = 0; m < M; m++) {
			if (coauthor[m].length == 0) {
				continue;
			}
			
			this.z[m] = new int[w[m].length];
			this.xy[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				final int t = w[m][n];
				final int k = rand.nextInt(K);
				final int idx = rand.nextInt(coauthor[m].length);
				final int r = coauthor[m][idx];
				
				z[m][n] = k;
				xy[m][n] = r;
				nrk[r][k]++;
				nr[r]++;
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
		final int[][] coauthor = corpus.getDocCoauthor();
		final double alpha = param.getAlpha();
		final double beta = param.getBeta();

		double[] pp = new double[K];
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				if (coauthor[m].length == 0) {
					continue;
				}

				double[] pr = new double[coauthor[m].length];

				for (int n = 0; n < w[m].length; n++) {
					// decrement
					final int k = z[m][n];
					final int t = w[m][n];
					final int r = xy[m][n];
					nrk[r][k]--;
					nr[r]--;
					nkt[k][t]--;
					nk[k]--;

					// compute weights for co-authors
					double rsum = 0;
					for (int rr = 0; rr < pr.length; rr++) {
						pr[rr] = (nrk[rr][k] + alpha) / (nr[rr] + Kalpha);
						rsum += pr[rr];
					}
					// sample for co-authors
					double u = this.rand.nextDouble() * rsum;
					rsum = 0;
					int rr = 0;
					for (rr = 0; rr < pr.length; rr++) {
						rsum += pr[rr];
						if (u <= rsum) {
							break;
						}
					}
					rr = coauthor[m][rr];

					// compute weights for topics
					double psum = 0;
					for (int kk = 0; kk < pp.length; kk++) {
						pp[kk] = (nrk[r][kk] + alpha) / (nr[r] + Kalpha) * (nkt[kk][t] + beta) / (nk[kk] + Vbeta);
						psum += pp[kk];
					}
					// sample for topics
					u = this.rand.nextDouble() * psum;
					psum = 0;
					int kk = 0;
					for (kk = 0; kk < pp.length; kk++) {
						psum += pp[kk];
						if (u <= psum) {
							break;
						}
					}

					// reassign and increment
					z[m][n] = kk;
					xy[m][n] = rr;
					nrk[rr][kk]++;
					nr[rr]++;
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

		double[][] vartheta = new double[R][K];
		for (int i = 0; i < R; i++) {
			for (int k = 0; k < K; k++) {
				vartheta[i][k] = (nrk[i][k] + alpha) / (nr[i] + Kalpha);
				
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
			vartheta[k] = (nrk[i][k] + alpha) / (nr[i] + Kalpha);
			
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

		double vartheta = (nrk[i][k] + alpha) / (nr[i] + Kalpha); 
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
	
	public void printTopCoauthors(final File f) {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(f));
			printTopCoauthors(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	} 
	
	public void printTopCoauthors(final PrintWriter pw) {
		final CorpusResolver cr = new CorpusResolver(filebase); 
		final int K = param.getNTopics(); 
		
		for (int k = 0; k < K; k++) {
			IntegerDoublePair[] coauthors = new IntegerDoublePair[R];
			for (int i = 0; i < R; i++) {
				coauthors[i] = new IntegerDoublePair(i, (double)nrk[i][k] / nr[i]); 
			}
			Arrays.sort(coauthors); 
			
			pw.write("Topic " + k + "th:\n"); 
			for (int i = 0; i < Math.min(R, param.getTcoauthors()); i++) {
				final Relation<Integer> r = corpus.getCoauthor(coauthors[i].getFirst()); 
				pw.write("\t(" + cr.getAuthor(r.getFirst()) + "," + cr.getAuthor(r.getSecond()) + ")\t" + coauthors[i].getSecond() + "\n");
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
		
		for (int i = 0; i < R; i++) {
			for (int k = 0; k < K; k++) {
				if (nrk[i][k] != 0) {
					final double vartheta = (nrk[i][k] + alpha) / (nr[i] + Kalpha);
					pw.println(i + " " + k + " " + nrk[i][k] + " " + nr[i] + " " + vartheta); 
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
			final int[] coauthor = corpus.getDocCoauthor(m); 
			if (coauthor.length == 0) {
				pw.println();
				continue; 
			}
			
			final int[] w = corpus.getDocWords(m);
			for (int n = 0; n < w.length; n++) {
				pw.print(w[n] + ":" + z[m][n] + ":" + corpus.getCoauthor(xy[m][n]) + " ");
			}
			pw.println();
		}
	}

}
