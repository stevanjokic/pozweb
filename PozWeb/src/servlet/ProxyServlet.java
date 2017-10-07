package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProxyServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static String PORXYING_URL = "url";
       
    public ProxyServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			String urlToProxy = request.getParameter(PORXYING_URL);
			
			URL oracle = new URL(urlToProxy);
			BufferedReader in = new BufferedReader(
			new InputStreamReader(oracle.openStream()));

			String inputLine;
			StringBuffer sb = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
//            System.out.println(inputLine);
				sb.append(inputLine);
			}
			in.close();
			
			response.getWriter().write(sb.toString());//append("Served at: ").append(request.getContextPath());
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception e) {
			e.printStackTrace();
			response.getWriter().write("error : " + e.getMessage());//append("Served at: ").append(request.getContextPath());
			response.getWriter().flush();
			response.getWriter().close();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
