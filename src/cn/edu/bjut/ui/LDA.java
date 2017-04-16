package cn.edu.bjut.ui;

import java.io.File;
import java.io.FileNotFoundException;

import cn.edu.bjut.estimators.LDAEstimator;
import cn.edu.bjut.inferencers.LDAInferencer;

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
public class LDA {
	public static void main(String[] args) throws FileNotFoundException {
		final String filebase = "data/nips/nips"; 
		
		final LDAEstimator estimator = new LDAEstimator(filebase, 56567651L); 
		estimator.init(); 
		estimator.estimate(20); 
		estimator.printVartheta(new File(filebase + ".LDA.vartheta"));
		estimator.printVarphi(new File(filebase + ".LDA.varphi"));
		estimator.printAssign(new File(filebase + ".LDA.assign")); 
		estimator.printTopWords(new File(filebase + ".LDA.twords"));
		estimator.printTopDocs(new File(filebase + ".LDA.tdocs")); 
		
		final LDAInferencer inferencer = new LDAInferencer(filebase + ".test", 56567651L, estimator); 
		inferencer.init(); 
		inferencer.inference(3);
		inferencer.printAssign(new File(filebase + ".test.LDA.assign"));
		System.out.println("perplexity: " + inferencer.ppx()); 
		
		System.out.println("done."); 
	}
}
