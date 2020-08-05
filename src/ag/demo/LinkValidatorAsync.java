package ag.demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LinkValidatorAsync {
		
	private static HttpClient client; 
	
	public static void main(String[] args) throws IOException {
		/*
		 * does not handle re-directs, so need to replace with newBuilder()
		 * client = HttpClient.newHttpClient(); 
		 */
		client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(3)) // set connection time out to 3s 
					.followRedirects(HttpClient.Redirect.NORMAL) // handles re-direct behaviour 
					.build(); 
		
		String url = "src/urls.txt";
		
		// shows where it is looking for a file 
		Path pathToFile = Paths.get(url);
	    System.out.println(pathToFile.toAbsolutePath());
	
	    /* 
	    * ensures all the requests are executed in parallel and we we get back a list 
	    * of completable future that represnts all the future results
	    */ 
	    var futures = Files.lines(Path.of(url)) // to read in every line of urls.txt 
			.map(LinkValidatorAsync:: validateLink) // map our validate link method over each url 
			.collect(Collectors.toList()); // turn completable future in a list to use it later 
	    
	    /*
	     * ensure the program does not end before completable future are async completed 
	     * we get a stream of a status strings for all the requests that have been performed 
	     */
	    futures.stream()
	    		.map(CompletableFuture::join)
	    		.forEach(System.out::println); 
	}
	
	
	private static CompletableFuture<String> validateLink(String link) {
		HttpRequest request = HttpRequest.newBuilder(URI.create(link))
				.timeout(Duration.ofSeconds(2)) // handles time outs
				.GET()
				.build(); 
		
		
		return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
					.thenApply(LinkValidatorAsync::responseToString) // transforms the response to a string rather than http response 
					.exceptionally(e -> String.format("%s -> %s", link, false)); // error handling in case something goes wrong 
	}
	
	private static String responseToString(HttpResponse<Void> response) {
		int status = response.statusCode(); 
		boolean success = status >= 200 && status <= 299; 
		
		return String.format("%s -> %s (status: %s)", response.uri(), success, status); 
	}
	


}
