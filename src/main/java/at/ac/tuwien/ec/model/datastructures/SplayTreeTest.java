package at.ac.tuwien.ec.model.datastructures;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class SplayTreeTest {

	public static void main(String[] args) {
		SparkConf configuration = new SparkConf();
		configuration.setMaster("local");
		configuration.setAppName("Sleipnir");
		JavaSparkContext jscontext = new JavaSparkContext(configuration);	    	
	    
		SplayTree<String,String> t = new SplayTree<String,String>();
		t.put("key0", "value0");
		t.put("key1", "value1");
		t.put("key2", "value2");
		t.put("key3", "value3");
		
		System.out.println(t.getRoot().getValue());
		System.out.println(t);
		
		t.remove("key0");
		
		
		
		System.out.println(t.getRoot().getValue());
		
		System.out.println(t);
		
		t.get("key0");
		
		
		
		System.out.println(t.getRoot().getValue());
		
		System.out.println(t);
		
		
		t.get("key0");
		
		
		
		System.out.println(t.getRoot().getValue());
		
		System.out.println(t);

	}

}
