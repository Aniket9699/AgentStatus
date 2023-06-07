import java.security.cert.TrustAnchor
import java.sql.Connection
import javax.net.ssl.SSLContext
import groovy.json.JsonSlurper
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.io.OutputStream
import groovy.json.*
import groovy.transform.Field

//Intiating varaibles
@Field def url = "${p:ucd_uat_url}"
@Field def username = "${p:ucd_user}"
@Field def password = "${p:ucd_user_pass}"
@Field def Number = ${p:PageNumbersInUCD}

//Encoding username and password
@Field String encoding = Base64.getEncoder().encodeToString(( username + ":" + password ).getBytes("UTF-8"));

def getAgents(int NumberOfpage){
    
    //Request URL
    def uri = url + "/rest/agent?rowsPerPage=250&pageNumber=${NumberOfpage}&orderField=name&sortType=asc"
    
    //Payload here 
    def data = ''
    
    //Creating object to accept all hostnames
    def nullHostnameVerifier = [
       verify:{hostname, session -> true} 
    ]
    
    //Accept all certificate ignore SSL check
    def sc = SSLContext.getInstance("SSL")
    def trustAll = [getAcceptedIssuers: {}, checkClientTrusted: { a, b -> }, checkServerTrusted: { a, b -> }]
    sc.init(null, [trustAll as X509TrustManager] as TrustManager[], null);
    HttpsURLConnection.defaultSSLSocketFactory = sc.socketFactory;
    HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier);
    
    //Setting up connection
    def connection = new URL(uri).openConnection() as HttpURLConnection

    //Setting Headers
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Accept", "application/json")
    connection.setRequestProperty("Authorization","Basic " + encoding)
 
    //println"===========================Response Headers============================================="
    Map<String, List<String>> map = connection.getHeaderFields();
    for(Map.Entry<String, List<String>> entry : map.entrySet()){
        //println"${entry.getKey()}:${entry.getValue()}"
    }
    //println"========================================================================================"
    def file = new File("agent.txt") 
    
    if ( connection.responseCode == 200) {
        // get the JSON response
        json = connection.inputStream.withCloseable { inStream ->
                        new JsonSlurper().parse( inStream as InputStream )
    }
    //retrieve data from JSON
        for (entry in json) {
        // retrieve data from each entry
        def name = entry.name
        def status = entry.status
        if (status == "OFFLINE") {
            println "Agent Name: ${name}"
        }
    }
    // Write data to text file
    }else if(connection.responseCode == 404){
     //Do nothing
    }else {
     //Do nothing
    }
}

def getAgentIpAndOsType(String AgentName,String Property){

    //API URL
    def uri = url + "/cli/agentCLI/getProperty?agent=${AgentName}&name=${Property}"
    
    //Creating object to accept all hostnames
    def nullHostnameVerifier = [
        verify:{hostname, session -> true} 
    ]
    
    //Accept all certificate ignore SSL check
    def sc = SSLContext.getInstance("SSL")
    def trustAll = [getAcceptedIssuers: {}, checkClientTrusted: { a, b -> }, checkServerTrusted: { a, b -> }]
    sc.init(null, [trustAll as X509TrustManager] as TrustManager[], null);
    HttpsURLConnection.defaultSSLSocketFactory = sc.socketFactory;
    HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier);
    
    //Setting up connection
    def connection = new URL(uri).openConnection() as HttpURLConnection
    
    //Setting Headers
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Accept", "text/plain")
    connection.setRequestProperty("Authorization","Basic " + encoding)
     
    //println"===========================Response Headers============================================="
    Map<String, List<String>> map = connection.getHeaderFields();
    for(Map.Entry<String, List<String>> entry : map.entrySet()){
        //println"${entry.getKey()}:${entry.getValue()}"
    }
    // println"========================================================================================"
    
    if ( connection.responseCode == 200) {
        // get the plain text response
        def inputStream = connection.inputStream
        def reader = new BufferedReader(new InputStreamReader(inputStream))
        def response = ''
        String line
        while ((line = reader.readLine()) != null) {
            response += line
        }
        reader.close()
        inputStream.close()
    
        //println "Response: ${response}"
        return response // Store response in a variable            
    }else if(connection.responseCode == 404){
        //Do nothing
    }else {
        //Do nothing
    }
}

for( int i = 0; i<=Number; i++ )  {
    println  "*************************** Getting Details for Page Number ${i}***********************************"
    getAgents(i)
    println  "***************************************************************************************************"
}
