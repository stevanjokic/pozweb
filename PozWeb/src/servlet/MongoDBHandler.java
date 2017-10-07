package servlet;

import java.io.IOException;
import java.util.Arrays;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class MongoDBHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Mongo mongo;
	private DB mongoDB;

	private String hostIP, dbName;

	public MongoDBHandler() {
		super();

		try {
			InitialContext initContext = new InitialContext();
			Context environmentContext = (Context) initContext.lookup("java:/comp/env");
			hostIP = (String) environmentContext.lookup("hostIP");
			dbName = (String) environmentContext.lookup("dbName");
		} catch (Exception e) {
			System.err.println("error initContext: " + e.getMessage());
		}
		
		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo: " + e.getMessage() + " " + hostIP);
		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String dbs = " dbs: ";
		try {
			if(mongo!=null) {
				dbs += Arrays.toString(mongo.getDatabaseNames().toArray());
			}
		} catch (Exception e) {
			System.err.println("error: " + e.getMessage());
		}
		
		response.getWriter().append(mongo + " " + hostIP + " " + dbName +  dbs );
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

}
