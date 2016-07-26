package com.alex.upsolving;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * This class provides the functionality for getting
 * the unsolved problems of a given user in the tju online judge
 * acm.tju.edu.cn
 * 
 * @author Alexis Hernandez
 *
 */
public class TJU extends OnlineJudge {

	private final String PHPSESSID;
	
	private final String user, password;
	
	public TJU(String user, String passw) throws Exception	{
		this.user = user;
		this.password = passw;
		
		final String url = "http://acm.tju.edu.cn/toj/list.php?vol=1";
		// get session id
        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpclient.execute(request)) {
            	PHPSESSID = response.getFirstHeader("Set-Cookie")
            			.getValue().split(";")[0]
            			.split("=")[1];
            }
        }

        // do login
        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
            HttpPost request = new HttpPost(url);

    		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
    		params.add(new BasicNameValuePair("PHPSESSID", PHPSESSID));
    		params.add(new BasicNameValuePair("login", "Login"));
    		params.add(new BasicNameValuePair("user_id", user));
    		params.add(new BasicNameValuePair("passwd", passw));
    		request.setEntity( new UrlEncodedFormEntity(params, "UTF-8") );
    		
            // set common headers (may useless)
            request.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0 Iceweasel/31.6.0");
            request.setHeader("Host", "acm.tju.edu.cn");
            request.setHeader("Connection", "keep-alive");
            request.setHeader("Accept-Language", "en-US,en;q=0.5");
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Referer", "http://acm.tju.edu.cn/toj/list32.html");

            try (CloseableHttpResponse response = httpclient.execute(request)) {
                
                HttpEntity entity = response.getEntity();
                // recover String response (for debug purposes)
                String result;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {

                    result = in.lines().collect( Collectors.joining("\n") );
                }

                if	( result.contains("<H1>Password error!</H1>") ||
                		!result.contains("user_" + user + ".html") )
                	throw new Exception("Wrong user or password");
                
            //    System.out.println(result);
            }
        }
	}
	
	@Override
	protected List<Problem> getUnsolvedProblems() throws Exception {
		int max = 32;
		List<Problem> result = new LinkedList<>();
		for (int num = 1; num <= max; num++)	{
			result.addAll( getUnsolvedProblemsOnList(num) );
		}
		return result;
	}
	
	private String PROBLEM_REGEX = "p[(][0,1]\\,[2].*[)]";
	
	private List<Problem> getUnsolvedProblemsOnList(int num)	throws Exception	{
		List<Problem> result = new ArrayList<>();
		final String url = "http://acm.tju.edu.cn/toj/list.php?vol=" + num;
		
        try (CloseableHttpClient httpclient = HttpClients.createDefault();) {
            HttpPost request = new HttpPost(url);

    		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
    		params.add(new BasicNameValuePair("PHPSESSID", PHPSESSID));
    		params.add(new BasicNameValuePair("login", "Login"));
    		params.add(new BasicNameValuePair("user_id", user));
    		params.add(new BasicNameValuePair("passwd", password));
    		request.setEntity( new UrlEncodedFormEntity(params, "UTF-8") );
    		
            // set common headers (may useless)
            request.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:31.0) Gecko/20100101 Firefox/31.0 Iceweasel/31.6.0");
            request.setHeader("Host", "acm.tju.edu.cn");
            request.setHeader("Connection", "keep-alive");
            request.setHeader("Accept-Language", "en-US,en;q=0.5");
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Referer", "http://acm.tju.edu.cn/toj/list32.html");

            try (CloseableHttpResponse response = httpclient.execute(request)) {
                
                HttpEntity entity = response.getEntity();
                // recover String response (for debug purposes)
                String lines;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {

                    lines = in.lines().collect( Collectors.joining("\n") );
                }

                if	( lines.contains("<H1>Password error!</H1>") ||
                		!lines.contains("user_" + user + ".html") )
                	throw new Exception("Wrong user or password");

                
                Pattern p = Pattern.compile(PROBLEM_REGEX);
                Matcher m = p.matcher(lines);
                StringTokenizer st;
                while	( m.find() )	{
                	int start = m.start();
                	int end = m.end();
                	st = new StringTokenizer( lines.substring(start, end), "(),\"" );
                	
                	st.nextToken();
                	st.nextToken();
                	st.nextToken();
                	String id = st.nextToken();
                	String name = st.nextToken();
                	Problem problem = new Problem(id, name);
                	result.add(problem);
                }
                
            }
        }
        return	result;
	}

	public static void main(String[] args) throws Exception {
        if ( args.length != 2 )
            throw new Exception( "Usage: java TJU [user] [password]" );

        String user = args[0];
        String password = args[1];
		TJU tju = new TJU(user, password);
		List<Problem> list = tju.getUnsolvedProblems();
		System.out.println("found: " + list.size() );
		for (Problem p : list)
			System.out.println(p);
	}


}
