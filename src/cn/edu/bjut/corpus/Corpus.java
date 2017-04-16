package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import cn.edu.bjut.utils.CokusRandom;
import cn.edu.bjut.utils.RandomSamplers;

public class Corpus implements ICorpus, ISplitCorpus {

	protected Document[] docs;
	protected int numTerms;

	private int readLimit = -1;

	protected int nfold;
	// permutation of the corpus used for splitting
	protected int[] perm;
	// starting points of the corpus segments
	protected int[] starts;
	protected int[][] origDocIds;

	public Corpus() {
		this.docs = null;
	}

	public Corpus(final int M) {
		this.docs = new Document[M];
	}

	public Corpus(final String fname) {
		read(fname);
	}

	public Corpus(final String fname, final int readLimit) {
		this.readLimit = readLimit;
		read(fname);
	}

	public Corpus(final Document[] docs, int numTerms) {
		this.docs = docs;
		this.numTerms = numTerms;
	}

	public void read(final String fname) {
		List<Document> docsList = new ArrayList<Document>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));

			// read corpus
			int nd = 0, nt = 0;
			for (String line; (line = reader.readLine()) != null;) {
				StringTokenizer tknr = new StringTokenizer(line, " \t\r\n");

				final int N = tknr.countTokens();
				Vector<Integer> words = new Vector<Integer>();

				for (int n = 0; n < N; n++) {
					final int w = Integer.parseInt(tknr.nextToken());
					words.add(w);
					if (w >= nt) {
						nt = w + 1;
					}
				} // end for each word

				docsList.add(new Document(words));

				if (nd % 1000 == 0) {
					System.out.println(nd);
				}
				nd++;

				// stop if read limit reached
				if (this.readLimit >= 0 && nd >= this.readLimit) {
					break;
				}
			} // end for each document

			reader.close();
			this.docs = docsList.toArray(new Document[docsList.size()]);
			this.numTerms = nt;
		} catch (Exception e) {
			System.err.println("Error while reading corpus:" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setDoc(final Document doc, final int idx) {
		if (idx < 0 || idx >= docs.length) {
			throw new IndexOutOfBoundsException("idx: " + idx);
		}

		docs[idx] = doc;
	}

	public Document[] getDocs() {
		return this.docs;
	}

	public Document getDoc(final int idx) {
		return this.docs[idx];
	}

	@Override
	public int getNumTerms() {
		return this.numTerms;
	}

	@Override
	public int getNumDocs() {
		return this.docs.length;
	}

	@Override
	public int getNumWords() {
		int count = 0;

		for (int m = 0; m < docs.length; m++) {
			count += docs[m].getWords().length;
		}

		return count;
	}

	@Override
	public int getNumWords(final int m) {
		if (m < 0 || m >= docs.length) {
			throw new IndexOutOfBoundsException("m: " + m);
		}

		return docs[m].getWords().length;
	}

	@Override
	public int[][] getDocWords() {
		int[][] words = new int[docs.length][];

		for (int m = 0; m < docs.length; m++) {
			words[m] = docs[m].getWords();
		}

		return words;
	}

	@Override
	public int[] getDocWords(final int m) {
		if (m < 0 || m >= docs.length) {
			throw new IndexOutOfBoundsException("m: " + m);
		}

		return docs[m].getWords();
	}

	public void setDocs(Document[] documents) {
		this.docs = documents;
	}

	@Override
	public void split(final int nfold, final long seed) {
		final int M = docs.length;
		this.nfold = nfold;

		Random rand = new CokusRandom(seed);
		RandomSamplers rs = new RandomSamplers(rand);
		perm = rs.randPerm(M);
		starts = new int[nfold + 1];

		for (int v = 0; v <= nfold; v++) {
			starts[v] = Math.round(M * (v / (float) nfold));
		}
	}

	@Override
	public ICorpus getTrainCorpus(final int split) {
		assert split < nfold && split >= 0; 
		
		final int testM = starts[split + 1] - starts[split];
		Document[] trainDocs = new Document[docs.length - testM];
		
		// before test split
		for (int m = 0; m < starts[split]; m++) {
			trainDocs[m] = docs[perm[m]];
		}
		
		// after test split
		for (int m = starts[split + 1]; m < docs.length; m++) {
			trainDocs[m] = docs[perm[m]];
		}
		
		return new Corpus(trainDocs, numTerms);
	}

	@Override
	public ICorpus getTestCorpus(final int split) {
		assert split < nfold && split >= 0;

		final int testM = starts[split + 1] - starts[split];
		Document[] testDocs = new Document[testM];

		for (int m = starts[split]; m < starts[split + 1]; m++) {
			testDocs[m] = docs[perm[m]];
			origDocIds[1][m] = perm[m];
		}

		return new Corpus(testDocs, numTerms);
	}

	@Override
	public int[][] getOrigDocIds(final int split) {
		final int testM = starts[split + 1] - starts[split];
		
		origDocIds = new int[2][]; 
		origDocIds[0] = new int[docs.length - testM]; // 0: train corpus; 
		origDocIds[1] = new int[testM]; // 1: test corpus
		
		int mtrain = 0;
		// before test split
		for (int m = 0; m < starts[split]; m++) {
			origDocIds[0][mtrain] = perm[m];
			mtrain++;
		}

		// after test split
		for (int m = starts[split + 1]; m < docs.length; m++) {
			origDocIds[0][mtrain] = perm[m];
			mtrain++;
		}
		
		// test split
		for (int m = starts[split]; m < starts[split + 1]; m++) {
			origDocIds[1][m - starts[split]] = perm[m];
		}
		
		return origDocIds;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Corpus {numDocs = " + docs.length + ", numTerms = " + numTerms + "}");

		return b.toString();
	}
}
