/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parseotglist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 *
 * @author spidchenko.d
 */
public class ParseOtgList {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

          updateOTGDatabase("0552");
        ubdateVillagesAndTgDatabase();
//        Parser parser = new Parser("http://decentralization.gov.ua/areas/0552/gromadu");
//        parser.setEncoding("UTF-8");
//        
//        NodeList nodeList = parser.parse(new HasAttributeFilter("title","Назва громади"));
//        
//        System.out.println(nodeList.toString());
        
    }
    
    static void updateOTGDatabase(String areaCode) throws IOException{ //0552 KS   0512 MK    
        
        DBConnection connection = new DBConnection();
        System.setProperty("javax.net.ssl.trustStore", "c:/Users/spidchenko.d/Downloads/decentralizationgovua.jks");
        Document doc = Jsoup.connect("https://decentralization.gov.ua/areas/"+areaCode+"/gromadu").get();
        Elements otgTitleElements = doc.getElementsByAttributeValue("title","Назва громади");
        
        //System.out.println(otgTitleElements.html());
        
        Iterator itr = otgTitleElements.iterator();
        Element e;
        
        while(itr.hasNext()){
            e = (Element)itr.next();
            connection.setNewOTG(Integer.parseInt(e.attr("href").split("/")[2]), e.html());
        }
    }
    
    static void ubdateVillagesAndTgDatabase() throws IOException{
        DBConnection connection = new DBConnection();
        System.setProperty("javax.net.ssl.trustStore", "c:/Users/spidchenko.d/Downloads/decentralizationgovua.jks");
        ArrayList ids = connection.getOTGIds();
        Iterator liTags = null, itr = ids.iterator();
        Document doc;
        Element divList, liElement;
        int numElements, currentOTG;
        while(itr.hasNext()){
            
            currentOTG = (int)itr.next();
            
            doc = Jsoup.connect("https://decentralization.gov.ua/gromada/"+currentOTG+"/composition").get();
            divList = doc.getElementById("consist");
            System.out.println(divList.html());
            
            numElements = divList.getElementsByTag("ul").size();
            
            System.out.println(currentOTG+"; Number of ul tags = "+numElements);
            
            if (numElements > 1){   //OTG has TG
                for(int i = 0; i < numElements; i++){
                    System.out.println(">>>>> "+divList.getElementsByTag("p").eq(i).text());
                    
                    int newTgId = connection.setNewTG(currentOTG, divList.getElementsByTag("p").eq(i).text());                    //DB
                    
                    //System.out.println("> "+divList.getElementsByTag("ul").get(i).children().iterator());
                    
                    liTags = divList.getElementsByTag("ul").get(i).children().iterator();
                    
                    while(liTags.hasNext()){
                        String villageName = ((Element)liTags.next()).text().replace(",", "");
                        System.out.println("> "+villageName);
                        
                        connection.setNewVillage(newTgId, currentOTG, villageName);                                               //DB
                    }

                }
            } else{ //OTG has no TG, only Villages
                try{
                    liTags = divList.getElementsByTag("ul").get(0).children().iterator();
                
                        while(liTags.hasNext()){
                            String villageName = ((Element)liTags.next()).text().replace(",", "");
                            System.out.println("> "+villageName);

                            connection.setNewVillage(0, currentOTG, villageName);                                               //DB
                        }
                }catch (java.lang.IndexOutOfBoundsException err){
                    System.out.println("OTG has no villages!");
                }    
                

            }
           // System.out.println(divList.getElementsByTag("p").text());
            
        }
    }
    
}
