package cn.edu.bjut.ui;

import java.io.File;
import java.io.FileNotFoundException;

import cn.edu.bjut.estimators.ATEstimator;
import cn.edu.bjut.inferencers.ATInferencer;

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
public class AT {
	public static void main(String[] args) throws FileNotFoundException {
		final String filebase = "data/nips/nips"; 
		
		final ATEstimator estimator = new ATEstimator(filebase, 56567651L); 
		estimator.init(); 
		estimator.estimate(10); 
		estimator.printVartheta(new File(filebase + ".AT.vartheta"));
		estimator.printVarphi(new File(filebase + ".AT.varphi"));
		estimator.printAssign(new File(filebase + ".AT.assign")); 
		estimator.printTopWords(new File(filebase + ".AT.twords"));
		estimator.printTopAuthors(new File(filebase + ".AT.tauthors"));
		
		final ATInferencer inferencer = new ATInferencer(filebase + ".test", 56567651L, estimator); 
		inferencer.init(); 
		inferencer.inference(3);
		inferencer.printAssign(new File(filebase + ".test.AT.assign"));
		System.out.println("perplexity: " + inferencer.ppx());
		
		System.out.println("done."); 
	}
}
