package cn.edu.bjut.ui;

import java.io.File;
import java.io.FileNotFoundException;

import cn.edu.bjut.estimators.coATEstimator;
import cn.edu.bjut.inferencers.coATInferencer;

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
public class coAT {
	public static void main(String[] args) throws FileNotFoundException {
		final String filebase = "data/nips/nips"; 
		
		final coATEstimator estimator = new coATEstimator(filebase, 56567651L); 
		estimator.init(); 
		estimator.estimate(5); 
		estimator.printVartheta(new File(filebase + ".coAT.vartheta"));
		estimator.printVarphi(new File(filebase + ".coAT.varphi"));
		estimator.printAssign(new File(filebase + ".coAT.assign")); 
		estimator.printTopWords(new File(filebase + ".coAT.twords"));
		estimator.printTopCoauthors(new File(filebase + ".coAT.tcoauthors"));
		
		final coATInferencer inferencer = new coATInferencer(filebase + ".test", 56567651L, estimator); 
		inferencer.init(); 
		inferencer.inference(3);
		inferencer.printAssign(new File(filebase + ".test.coAT.assign"));
		System.out.println("perplexity: " + inferencer.ppx());
		
		System.out.println("done."); 
	}
}
