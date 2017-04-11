import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TFIDFImplementation { 

	public static void main(String args[]) throws IOException, ClassNotFoundException 
	{
		Scanner scan=new Scanner(System.in);
		// Storing all possible files of a certain path in an array.


		// Change the path according to where the documents are present in the system.
		File folder = new File("/Users/kennymac/Documents/workspace/InformationRetrieval/k");
		File[] listOfFiles = folder.listFiles();
		// Creating an object for stemming Hindi words.
		HindiStemmer stemmer=new HindiStemmer();

		// Maintaining an Array list for words, their respective frequencies and the the document indexing.
		//Same index in all the three array lists correspond to the same element's word, frequency and set of documents in the indexing array list.
		ArrayList<String> words=new ArrayList<>();
		ArrayList<Integer> frequency=new ArrayList<>();
		ArrayList<HashSet<Integer>> Indexing=new ArrayList<>();

		ArrayList<HashMap<String, Double>> TF= new ArrayList<>();

		// Storing documents ID and name.
		ArrayList<Integer> documents=new ArrayList<>();
		ArrayList<String> documents_name=new ArrayList<>();
		int choice =0;



		while(choice!=1 && choice!=2 )
		{
			System.out.println("What do you want to do?");
			System.out.println("1. Give me the implementation of query 'आंख'!");
			System.out.println("2.Give me the Indexing instead!");
			System.out.println("1/2?");
			choice=scan.nextInt();
			if(choice!=1 && choice!=2 )
			{
				System.out.println("Enter a valid choice!");
				System.out.println();
			}
		}


		// Create a hash-set for the set of Stop words and store them in it using a function.
		HashSet<String> StopHash=new HashSet<>();  
		CreateStopWordsHash(StopHash, "HindiStopWords.txt");

		// For Giving an index to every document.
		int doc_id=1;

		for (int k = 0; k < listOfFiles.length; k++) 
		{
			//  Segregating the text files
			if (listOfFiles[k].isFile() && listOfFiles[k].toString().endsWith(".txt") && !listOfFiles[k].toString().equals("HindiStopWords.txt")) 
			{		 
				documents.add(doc_id);
				//System.out.println(doc_id);
				//			    	  		
				// Extracting the file name from path
				String fileName = listOfFiles[k].getName();

				// Linking document IDs with their filenames.
				documents_name.add(fileName);

				// Adds the word in Array list with frequency 1 if it doen't exist. 
				// Else It increases the count by one of corresponding index in frequency ArrayList.

				Entering(words, frequency, fileName, Indexing, doc_id, stemmer, StopHash, TF);		 
				doc_id++;
			}
		}

		int Number_of_documents=doc_id-1;
		String querystring= "आंख";


		if(choice==1)
		{
			WriteToFile(words, frequency);   // Writing the words and frequencies in a file.
			//ReadFromFile(words, frequency);
			ArrayList<ArrayList<Double>> tfidf= TFIDF(words, frequency, Indexing, TF, Number_of_documents);
			query(tfidf, querystring, words, documents);
		}

		else if(choice==2)
		{
			WriteInvertedIndex(words, Indexing);
			ReadInvertedIndex(words, Indexing);
		}

	}

	public static void CreateStopWordsHash(HashSet<String> StopHash, String file ) throws IOException 
	{
		String line = null;	    		  	              
		FileReader fileReader =  new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		while((line = bufferedReader.readLine()) != null) 
		{
			String str=line.toString();
			String string[]=str.split(" ");
			str=string[0];
			StopHash.add(str);
		}

		bufferedReader.close();

	}

	public static void query(ArrayList<ArrayList<Double>> tfidf, String querystring, ArrayList<String> words, ArrayList<Integer> documents) 
	{
		System.out.println("Ranked list for query " + querystring + " is: ");
		int index=-1;

		for(int i=0; i < words.size(); i++)
		{
			if(words.get(i).equals(querystring))
			{
				index=i;
			}
		}

		ArrayList<Double> Retrieved= new ArrayList<>();

		if(index==-1)
		{
			System.out.println("No search results found");
		}
		else
		{
			for(int i=0; i < tfidf.size(); i++)
			{
				Retrieved.add(tfidf.get(i).get(index));
				//System.out.println( "doc id " + (i+1) + " : " + tfidf.get(i).get(index) );
			}

			//Sorting 

			for(int j=0; j<Retrieved.size(); j++)
			{		
				int big_index=j;
				for(int i=j+1; i<Retrieved.size(); i++)
				{
					if(	Retrieved.get(i) > Retrieved.get(big_index))
					{
						big_index=i;
					}
				}
				Double temp=Retrieved.get(big_index);
				Retrieved.set(big_index, Retrieved.get(j));
				Retrieved.set(j, temp);

				int temp2=documents.get(big_index);
				documents.set(big_index, documents.get(j));
				documents.set(j, temp2);
			}

			for(int j=0; j<Retrieved.size(); j++)
			{
				System.out.println("Doc id: " + documents.get(j) + " Score: " + Retrieved.get(j));
			}

		}

	}
	public static void WriteInvertedIndex(ArrayList<String> words,ArrayList<HashSet<Integer>> Indexing) throws IOException 
	{


		FileOutputStream fos = new FileOutputStream("Indexing.tmp");
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(Indexing);

		oos.close();

		fos = new FileOutputStream("Words.tmp");
		oos = new ObjectOutputStream(fos);

		oos.writeObject(words);

		oos.close();




	}

	public static void ReadInvertedIndex(ArrayList<String> words,ArrayList<HashSet<Integer>> Indexing) throws IOException, ClassNotFoundException 
	{

		FileInputStream fos = new FileInputStream("random2.json");
		ObjectInputStream oos = new ObjectInputStream(fos);


		while(fos.available() > 0)			// Till End of file
		{
			System.out.print(oos.readObject());					
		}


		oos.close();

	}

	public static void Entering(ArrayList<String> words, ArrayList<Integer> frequency, String fileName, ArrayList<HashSet<Integer>> Indexing, int doc_id, HindiStemmer stemmer, HashSet<String> StopHash, ArrayList<HashMap<String, Double>> TF) throws IOException 
	{
		// We are creating the Indexing array.(The array which contains the document IDs in which the particular word is present)
		// Same index in Indexing and words ArrayList means the same term.

		String line = null;	

		FileReader fileReader= new FileReader(new File("/Users/kennymac/Documents/workspace/InformationRetrieval/k/" + fileName));
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		int doc_length=0;
		HashMap<String, Integer> FrequencyForADocument=new HashMap<>();
		HashMap<String, Double> TF_document=new HashMap<>();


		while((line = bufferedReader.readLine()) != null) 
		{
			String str=line.toString();

			for(String i : str.split(" "))
			{
				//Incrementing Doc length
				doc_length++;

				i=i.replaceAll("।", "");
				i=i.replaceAll(",", "");

				if(StopHash.contains(i))	
				{
					continue;
				}

				i=stemmer.stem(i);			// Stemming of a Hindi word

				int flag=0;


				// If the world has already occurred
				for(int j=0; j<words.size();j++)
				{
					if(words.get(j).equals(i) )
					{
						flag=1;
						frequency.set(j, frequency.get(j) + 1);
						HashSet<Integer> returned=Indexing.get(j);


						if(!returned.contains(doc_id))
						{
							returned.add(doc_id);
							Indexing.set(j, returned);
						}
					}
				}	


				// If the word is occurring for the first time
				if(flag==0  )
				{
					words.add(i);
					frequency.add(1);
					FrequencyForADocument.put(i, 1);
					HashSet<Integer> returned=new HashSet<>();
					returned.add(doc_id);
					Indexing.add(returned);

				}

				if(FrequencyForADocument.containsKey(i))
				{
					FrequencyForADocument.put(i, FrequencyForADocument.get(i) + 1);
				}
				else FrequencyForADocument.put(i, 1);

			}

		}   
		//TF of the document
		for(int i=0; i< words.size(); i++)
		{
			if(FrequencyForADocument.containsKey(words.get(i)))
			{
				double TermFrequency=(double)FrequencyForADocument.get(words.get(i))/doc_length;
				TF_document.put(words.get(i), TermFrequency);
			}

		}



		TF.add(TF_document);



		//System.out.println("dfe" + TF.get(0).containsKey(words.get(0)));
		bufferedReader.close();

	}

	public static ArrayList<ArrayList<Double>> TFIDF(ArrayList<String> words, ArrayList<Integer> frequency, ArrayList<HashSet<Integer>> Indexing, ArrayList< HashMap<String, Double>> TF, int Number_of_documents) 
	{
		System.out.println();


		ArrayList<ArrayList<Double>> TFIDFarray=new ArrayList<>();

		int n= words.size();


		for(int i=0; i<Number_of_documents; i++ )
		{
			ArrayList<Double> TFIDF_document=new ArrayList<>();

			double IDF= Math.log ( Number_of_documents / ( Indexing.get(i).size() ) ); // IDF for a term remains same for every document.

			for(int j=0; j<n; j++ )
			{
				double product=0;
				if(TF.get(i).containsKey(words.get(j)))
				{
					product = TF.get(i).get(words.get(j)) * IDF;
				}
				TFIDF_document.add(product);
			}

			TFIDFarray.add(TFIDF_document);
		}



		//		for(ArrayList<Double> i: TFIDFarray)
		//		{
		//			System.out.println(i);
		//		}

		return TFIDFarray;
	}

	public static void WriteToFile(ArrayList<String> words, ArrayList<Integer> frequency) throws IOException 
	{


		FileOutputStream fos = new FileOutputStream("random.tmp");		// Writing into a temporary object
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(frequency);
		oos.writeObject(words);

		//		oos.writeObject("Word\t\tFrequency\n\n");
		//
		//		for(int i=0; i<words.size();i++)
		//		{       		 
		//			oos.writeObject(words.get(i)+ "\t:\t");
		//			oos.writeObject(frequency.get(i));
		//			oos.writeObject("\n");
		//		}

		oos.close();

	}

	public static void ReadFromFile(ArrayList<String> words, ArrayList<Integer> frequency) throws IOException, ClassNotFoundException 
	{

		FileInputStream fos = new FileInputStream("random.tmp");
		ObjectInputStream oos = new ObjectInputStream(fos);


		while(fos.available() > 0)			// Till End of file
		{
			System.out.print(oos.readObject());
		}

		oos.close();

	}
}