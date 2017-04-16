package cn.edu.bjut.inferencers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cn.edu.bjut.corpus.LabelCorpus;
import cn.edu.bjut.estimators.coATEstimator;
import cn.edu.bjut.parameters.coATParameter;
import cn.edu.bjut.utils.CokusRandom;

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
public class coATInferencer implements IInferencer, IPerplexity {
	private LabelCorpus corpus;
	private coATParameter param;
	private coATEstimator estimator;

	private CokusRandom rand;

	private int[][] z;
	private int[][] xy; 

	public coATInferencer(final String filebase, final long seed, final coATEstimator estimator) {
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
		final int[][] coauthor = corpus.getDocCoauthor();
		
		// allocate
		z = new int[M][];
		xy = new int[M][];
		
		// initialize
		for (int m = 0; m < M; m++) {
			if (coauthor[m].length == 0) {
				continue;
			}
			
			z[m] = new int[w[m].length];
			xy[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				final int k = rand.nextInt(K);
				int r = rand.nextInt(coauthor[m].length);
				r = coauthor[m][r];
				
				z[m][n] = k;
				xy[m][n] = r;
			}
		}
	}

	@Override
	public void inference(final int niter) {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		final int[][] w = corpus.getDocWords();
		final int[][] coauthor = corpus.getDocCoauthor();
		
		final double[][] varphi = estimator.getVarphi(false);
		final double[][] vartheta = estimator.getVartheta(false); 

		double[] pp = new double[K];
		for (int iter = 0; iter < niter; iter++) {
			System.out.println("iter: " + iter);
			for (int m = 0; m < M; m++) {
				if (coauthor[m].length == 0) {
					continue;
				}

				double[] pr = new double[coauthor[m].length];

				for (int n = 0; n < w[m].length; n++) {
					final int k = z[m][n];
					final int t = w[m][n];
					final int r = xy[m][n];

					// compute weights for co-authors
					double rsum = 0;
					for (int rr = 0; rr < pr.length; rr++) {
						pr[rr] = vartheta[rr][k]; 
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
						pp[kk] = vartheta[r][kk] * varphi[kk][t]; 
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

					// reassign
					z[m][n] = kk;
					xy[m][n] = rr;
				} // n
			} // m
		} // iter
	}
	
	@Override
	public double ppx() {
		final int M = corpus.getNumDocs();
		final int K = param.getNTopics();
		final int[][] coauthor = corpus.getDocCoauthor();
		
		final double[][] varphi = estimator.getVarphi(false);
		final double[][] vartheta = estimator.getVartheta(false); 
		
		double loglik = 0;
		int W = 0; 
		for (int m = 0; m < M; m++) {
			if (coauthor[m].length == 0) {
				continue; 
			}
			
			final int[] w = corpus.getDocWords(m); 
			final int Nm = w.length; 

			for (int n = 0; n < w.length; n++) {
				double sum = 0;
				for (int k = 0; k < K; k++) {
					for (int i = 0; i < coauthor[m].length; i++) {
						sum += varphi[k][w[n]] * vartheta[coauthor[m][i]][k]; 
					}
				}
				loglik += Math.log(sum);
			}
			loglik -=  Nm * Math.log(coauthor[m].length); 
			
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
