package ag.demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LinkValidatorSync {
	
	private static HttpClient client; 
	
	/*
	 * storing as a static, so no need to recreate it every time, but can re-use it
	 */
	public static void main(String[] args) throws IOException {
		client = HttpClient.newHttpClient(); 
		
		String url = "src/urls.txt";
		
		// shows where it is looking for a file 
		Path pathToFile = Paths.get(url);
	    System.out.println(pathToFile.toAbsolutePath());
		
	    Files.lines(Path.of(url)) // to read in every line of urls.txt 
			.map(LinkValidatorSync:: validateLink) // map our validate link method over each url 
			.forEach(System.out:: println); // now we are going to print them to the console 

	}
	
	
	private static String validateLink(String link) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(link))
				.GET()
				.build(); 
		
		try {
			HttpResponse<Void> response = client.send(request, 
					HttpResponse.BodyHandlers.discarding()); // body handler is responsible for turning from the Http server into a java object, used discard, which will return empty body, which we're not interested in 
			return responseToString(response); // we get back a response body, headers and status code 
		}
		catch (IOException | InterruptedException e) {
			return String.format("%s -> %s", link, false); 
		}
	}
	
	private static String responseToString(HttpResponse<Void> response) {
		int status = response.statusCode(); 
		boolean success = status >= 200 && status <= 299; 
		
		return String.format("%s -> %s (status: %s)", response.uri(), success, status); 
	}
}
