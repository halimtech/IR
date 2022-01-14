package com.example.restservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

public class FifthTask {

    public static final String FILES_TO_INDEX_DIRECTORY = "C:\\Users\\halee\\Downloads\\bigman\\a";

    static int docnum=0;
    static FieldType Main=new FieldType();
    static FieldType Topic=new FieldType();
    static Directory index;
    static String pathRead;
    static String pathWrite;

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
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
//                .addTokenFilter("stop", "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
                .addTokenFilter("porterstem")
                .build();
        /*
         * add config as parameter
         *
         * if stem then
         * else if lower then
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
        IndexWriter writer = new IndexWriter(index, config); // making IndexWriter to add document to the index

        return writer;
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
        File dir = new File(pathWrite);
        File[] files = dir.listFiles();
        for(File file: files)
            file.delete();

        setDirectory(path, pathRead);
        IndexWriter writer = CreateWriter(analyzer);
        addFilesToIndex(writer);

    }

    public static void query(Analyzer analyzer,String querys,int x) throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(index);	//reader to read the index



        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity());
        String queryString = querys;
        //from user
        QueryParser parser = new QueryParser("Main", analyzer);
        Query query = parser.parse(queryString);

        TopScoreDocCollector docCollcetor = TopScoreDocCollector.create(x);
        searcher.search(query, docCollcetor);
        System.out.println("ranking: ");

        ScoreDoc[] docs = docCollcetor.topDocs().scoreDocs;
        for (int j = 0; j < docs.length && j < x; j++) {
            Document docu = searcher.doc(docs[j].doc);
            System.out.println("<"+ docs[j].score+ ">"+" : "+ "<"+ docu.get("Main")+">");
        }

        reader.close();



        /*
         * similiraty score method as parameter
         *
         * if BM25 then
         * else if cosin then
         */
    }

    public static void main(String[] args) throws IOException, ParseException {
        //from here

        Analyzer analyzer=analyzer();
        setDirectory("C:\\Users\\halee\\Downloads\\bigman\\b", "C:\\Users\\halee\\Downloads\\bigman\\a");
//from user
        IndexWriter writer = CreateWriter(analyzer);


        addFilesToIndex(writer);


        //to here      only the first time    if dir not empty



        //reIndex(analyzer,"C:\\Users\\moaya\\Downloads\\P05_additional_resources\\b"); //if user wants (1st crwling using python with add files and then tthis with reindex) //path is also from user


        String query="Football world cup";
//from user
        query(analyzer, query,20);
//number also from user

        File dir = new File(pathWrite);
        File[] files = dir.listFiles();
        for(File file: files)
            file.delete();
//remove
    }
}
