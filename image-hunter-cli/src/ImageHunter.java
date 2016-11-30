

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;



public class ImageHunter {

    private static final String googleImageSearchURL = "https://images.google.com/searchbyimage?image_url=";
    private static final String googleDotCom = "https://google.com";


    public static void main(String[] args) {
    	System.out.println("Enter a webpage to begin the hunt!");
    	System.out.print("Webpage URL: ");
    	Scanner scan = new Scanner(System.in);
    	String webpageURL = scan.next();
    	scan.close();
    	
    	List<String> links = new ArrayList<String>();
    	List<String> images = new ArrayList<String>();
    	boolean DEBUG = false;
    	
    	if(DEBUG) {
        	System.out.println("Working Directory = " + System.getProperty("user.dir"));
    	}
    	
    	if(webpageURL != null) {
    		System.out.println("Sniffing...");
    	}
	    try {  
            Document doc = Jsoup.connect(webpageURL).timeout(300000).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_16) Gecko/20100101 Firefox/25.0")
													.referrer("http://www.google.com")
													.validateTLSCertificates(false)
													.get();       
            
            Elements img = doc.getElementsByTag("img");
            

            images = getImages(img, doc);
            
            if(DEBUG) {
            	System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------");
            }
            
            for(String link : images) {            	
            	String linkURL = googleImageSearchURL + link;
            	if(DEBUG) {
            		System.out.println("Hunting for traces of " + linkURL);
            		System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------");
            	}
            	            	
            	Document redirectDoc = getDocument(linkURL);
            	Elements resultImgs = redirectDoc.select("h3");
            	if(DEBUG) {
            		for(Element targetImg : resultImgs) {
            			System.out.println("Possible Target Found: " + targetImg.select("a").attr("href"));
            		}           	
            	}            	
            	if(!redirectDoc.select("table[id=nav]").html().isEmpty() &&
            			redirectDoc.select("table[id=nav]").html() != null) {
            		Elements googleTable = redirectDoc.select("table[role=presentation]");
            		Elements googleCells = googleTable.select("td");
            		
            		for(int i = 0; i < googleCells.size(); i++) {
            			Element cell = googleCells.get(i);
            			String googlePageLink = cell.select("a").attr("href");
                    	Document pageDoc = getDocument(googleDotCom + googlePageLink);
                    	Elements targetPageLinks = pageDoc.select("h3");
                    	for(Element targetPageLink : targetPageLinks) {
                    		if(DEBUG) {
                    			System.out.println("Possible Target Found: " + targetPageLink.select("a").attr("href"));
                    		}
                    		links.add(targetPageLink.select("a").attr("href"));
                    	}                    	
            		}
            	} 	
            	
            	if(DEBUG){ 
            		System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            	}
            }
            
        } catch (IOException ex) {
            System.err.println("There was an error.  "
            		+ "Make sure you have the proper protocol (http, https) and the URL is correct!!!");
            //Logger.getLogger(ImageHunter.class.getName()).log(Level.SEVERE, null, ex);
        }
	    htmlWriter(images, links);
	    System.out.println("Hunt Completed");
	    System.exit(0);
    }
    
    public static List<String> getImages(Elements img, Document doc) {
    	List<String> imgURLs = new ArrayList<String>();
    	int count = 0;
    	
    	 for (Element el : img) {             
	         if(el.attr("data-src") != null && !el.attr("data-src").isEmpty()) {
	        	 imgURLs.add(el.attr("data-src"));
	        	 count++;
	         } 
         }
    	 System.out.println(count + " Scent(s) identified...");
    	 System.out.println("Hunting...");
    	 return imgURLs;
    }
    
    public static Document getDocument(String URL) {
    	Document doc = null;
    	try {
    		Response response = Jsoup.connect(URL).timeout(500000).userAgent("Mozilla/17.0 (Macintosh; Intel Mac OS X 10_11_16) Gecko/20100101 Firefox/25.0")
    				.referrer("http://www.google.com").followRedirects(false).ignoreHttpErrors(true).ignoreContentType(true).execute();
        	Response responseRedirect = Jsoup.connect(response.header("location")).timeout(300000).userAgent("Mozilla/17.0 (Macintosh; Intel Mac OS X 10_11_16) Gecko/20100101 Firefox/25.0")
    				.referrer("http://www.google.com").followRedirects(false).ignoreHttpErrors(true).ignoreContentType(true).execute();  
        		doc = responseRedirect.parse();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
		return doc;
    }
    
    public static void htmlWriter(List<String> targetLinks, List<String> resultLinks) {
    	FileWriter fWriter = null;
    	BufferedWriter writer = null;
    	try {
    	    fWriter = new FileWriter("image-hunter_RESULTS.html");
    	    writer = new BufferedWriter(fWriter);
    	    writer.write("<h1>image-hunter</h1>");
    	    writer.write("<h2>Scent(s) identified...</h2>");
    	    for(String link : targetLinks) {
    	    	writer.write("<li>" + "<a href=" + "'" + link + "'>"+ link + "</a></li>");
    	    }
    	    writer.write("<h2>hunted results</h2>");
    	    for(String link : resultLinks) {
    	    	writer.write("<li>" + "<a href=" + "'" + link + "'>"+ link + "</a></li>");
    	    	writer.write("<br>");
    	    }
    	    writer.newLine(); 
    	    writer.close();
    	} catch (Exception e) {
    		e.printStackTrace();    	}
    }
} //End of Class