package com.example.restservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import au.com.bytecode.opencsv.CSVWriter;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

public class FifthTask {

    //public static final String FILES_TO_INDEX_DIRECTORY = "C:\\Users\\moaya\\Downloads\\P05_additional_resources\\a";

    static int docnum=0;
    static FieldType Main=new FieldType();
    static FieldType Topic=new FieldType();
    static Directory index;
    static String pathRead;
    static String pathWrite;
    static IndexSearcher searcher;
    static IndexReader reader;
    static int[] docsid;
    static HashMap<String, Integer> clust;
    static ArrayList<String> clust1 = new ArrayList<>();
    static ArrayList<String> clust2 = new ArrayList<>();
    static ArrayList<String> clust3 = new ArrayList<>();
    static ArrayList<String> clust4 = new ArrayList<>();
    static ArrayList<String> clust5 = new ArrayList<>();
    static Similarity simType;
    static Analyzer analyzer;







    private static Set<String> terms = new HashSet<>();
    private static RealVector v1=null;
    private static RealVector v2=null;
    private static RealVector v3=null;
    private static RealVector v4=null;
    private static RealVector v5=null;
    private static RealVector v6=null;
    private static RealVector v7=null;
    private static RealVector v8=null;
    private static RealVector v9=null;
    private static RealVector v10=null;





    private static void addDoc(IndexWriter w, String content, String titel) throws IOException {
        Document doc = new Document();



        Main.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS); //enable storing the reuired statistics
        Main.setStored(true);
        Main.setStoreTermVectors(true);
        Main.setStoreTermVectorPositions(true);
        Main.setStoreTermVectorPayloads(true);
        Main.setStoreTermVectorOffsets(true);
        Topic.setStored(true);
        doc.add(new Field("Main", content, Main));
        doc.add(new Field("Topic", titel, Topic));
        w.addDocument(doc);
        docnum++;
    }


    public static Analyzer analyzer() throws IOException {
         analyzer = new EnglishAnalyzer();
        /*
		 * add config as parameter
		 *
		 * if stem then
		 * else if lower then
		 * 		Analyzer analyzer = CustomAnalyzer.builder()
				.withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter("stop", "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
				.addTokenFilter("porterstem")
                .build();

		 */
        return analyzer;
    }

    public static void setDirectory(String pathW,String pathR) {
        pathWrite=pathW;
        pathRead=pathR;
    }

    public static IndexWriter CreateWriter(Analyzer analyzer) throws IOException {
        index = FSDirectory.open(Paths.get(pathWrite)); //makes a new directory in the ram for storing the index
        //index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setSimilarity(simType);
        //config.setSimilarity(new ClassicSimilarity());
        IndexWriter writer = new IndexWriter(index, config); // making IndexWriter to add document to the index

        return writer;
    }

    public static void clearDirectory() {

        File dir = new File(pathWrite);
        File[] files = dir.listFiles();
        for(File file: files)
            file.delete();
    }


    public static void addFilesToIndex(IndexWriter writer) throws IOException {
        File dir = new File(pathRead);
        File[] files = dir.listFiles();
        String docMain="";
        String docTitel="";
        for (File file : files) {
            docMain="";
            docTitel="";
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();


            String line = br.readLine();
            while (line != null) {
                // reading lines until the end of the file
                sb.append(line).append("\n");
                line = br.readLine();
            }
            docMain = sb.toString();
            docTitel = file.getName().substring(0, file.getName().length()-4);
            br.close();

            addDoc(writer, docMain, docTitel);
        }
        writer.close();
    }

    public static void reIndex(Analyzer analyzer, String path) throws IOException {
        clearDirectory();

        setDirectory(path, pathRead);
        IndexWriter writer = CreateWriter(analyzer);
        addFilesToIndex(writer);

    }


    public static int[] query(Analyzer analyzer,String querys) throws IOException, ParseException {

        clust1.clear();
        clust2.clear();
        clust3.clear();
        clust4.clear();
        clust5.clear();

        reader = DirectoryReader.open(index);	//reader to read the index

        searcher = new IndexSearcher(reader);
        searcher.setSimilarity(simType);
        //searcher.setSimilarity(new ClassicSimilarity());
        String queryString = querys;
        //from user
        QueryParser parser = new QueryParser("Main", analyzer);
        Query query = parser.parse(queryString);

        TopScoreDocCollector docCollcetor = TopScoreDocCollector.create(10);
        searcher.search(query, docCollcetor);
        //System.out.println("ranking: ");

        ScoreDoc[] docs = docCollcetor.topDocs().scoreDocs;
        int[] docsid=new int[10];
        for (int j = 0; j < docs.length && j < 10; j++) {
//            Document docu = searcher.doc(docs[j].doc);
            docsid[j]=docs[j].doc;
//            System.out.println("<"+ docs[j].score+ ">"+" : "+ "<"+ docu.get("Topic")+">");
        }
        //reader.close();
        return docsid;


        /*
         * similiraty score method as parameter
         *
         * if BM25 then
         * else if cosin then
         */
    }

    public static void setSimType(String s) {
        if (s == "BM25") {
            simType = new BM25Similarity();
        } else {
            simType = new ClassicSimilarity();
        }
    }

    // IMPORTANT PAGE
    public static ArrayList<ArrayList<String>> clusterFront(String query,int x) throws Exception {

//from user
        docsid=query(analyzer, query);

        if(x<2||x>5) {
            x=2;
        }

        cluster(x);
        ArrayList<ArrayList<String>> z = new ArrayList();
        z.add(clust1);
        z.add(clust2);
        z.add(clust3);
        z.add(clust4);
        z.add(clust5);
        return z;
    }

    public static void button1continue(String s1, String s2) throws IOException {
        Analyzer analyzer=analyzer();
        setDirectory(s1, s2);
        setSimType("BM25");
        clearDirectory();
        IndexWriter writer = CreateWriter(analyzer);
        addFilesToIndex(writer);

    }
    public static void main(String[] args) throws Exception {
        //from here
        Analyzer analyzer=analyzer();
        setDirectory("C:\\Users\\halee\\Downloads\\bigman\\b", "C:\\Users\\halee\\Downloads\\bigman\\a");
        setSimType("BM25");
        clearDirectory();

//from user (dir or ram) but also default
        IndexWriter writer = CreateWriter(analyzer);


        addFilesToIndex(writer);


        //to here      only the first time    if dir not empty



        //reIndex(analyzer,pathWrite); //if user wants (1st crwling using python with add files and then tthis with reindex) //path is also from user


        String query="player";
//from user
        docsid=query(analyzer, query);
        /*
         * similiraty score method as parameter
         *
         * if BM25 then
         * else if cosin then
         */

        int x=3;

        if(x<2||x>5) {
            x=2;
        }

        cluster(x);  //shows the clusters
//num of clusters from user


        int y=1;
        listCluster(y);

        clearDirectory();//remove


        reader.close();


        //if a name was clicked then edirect to ("https://en.wikipedia.org/wiki/" + name.replaceAll("\\s","_"))

    }



    static RealVector getTermFrequencies(IndexReader reader, int docId)
            throws IOException {
        Terms vector = reader.getTermVector(docId, "Main");
        double n=reader.getDocCount("Main");
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator();
        Map<String, Integer> frequencies = new HashMap<>();
        RealVector rvector = new ArrayRealVector(terms.size());
        BytesRef text = null;
        ArrayList<Term> v=new ArrayList<Term>();
        ArrayList<Long> g=new ArrayList<Long>();
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            int freq = (int) termsEnum.totalTermFreq();
            Term termInstance = new Term("Main", term);
            frequencies.put(term, freq);
            v.add(termInstance);
            g.add(termsEnum.totalTermFreq());
        }
        int i = 0;
        //int j=0;
        double idf=0.0;
        double tf=0.0;
        double tfidf=0.0;
        for (String term1 : terms) {
            if(frequencies.containsKey(term1)) {
                Term termm = new Term("Main", term1);
                int index=v.indexOf(termm);
                Term termInstance=v.get(index);
                tf=g.get(index);
                double docCount = reader.docFreq(termInstance);
                double z=n/docCount;
                idf=Math.log10(z);
                tfidf=tf*idf;
            } else {
                tfidf=0.0;
            }
            rvector.setEntry(i++, tfidf);
        }
        return rvector;
    }


    static void addTerms(IndexReader reader, int docId) throws IOException {
        Terms vector = reader.getTermVector(docId, "Main");
        TermsEnum termsEnum = null;
        termsEnum = vector.iterator();
        BytesRef text = null;
        while ((text = termsEnum.next()) != null) {
            String term = text.utf8ToString();
            terms.add(term);
        }
    }


    public static void createCSV() throws IOException {
        addTerms(reader, docsid[0]);
        addTerms(reader, docsid[1]);
        addTerms(reader, docsid[2]);
        addTerms(reader, docsid[3]);
        addTerms(reader, docsid[4]);
        addTerms(reader, docsid[5]);
        addTerms(reader, docsid[6]);
        addTerms(reader, docsid[7]);
        addTerms(reader, docsid[8]);
        addTerms(reader, docsid[9]);



        v1 = getTermFrequencies(reader, docsid[0]);
        v2 = getTermFrequencies(reader, docsid[1]);
        v3 = getTermFrequencies(reader, docsid[2]);
        v4 = getTermFrequencies(reader, docsid[3]);
        v5 = getTermFrequencies(reader, docsid[4]);
        v6 = getTermFrequencies(reader, docsid[5]);
        v7 = getTermFrequencies(reader, docsid[6]);
        v8 = getTermFrequencies(reader, docsid[7]);
        v9 = getTermFrequencies(reader, docsid[8]);
        v10 = getTermFrequencies(reader, docsid[9]);

        double[] arr1 = v1.toArray();
        double[] arr2 = v2.toArray();
        double[] arr3 = v3.toArray();
        double[] arr4 = v4.toArray();
        double[] arr5 = v5.toArray();
        double[] arr6 = v6.toArray();
        double[] arr7 = v7.toArray();
        double[] arr8 = v8.toArray();
        double[] arr9 = v9.toArray();
        double[] arr10 = v10.toArray();

        String s1 = Arrays.toString(arr1);
        s1=s1.substring(1, s1.length()-1);
        String[] ss1 = s1.split(", ");

        String s2 = Arrays.toString(arr2);
        s2=s2.substring(1, s2.length()-1);
        String[] ss2 = s2.split(", ");

        String s3 = Arrays.toString(arr3);
        s3=s3.substring(1, s3.length()-1);
        String[] ss3 = s3.split(", ");

        String s4 = Arrays.toString(arr4);
        s4=s4.substring(1, s4.length()-1);
        String[] ss4 = s4.split(", ");

        String s5 = Arrays.toString(arr5);
        s5=s5.substring(1, s5.length()-1);
        String[] ss5 = s5.split(", ");

        String s6 = Arrays.toString(arr6);
        s6=s6.substring(1, s6.length()-1);
        String[] ss6 = s6.split(", ");

        String s7 = Arrays.toString(arr7);
        s7=s7.substring(1, s7.length()-1);
        String[] ss7 = s7.split(", ");

        String s8 = Arrays.toString(arr8);
        s8=s8.substring(1, s8.length()-1);
        String[] ss8 = s8.split(", ");

        String s9 = Arrays.toString(arr9);
        s9=s9.substring(1, s9.length()-1);
        String[] ss9 = s9.split(", ");

        String s10 = Arrays.toString(arr10);
        s10=s10.substring(1, s10.length()-1);
        String[] ss10 = s10.split(", ");






        File file2 = new File(pathWrite+"\\test.csv");
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file2);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer2 = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = new String[ss1.length];
            int a=0;
            for(int i=0;i<header.length;i++) {
                a=a+1;
                header[i]=String.valueOf(a);
            }
            writer2.writeNext(header);

            writer2.writeNext(ss1);
            writer2.writeNext(ss2);
            writer2.writeNext(ss3);
            writer2.writeNext(ss4);
            writer2.writeNext(ss5);
            writer2.writeNext(ss6);
            writer2.writeNext(ss7);
            writer2.writeNext(ss8);
            writer2.writeNext(ss9);
            writer2.writeNext(ss10);



            // closing writer connection
            writer2.close();
        }
        catch (IOException e) {

            e.printStackTrace();
        }






        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(pathWrite+"\\test.csv"));
        Instances data = loader.getDataSet();

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(pathWrite+"\\tests.arff"));
        saver.writeBatch();
    }



    public static void cluster(int x) throws Exception {
        createCSV();
        BufferedReader breader = null;
        breader = new BufferedReader(new FileReader(
                pathWrite+"\\tests.arff"));
        Instances Train = new Instances(breader);
        //Train.setClassIndex(Train.numAttributes() - 1); // comment out this line
        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setSeed(10);
        kMeans.setPreserveInstancesOrder(true);
        kMeans.setNumClusters(x);
        kMeans.buildClusterer(Train);
        int[] assignments = kMeans.getAssignments();
        int i2 = 0;
        clust = new HashMap<>();
        for (int clusterNum : assignments) {
            //System.out.printf("Instance %d -> Cluster %d", i2, clusterNum);

            Document docu = searcher.doc(docsid[i2]);
            clust.put(docu.get("Topic"), clusterNum);

            i2++;
        }
        breader.close();
        /*
        for (String name: clust.keySet()) {
            String key = name.toString();
            String value = clust.get(name).toString();
            System.out.println(key + " " + value);
        }
        */

        for (String name: clust.keySet()) {
            String key = name.toString();
            int value = clust.get(name);
            if(value==0) {
                clust1.add(key);
            }else if(value==1) {
                clust2.add(key);
            }else if(value==2) {
                clust3.add(key);
            }else if(value==3){
                clust4.add(key);
            }else if(value==4) {
                clust5.add(key);
            }
        }

    }

    public static ArrayList<String> listCluster(int y) throws IOException {
        ArrayList<String> z = new ArrayList<>();
        if(y==1) {//clust1 was clicked
            for(int id: docsid) {//list them
                String name = searcher.doc(id).get("Topic");
                if(clust1.contains(name)) {
                    z.add(name);
                }
            }
            return z;
        }else if(y==2) {
            for(int id: docsid) {//list them
                String name = searcher.doc(id).get("Topic");
                if(clust2.contains(name)) {
                    z.add(name);
                }
            }
            return z;
        }else if(y==3) {
            for(int id: docsid) {//list them
                String name = searcher.doc(id).get("Topic");
                if(clust3.contains(name)) {
                    z.add(name);
                }
            }
            return z;
        }else if(y==4) {
            for(int id: docsid) {//list them
                String name = searcher.doc(id).get("Topic");
                if(clust4.contains(name)) {
                    z.add(name);
                }
            }
            return z;
        }else if(y==5) {
            for(int id: docsid) {//list them
                String name = searcher.doc(id).get("Topic");
                if(clust5.contains(name)) {
                    z.add(name);
                }
            }
        }
        return z;
    }

}
