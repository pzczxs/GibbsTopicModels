package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.bjut.utils.Relation;

public class LabelCorpus extends Corpus implements ILabelCorpus {

	public static final String[] EXTENSIONS = { ".authors", ".labels", ".pubs", ".refs", ".tags", ".years" };

	/**
	 * array of labels. Elements are filled as soon as readlabels is called.
	 */
	private int[][][] labels;
	/**
	 * total count of labels
	 */
	private int[] labelsW;
	/**
	 * total range of labels
	 */
	private int[] labelsV;

	private String dataFilebase = null;

	private int[][] coauthor;
	private Map<Relation<Integer>, Integer> coauthor2id;
	private Map<Integer, Relation<Integer>> id2coauthor;
	private Map<Relation<Integer>, List<Integer>> coauthorDocs;

	/**
	 * 
	 */
	public LabelCorpus() {
		super();
		init();
	}

	/**
	 * @param dataFilebase
	 *            (filename without extension)
	 */
	public LabelCorpus(String dataFilebase) {
		super(dataFilebase + ".corpus");
		this.dataFilebase = dataFilebase;
		init();
	}

	/**
	 * @param dataFilebase
	 *            (filename without extension)
	 * @param readlimit
	 *            number of docs to reduce corpus when reading (-1 = unlimited)
	 */
	public LabelCorpus(String dataFilebase, int readlimit) {
		super(dataFilebase + ".corpus", readlimit);
		this.dataFilebase = dataFilebase;
		init();
	}

	/**
	 * create label corpus from standard one
	 * 
	 * @param corp
	 */
	public LabelCorpus(Corpus corp) {
		this.docs = corp.docs;
		this.numTerms = corp.numTerms;
		init();
	}

	protected void init() {
		this.labels = new int[EXTENSIONS.length][][];
		this.labelsW = new int[EXTENSIONS.length];
		this.labelsV = new int[EXTENSIONS.length];
	}

	/**
	 * loads and returns the document labels of given kind
	 */
	// @Override
	public int[][] getDocLabels(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		return this.labels[kind];
	}

	private void buildCoauthor() {
		int[][] a = getDocLabels(ILabelCorpus.LAUTHORS);

		this.coauthor2id = new HashMap<Relation<Integer>, Integer>();
		this.id2coauthor = new HashMap<Integer, Relation<Integer>>();
		this.coauthor = new int[a.length][];
		this.coauthorDocs = new HashMap<Relation<Integer>, List<Integer>>();

		int count = 0;
		for (int m = 0; m < a.length; m++) {
			int Am = a[m].length;
			this.coauthor[m] = new int[Am * (Am - 1) / 2];
			int rm = 0;
			for (int i = 0; i < Am; i++) {
				for (int j = i + 1; j < Am; j++) {
					Relation<Integer> r;
					if (a[m][i] < a[m][j]) {
						r = new Relation<Integer>(a[m][i], a[m][j]);
					} else {
						r = new Relation<Integer>(a[m][j], a[m][i]);
					}

					int id;
					if (this.coauthor2id.containsKey(r)) {
						id = this.coauthor2id.get(r);

						List<Integer> docs = this.coauthorDocs.get(r);
						docs.add(m);
						this.coauthorDocs.put(r, docs);
					} else {
						id = count++;
						this.coauthor2id.put(r, id);
						this.id2coauthor.put(id, r);
						List<Integer> docs = new ArrayList<Integer>();
						docs.add(m);
						this.coauthorDocs.put(r, docs);
					}

					this.coauthor[m][rm++] = id;
				}
			}
		}
	}

	/**
	 * builds and returns the co-author
	 */
	public int[][] getDocCoauthor() {
		if (this.coauthor == null) {
			buildCoauthor();
		}

		return this.coauthor;
	}
	
	public int[] getDocCoauthor(final int m) {
		if (m < 0 || m >= docs.length) {
			throw new IndexOutOfBoundsException("m: " + m);
		}
		
		if (this.coauthor == null) {
			buildCoauthor();
		}

		return this.coauthor[m];
	}

	/**
	 * returns the number of unique coauthor
	 */
	public int getCoauthorV() {
		if (this.coauthor == null) {
			buildCoauthor();
		}

		return this.coauthor2id.size();
	}

	/**
	 * return the number of coauthor
	 * 
	 * @return
	 */
	public int getCoauthorW() {
		if (this.coauthor == null) {
			buildCoauthor();
		}

		int W = 0;
		for (int m = 0; m < this.coauthor.length; m++) {
			W += this.coauthor[m].length;
		}

		return W;
	}

	/**
	 * resolve the numeric coauthor id
	 * 
	 * @param r
	 * @return
	 */
	public Relation<Integer> getCoauthor(int r) {
		if (this.coauthor == null) {
			buildCoauthor();
		}

		return this.id2coauthor.get(r);
	}

	/**
	 * return the co-authored documents by the coauthor id r
	 * 
	 * @param r
	 * @return
	 */
	public List<Integer> getCoauthorDocs(int r) {
		if (this.coauthor == null) {
			buildCoauthor();
		}

		Relation<Integer> coauthor = this.id2coauthor.get(r);

		return this.coauthorDocs.get(coauthor);
	}

	/**
	 * return the co-authored documents by the coauthor r
	 * 
	 * @param r
	 * @return
	 */
	public List<Integer> getCoauthorDocs(Relation<Integer> r) {
		if (this.coauthor == null) {
			buildCoauthor();
		}

		return this.coauthorDocs.get(r);
	}

	public Map<Integer, Relation<Integer>> getId2Coauthor() {
		return this.id2coauthor;
	}

	/**
	 * return the maximum number of labels in any document
	 * 
	 * @param kind
	 * @return
	 */
	public int getLabelsMaxN(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		int max = 0;
		for (int m = 0; m < this.labels[kind].length; m++) {
			max = Math.max(this.labels[kind][m].length, max);
		}

		return max;
	}

	// @Override
	public int getLabelsW(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		return this.labelsW[kind];
	}

	// @Override
	public int getLabelsV(int kind) {
		if (this.labels[kind] == null) {
			readLabels(kind);
		}

		return this.labelsV[kind];
	}

	/**
	 * read a label file with one line per document and associated labels
	 * 
	 * @param kind
	 * @return
	 */
	private void readLabels(int kind) {
		List<int[]> data = new ArrayList<int[]>();
		int W = 0;
		int V = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.dataFilebase + EXTENSIONS[kind]));
			for (String line; (line = br.readLine()) != null;) {
				line = line.trim();
				if (line.length() == 0) {
					data.add(new int[0]);
					continue;
				}
				String[] parts = line.split(" ");
				int[] a = new int[parts.length];
				for (int i = 0; i < parts.length; i++) {
					a[i] = Integer.parseInt(parts[i].trim());
					if (a[i] >= V) {
						V = a[i] + 1;
					}
				}
				W += a.length;
				data.add(a);
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.labels[kind] = data.toArray(new int[0][0]);
		this.labelsW[kind] = W;
		this.labelsV[kind] = V;
	}

	@Override
	public ICorpus getTrainCorpus(final int split) {
		final int trainM = starts[split + 1] - starts[split];

		int[][][] trainLabels = new int[EXTENSIONS.length][trainM][];
		int[] trainLabelsW = new int[EXTENSIONS.length];
		
		// for each label type
		for (int type = 0; type < EXTENSIONS.length; type++) {
			if (this.labels[type] == null) {
				continue;
			}
			
			int mtrain = 0;
			// before test split
			for (int m = 0; m < starts[split]; m++) {
				trainLabels[type][mtrain] = labels[type][perm[m]];
				trainLabelsW[type] += trainLabels[type][mtrain].length;
				mtrain++;
			}
			
			// after test split
			for (int m = starts[split + 1]; m < docs.length; m++) {
				trainLabels[type][mtrain] = this.labels[type][perm[m]];
				trainLabelsW[type] += trainLabels[type][mtrain].length;
				mtrain++;
			}
		}

		LabelCorpus trainCorpus = new LabelCorpus((Corpus) super.getTrainCorpus(split));
		trainCorpus.labels = trainLabels;
		trainCorpus.labelsV = labelsV;
		trainCorpus.labelsW = trainLabelsW;

		return trainCorpus;
	}

	@Override
	public ICorpus getTestCorpus(final int split) {
		final int testM = starts[split + 1] - starts[split];

		int[][][] testLabels = new int[EXTENSIONS.length][testM][];
		int[] testLabelsW = new int[EXTENSIONS.length];

		// for each label type
		for (int type = 0; type < EXTENSIONS.length; type++) {
			if (labels[type] == null) {
				continue;
			}

			for (int m = starts[split]; m < starts[split + 1]; m++) {
				testLabels[type][m - starts[split]] = labels[type][perm[m]];
				testLabelsW[type] += testLabels[type][m - starts[split]].length;
			}
		}

		LabelCorpus testCorpus = new LabelCorpus((Corpus) super.getTestCorpus(split));
		testCorpus.labels = testLabels;
		testCorpus.labelsV = labelsV;
		testCorpus.labelsW = testLabelsW;

		return testCorpus;
	}
}
