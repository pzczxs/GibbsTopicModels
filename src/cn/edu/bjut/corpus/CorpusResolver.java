package cn.edu.bjut.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CorpusResolver resolves indices into names.
 * 
 * @author gregor
 */
public class CorpusResolver {

	public final String[] EXTENSIONS = { "docs", "vocab", "authors.key", "labels.key", "pubs.key", "docnames" };

	private Map<String, Integer> termids = null;
	private Map<String, Integer> authorids = null; 
	private String[][] data = new String[EXTENSIONS.length][];
		
	private boolean parmode;

	public CorpusResolver(String filebase) {
		this(filebase, false);
	}

	/**
	 * control paragraph mode (possibly different vocabulary)
	 * 
	 * @param filebase
	 * @param parmode
	 */
	public CorpusResolver(String filebase, boolean parmode) {
		this.parmode = parmode;
		for (int i = 0; i < this.EXTENSIONS.length; i++) {
			String base = filebase;
			// read alternative vocabulary for paragraph mode
			if (this.parmode && this.EXTENSIONS[i].equals("vocab")) {
				base += ".par";
			}
			File f = new File(base + "." + this.EXTENSIONS[i]);
			if (f.exists()) {
				this.data[i] = load(f);
			}
		}
	}

	/**
	 * load from file removing every information after a = sign in
	 * each line
	 * 
	 * @param f
	 * @return array of label strings
	 */
	private String[] load(File f) {
		String[] strings = null;
		try {
			List<String> a = new ArrayList<String>();
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			for (String line = null; (line = br.readLine()) != null; ) {
				line = line.trim();
				int ii = line.indexOf('=');
				if (ii > -1) {
					a.add(line.substring(0, ii));
				} else {
					a.add(line);
				}
			}
			br.close();
			strings = a.toArray(new String[] {});
        
			return strings;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    
		return strings;
	}

	/**
	 * resolve the numeric term id
	 * 
	 * @param t
	 * @return
	 */
	public String getTerm(int t) {    
	    return (this.data[1] != null)? data[1][t]: null; 
	}

	/**
	 * find id for string term
	 * 
	 * @param term
	 * @return
	 */
	public int getTermId(String term) {
	    if (this.termids == null) {
	    	this.termids = new HashMap<String, Integer>();
	        for (int i = 0; i < this.data[1].length; i++) {
	        	this.termids.put(this.data[1][i], i);
	        }
	    }
	    
	    return this.termids.get(term);
	}

	/**
	 * resolve the numeric label id
	 * 
	 * @param i
	 * @return
	 */
	public String getLabel(int i) {    
	    return (this.data[3] != null)? this.data[3][i]: null; 
	}

	/**
	 * resolve the numeric author id
	 * 
	 * @param i
	 * @return
	 */
	public String getAuthor(int i) {   
	    return (this.data[2] != null)? this.data[2][i]: null; 
	}
	
	/**
	 * find id for string author
	 * 
	 * @param author
	 * @return
	 */
	public int getAuthorId(String author) {
		if (this.authorids == null) {
			this.authorids = new HashMap<String, Integer>(); 
			for (int i = 0; i < this.data[2].length; i++) {
				this.authorids.put(this.data[2][i], i); 
			}
		}
		
		return this.authorids.get(author); 
	}
	
	/**
	 * resolve the numeric term id
	 * 
	 * @param i
	 * @return
	 */
	public String getDoc(int i) {
	    return (this.data[0] != null)? this.data[0][i]: null; 
	}

	/**
	 * resolve the numeric term id
	 * 
	 * @param i
	 * @return
	 */
	public String getDocName(int i) {
	    return (this.data[5] != null)? this.data[5][i]: null; 
	}
	
	/**
	 * resolve the numeric volume id
	 * 
	 * @param i
	 * @return
	 */
	public String getPub(int i) {
	    return (this.data[4] != null)? this.data[4][i]: null; 
	}

	public String getLabel(int type, int id) {
	    if (type == LabelCorpus.LTERMS) {
	        return getTerm(id);
	    } else if (type == LabelCorpus.LAUTHORS) {
	        return getAuthor(id);
	    } else if (type == LabelCorpus.LCATEGORIES) {
	        return getLabel(id);
	    } else if (type == LabelCorpus.LPUBS) {
	        return getPub(id);
	    } else if (type == LabelCorpus.LREFERENCES) {
	        return null;
	    } else if (type == LabelCorpus.LDOCS) {
	        return getDoc(id);
	    }
	    
	    return null;
	}

	public int getId(int type, String label) {
	    if (type == LabelCorpus.LTERMS) {
	        return getTermId(label);
	    } else if (type == LabelCorpus.LAUTHORS) {
	        return getAuthorId(label);
	    }
	    return -1;
	}
	
	public static void main(String[] args) {
	    CorpusResolver cr = new CorpusResolver("nips/nips");
	    System.out.println(cr.getAuthor(2));
	    System.out.println(cr.getLabel(20));
	    System.out.println(cr.getDoc(501));
	    System.out.println(cr.getTerm(1));
	    System.out.println(cr.getTermId(cr.getTerm(1)));
	}
}
