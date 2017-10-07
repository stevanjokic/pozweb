package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import com.google.gson.Gson;

public class PostTest {
	
	public static String getFile(String file) {
		
		FileInputStream fis;
		StringBuffer sb = new StringBuffer();
		
		try {
			fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			 
			String line = null;
			while ((line = br.readLine()) != null) {
//				System.out.println(line);
				try{ 
					sb.append(line);
				} catch (Exception e) {
					
				}
			}
		 
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {

		String rawData = "{ \"сјокић\" : 33 }";

//		Gson gs = new Gson();
//		System.out.println(gs.toJson(new a()));
//		rawData = gs.toJson(new a());
		
//		rawData = "{ \"a\" : 22 }";
		
//		rawData = getFile("data/all_73.json");
//		rawData = getFile("data/dana.json");
//		rawData = getFile("data/ecgdatash.json");
//		rawData = getFile("data/arr.json");
//		rawData = getFile("data/hrv2");
		
		URL u = new URL("http://localhost:8080/poz/srv/mongodb/post/test/rawdata/");
		u = new URL("http://sj-devsrv.rhcloud.com/poz/srv/mongodb/post/sj/openshift/");
		u = new URL("http://sj-devsrv.rhcloud.com/poz/srv/mongodb/post/sj/mix/");
		u = new URL("http://localhost:8080/poz/srv/mongodb/post/sj/tst/");
//		u = new URL("http://localhost:8080/poz/srv/mongodb/put/sj/mixdata/57b6c33675ab58b1b66d7cd9vvvvv");
		
		
		String type = "application/json";

		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty( "Content-Type", type );
		conn.setRequestProperty( "Content-Length", String.valueOf(rawData.length()));
		OutputStream os = conn.getOutputStream();
		os.write(rawData.getBytes());
		os.flush();
		InputStream is = conn.getInputStream();
		long start = System.currentTimeMillis();
		while(is.available()<=0 && System.currentTimeMillis()-start < 30000) {
			Thread.sleep(100);
		}
		System.out.println("read:");
		while(is.available()>0) {
			System.out.print((char)is.read());
		}
		
		is.close();
		os.close();
		conn.disconnect();
		
	}
	
//	public static class a {
//		String n = "ћирилично име,  latinično malo";
//	}

}
