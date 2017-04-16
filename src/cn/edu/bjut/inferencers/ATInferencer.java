package cn.edu.bjut.inferencers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cn.edu.bjut.corpus.ILabelCorpus;
import cn.edu.bjut.corpus.LabelCorpus;
import cn.edu.bjut.estimators.ATEstimator;
import cn.edu.bjut.parameters.ATParameter;
import cn.edu.bjut.utils.CokusRandom;

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
public class ATInferencer implements IInferencer, IPerplexity {
	private LabelCorpus corpus;
	private ATParameter param;
	private ATEstimator estimator;

	private CokusRandom rand;

	private int[][] z;
	private int[][] x; 

	public ATInferencer(final String filebase, final long seed, final ATEstimator estimator) {
		this.estimator = estimator;
		this.param = estimator.getParameter();
		this.corpus = new LabelCorpus(filebase);

		this.rand = new CokusRandom(seed);
	}

	@Override
	public void init() {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		final int[][] a = corpus.getDocLabels(ILabelCorpus.LAUTHORS); 
		
		// allocate
		z = new int[M][];
		x = new int[M][];
		
		// initialize
		for (int m = 0; m < M; m++) {
			z[m] = new int[w[m].length];
			x[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				final int k = rand.nextInt(K);
				final int idx = rand.nextInt(a[m].length);
				final int i = a[m][idx];
				
				z[m][n] = k;
				x[m][n] = i;
			}
		}
	}

	@Override
	public void inference(int niter) {
		final int M = corpus.getNumDocs(); 
		final int K = param.getNTopics(); 
		final int[][] w = corpus.getDocWords(); 
		final int[][] a = corpus.getDocLabels(ILabelCorpus.LAUTHORS); 
		
		final double[][] varphi = estimator.getVarphi(false);
		final double[][] vartheta = estimator.getVartheta(false); 
		
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				double[] pp = new double[a[m].length * K]; 
				
				for (int n = 0; n < w[m].length; n++) {
					final int t = w[m][n];
					
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < K; kk++) {
						for (int ii = 0; ii < a[m].length; ii++) {
							final int idx = kk*a[m].length + ii; 
							final int aid = a[m][ii]; 
							pp[idx] = vartheta[aid][kk] * varphi[kk][t]; 
							psum += pp[idx];
						}
					}
					
					// sample
					final int idx = rand.nextDiscrete(pp, psum); 
					
					final int kk = idx / a[m].length; 
					final int ii = a[m][idx % a[m].length]; 
					
					// reassign
					z[m][n] = kk;
					x[m][n] = ii; 
				} // n
			} // m
		} // iter
	}

	@Override
	public double ppx() {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		final int[][] a = corpus.getDocLabels(ILabelCorpus.LAUTHORS); 
		
		final double[][] varphi = estimator.getVarphi(false);
		final double[][] vartheta = estimator.getVartheta(false); 
		
		double loglik = 0;
		int W = 0; 
		for (int m = 0; m < M; m++) {
			final int[] w = corpus.getDocWords(m); 
			final int Nm = w.length; 

			for (int n = 0; n < w.length; n++) {
				double sum = 0;
				for (int k = 0; k < K; k++) {
					for (int i = 0; i < a[m].length; i++) {
						sum += varphi[k][w[n]] * vartheta[a[m][i]][k]; 
					}
				}
				loglik += Math.log(sum);
			}
			loglik -=  Nm * Math.log(a[m].length); 
			
			W += Nm; 
		}
		
		return Math.exp(-loglik / W);
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
				pw.print(w[n] + ":" + z[m][n] + ":" + x[m][n] + " ");
			}
			pw.println();
		}
	}

}
