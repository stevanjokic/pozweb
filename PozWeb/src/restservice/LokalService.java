package restservice;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import model.Lokal;

import com.google.gson.Gson;


@Path("/lokal")
public class LokalService {

	@GET
	@Path("/list")
	@Produces("application/json")
	public String feed()
	{
		String res  = null;
		try 
		{
			ArrayList<Lokal> lokals = new ArrayList<Lokal>();
			lokals.add(new Lokal("name", "address", "1234", "567", "descr", 44, 19));
			Gson gson = new Gson();
			System.out.println(gson.toJson(lokals));
			res = gson.toJson(lokals);

		} catch (Exception e)
		{
			System.out.println("error");
		}
		return res;
	}

	
	
}
