package cn.edu.bjut.parameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class coATParameter {
	private int K;
	private double alpha;
	private double beta;
	private int twords;
	private int tcoauthors; 
	
	private final static int NTOPICS_DEFAULT = 100; 
	private final static double ALPHA_DEFAULT = 0.5; 
	private final static double BETA_DEFAULT = 0.01;  
	private final static int TWORDS_DEFAULT = 20; 
	private final static int TCOAUTHORS_DEFAULT = 20; 
	
	public coATParameter(final int K, final double alpha, final double beta, 
			final int twords, final int tcoauthors) {
		
		this.K = K; 
		this.alpha = alpha; 
		this.beta = beta; 
		this.twords = twords; 
		this.tcoauthors = tcoauthors; 
		
		check(); 
	}
	
	public coATParameter(final int K) {
		this(K, ALPHA_DEFAULT, BETA_DEFAULT, TWORDS_DEFAULT, TCOAUTHORS_DEFAULT); 
	}
	
	public coATParameter(final Properties properties) {
		loadConfigure(properties);
		
		check(); 
	}
	
	public coATParameter(final String fname) {
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(fname));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		
		loadConfigure(properties); 
		
		check(); 
	}
	
	public coATParameter() {
		this("conf/coAT.properties"); 
	}
	
	public int getNTopics() {
 		return this.K; 
 	}
 	
 	public void setNTopics(final int K) {
 		this.K = K; 
 	}
 	
 	public double getAlpha() {
 		return this.alpha; 
 	}
 	
 	public void setAlpha(final double alpha) {
 		this.alpha = alpha; 
 	}
 	
 	public double getBeta() {
 		return this.beta; 
 	}
 	
 	public void setBeta(final double beta) {
 		this.beta = beta; 
 	}
	
	public int getTwords() {
 		return this.twords; 
 	}
 	
 	public void setTwords(final int twords) {
 		this.twords = twords; 
 	}
 	
 	public int getTcoauthors() {
 		return this.tcoauthors; 
 	}
 	
 	public void setTcoauthors(final int tcoauthors) {
 		this.tcoauthors = tcoauthors; 
 	}
	
	private void loadConfigure(final Properties properties) {
		K = Integer.parseInt(properties.getProperty("ntopics", Integer.toString(NTOPICS_DEFAULT))); 
		alpha = Double.parseDouble(properties.getProperty("alpha", Double.toString(ALPHA_DEFAULT))); 
		beta = Double.parseDouble(properties.getProperty("beta", Double.toString(BETA_DEFAULT))); 
		twords = Integer.parseInt(properties.getProperty("twords", Integer.toString(TWORDS_DEFAULT))); 
		tcoauthors = Integer.parseInt(properties.getProperty("tcoauthors", Integer.toString(TCOAUTHORS_DEFAULT))); 
	} 

	private void check() {
		if (K <= 0) {
 			K = NTOPICS_DEFAULT; 
 		}
 		
 		if (alpha <= 0.0) {
			alpha = ALPHA_DEFAULT; 
		}
 		
		if (beta <= 0.0) {
			beta = BETA_DEFAULT; 
		}
		
		if (twords <= 0) {
			twords = TWORDS_DEFAULT; 
		}
		
		if (tcoauthors <= 0) {
			tcoauthors = TCOAUTHORS_DEFAULT; 
		}
 	}

	@Override
	public String toString() {
		return "coATParameter [K=" + K + ", alpha=" + alpha + ", beta=" + beta + 
				", twords=" + twords + ", tcoauthors=" + tcoauthors + "]";
	}
}
