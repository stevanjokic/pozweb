package restservice;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import util.MongoObjectIdSerializer;

@Path("/mongodb")
public class MongoDBAccess {

	private String hostIP, us, pwd;

	public MongoDBAccess() {
		super();

		try {
			InitialContext initContext = new InitialContext();
			Context environmentContext = (Context) initContext.lookup("java:/comp/env");
			hostIP = (String) environmentContext.lookup("hostIP");
			us = (String) environmentContext.lookup("us");
			pwd = (String) environmentContext.lookup("pwd");
		} catch (Exception e) {
//			System.err.println("error initContext: " + e.getMessage());
		}

		if (hostIP == null) {
			hostIP = "127.11.10.4";
			us = "admin";
			pwd = "7vgqwA73-SGn";
		}

	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getServicehelp() {
		return "usage: \r\n/mongodb/get/{db}/{collection}/ list all items in the collection, URL parameters: limit, offset, pretty:true/false, sort:asc/desc (by _id)"
				+ " \r\n/mongodb/get/{db}/{collection}/{id} get by ID, URL param pretty:true/false"
				+ " \r\n/mongodb/post/{db}/{collection}/ post JSON, return stored document ID"
				+ " \r\n/mongodb/delete/{db}/{collection}/{id} delete document by id"
				+ " \r\n/mongodb/put/{db}/{collection}/{id} update document by id, or create if id does not exist"
				+ " \r\n/mongodb/query/{db}/{collection}/ post query, URL parameters: limit, offset, pretty:true/false, sort:asc/desc (by _id), fields"
				+ " \r\n/mongodb/query/{db}/{collection}/ get query, URL parameters: limit, offset, pretty:true/false, sort:asc/desc (by _id), query, fields"
				+ " \r\n/mongodb/overalscore/{db}/{collection}/ get score at ECG for Everybody in percentage, URL parameters: age, gender, activity, hr, rmssd, sdnn. Activity can be one of: {Resting: Work, low activity; Unspecified; Post-Exercising; Exercising; Pre-Exercising}"
				+ " \r\n/mongodb/overalscore/json/{db}/{collection}/ get detail score at ECG for Everybody, URL parameters: age, gender, activity, hr, rmssd, sdnn. Activity can be one of: {Resting: Work, low activity; Unspecified; Post-Exercising; Exercising; Pre-Exercising}"
				
				+ " \r\n\r\n/mongodb/get/excel/{db}/{collection}/ shows TSV or CSV, URL parameters: limit, offset, separator:tsv/csv, sort:asc/desc (by _id)"
				+ " \r\n/mongodb/get/excelfile/{db}/{collection}/ download TSV or CSV file, URL parameters: limit, offset, separator:tsv/csv, sort:asc/desc (by _id)"
				+ " \r\n/mongodb/query/excel/{db}/{collection}/{query} get query to show TSV or CSV file, URL parameters: limit, offset, separator:tsv/csv, sort:asc/desc (by _id)"
				+ " \r\n/mongodb/query/excelfile/{db}/{collection}/{query} get query to download TSV or CSV file, URL parameters: limit, offset, separator:tsv/csv, sort:asc/desc (by _id)"
				+ " \r\n/mongodb/query/excelfile/{db}/{collection}/ post query to show TSV or CSV file, URL parameters: limit, offset, separator:tsv/csv, sort:asc/desc (by _id)"
				;		
	}

	@GET
	@Path("get/{db}/{collection}/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@DefaultValue("100") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("false") @QueryParam("pretty") boolean pretty, @QueryParam("sort") String sort) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBCursor cursor; // =
									// dBcollection.find().skip(offset).limit(limit);

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find().sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find().sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find().skip(offset).limit(limit);
				}

				ArrayList<DBObject> results = new ArrayList<DBObject>();
				try {
					while (cursor.hasNext()) {
						results.add(cursor.next());
					}
				} finally {
					cursor.close();
				}

				return getGSON(pretty).toJson(results);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}



	@GET
	@Path("get/{db}/{collection}/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getMongoDataByID(@PathParam("db") String db, @PathParam("collection") String collection,
			@PathParam("id") String id, @DefaultValue("false") @QueryParam("pretty") boolean pretty) {

		Mongo mongo = null;

		ObjectId idObj = null;
		try {
			idObj = new ObjectId(id);
		} catch (Exception e) {

		}
		if (idObj == null) {
			return "null";
		}

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject res = dBcollection.findOne(new BasicDBObject("_id", idObj));
				return getGSON(pretty).toJson(res);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}

	@GET
	@Path("delete/{db}/{collection}/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteMongoDataByID(@PathParam("db") String db, @PathParam("collection") String collection,
			@PathParam("id") String id) {

		Mongo mongo = null;

		ObjectId idObj = null;
		try {
			idObj = new ObjectId(id);
		} catch (Exception e) {

		}
		if (idObj == null) {
			return "null";
		}

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				dBcollection.remove(new BasicDBObject("_id", idObj));
				return "ok";

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}

	@POST
	@Path("post/{db}/{collection}/")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Consumes(MediaType.APPLICATION_JSON)
	public String postMongoData(@PathParam("db") String db, @PathParam("collection") String collection, String data) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject doc = (DBObject) JSON.parse(data);

				if (dBcollection == null || doc == null) {
					return "{ \"error\" : \"null db\" }";
				}

				if (doc instanceof BasicDBList) {
					BasicDBObject host = new BasicDBObject();
					host.append("array_data", doc);
					dBcollection.insert(host);
					return host.get("_id").toString();
				} else {
					dBcollection.insert(doc);
//					return "{ \"ok\" : \" " + "\" }";
					return doc.get("_id").toString();
				}

				// Object id = doc.get( "_id" );
				// return new Gson().toJson(doc);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}

	@POST
	@Path("put/{db}/{collection}/{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Consumes(MediaType.APPLICATION_JSON)
	public String putMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@PathParam("id") String id, String data) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject doc = (DBObject) JSON.parse(data);

				if (dBcollection == null || doc == null) {
					return "null";
				}

				ObjectId idObj = null;
				try {
					idObj = new ObjectId(id);
				} catch (Exception e) {
				}

				if (doc instanceof BasicDBList) {
					BasicDBObject host = new BasicDBObject();
					host.append("array_data", doc);
					if (idObj != null) {
						host.append("_id", idObj);
					}
					// dBcollection.insert(host);
					dBcollection.save(host);
					return host.get("_id").toString();
				} else {
					if (idObj != null) {
						doc.put("_id", idObj);
					}
					dBcollection.save(doc);
					return doc.get("_id").toString();
				}

				// Object id = doc.get( "_id" );
				// return new Gson().toJson(doc);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}

	@GET
	@Path("query/{db}/{collection}/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getQueryMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@DefaultValue("100") @QueryParam("limit") int limit, 
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("false") @QueryParam("pretty") boolean pretty, 
			@DefaultValue("desc") @QueryParam("sort") String sort,
			@DefaultValue("{}") @QueryParam("query") String query,
			@QueryParam("fields") String fields
			) {
		
		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject doc = (DBObject) JSON.parse(query);

				if (dBcollection == null || doc == null) {
					return "null";
				}
				
				DBObject fieldsObj = null;
				try {
				    if (fields!=null) {
				    	fieldsObj = (DBObject) JSON.parse(fields);
				    }
				} catch (Exception e) {}

				DBCursor cursor;

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find(doc, fieldsObj).sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find(doc, fieldsObj).sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find(doc).skip(offset).limit(limit);
				}

				ArrayList<DBObject> results = new ArrayList<DBObject>();
				try {
					while (cursor.hasNext()) {
						results.add(cursor.next());
					}
				} finally {
					cursor.close();
				}

				return getGSON(pretty).toJson(results);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
		
	}
	
	@POST
	@Path("query/{db}/{collection}/")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
	@Consumes(MediaType.APPLICATION_JSON)
	public String postQueryMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@DefaultValue("100") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("false") @QueryParam("pretty") boolean pretty, 
			@DefaultValue("desc") @QueryParam("sort") String sort,
			@DefaultValue("{}") String query,
			@QueryParam("fields") String fields) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				if(query==null) {
					query = "{}";
				}
				
				DBObject fieldsObj = null;
				try {
				    if (fields!=null) {
				    	fieldsObj = (DBObject) JSON.parse(fields);
				    }
				} catch (Exception e) {}
				
				DBObject doc = (DBObject) JSON.parse(query);

				if (dBcollection == null || doc == null) {
					return "null";
				}

				DBCursor cursor;

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find(doc, fieldsObj).sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find(doc, fieldsObj).sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find(doc).skip(offset).limit(limit);
				}

				ArrayList<DBObject> results = new ArrayList<DBObject>();
				try {
					while (cursor.hasNext()) {
						results.add(cursor.next());
					}
				} finally {
					cursor.close();
				}

				return getGSON(pretty).toJson(results);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}

	@GET
	@Path("overalscore/{db}/{collection}/")
	@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
	public String getOveralrating(@PathParam("db") String db, @PathParam("collection") String collection,
			@QueryParam("age") String age,
			@QueryParam("hr") String hr,
			@QueryParam("rmssd") String rmssd,
			@QueryParam("sdnn") String sdnn,
			@QueryParam("gender") String gender,
			@QueryParam("activity") String activity,
			@QueryParam("appuuid") String appUUID,
			@QueryParam("code") String code) {
		
		
		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				BasicDBObject queryObj = new BasicDBObject();
				if(age!=null) {
					try {
						
						Double ageD = null;
						try {
							if (age != null) {
								ageD = Double.parseDouble(age);
								// TODO remove when db grove
								if (ageD < 20) {
									ageD = 20d;
								}
								if (ageD > 60) {
									ageD = 60d;
								}
							}
						}catch(Exception e) {
							
						}
	
						
						BasicDBList baseQuery = new BasicDBList();
						
						if(ageD!=null) {
							baseQuery.add(new BasicDBObject("age", new BasicDBObject("$gt" , ageD-5)));
							baseQuery.add(new BasicDBObject("age", new BasicDBObject("$lt" , ageD+5)));
						}
						if(gender!=null) {
							baseQuery.add(new BasicDBObject("gender", gender));
						}
						if(activity!=null) {
							baseQuery.add(new BasicDBObject("activity", activity));
						}
						if(code!=null) {
							baseQuery.add(new BasicDBObject("code", code));
						}
						if(appUUID!=null) {
							baseQuery.add(new BasicDBObject("app_uuid", new BasicDBObject("$ne" , appUUID)));
						}
						
						queryObj.put("$and", baseQuery);
						
						int total = dBcollection.find(queryObj ).count();
						
						int scale = 0, worseTot = 0;
						
						int worseHR = -1;
						queryObj.clear();
						if(hr!=null) {
							try {
								BasicDBList currQL = new BasicDBList();
								currQL.addAll(baseQuery);
								currQL.add(new BasicDBObject("HR", new BasicDBObject("$gt" , Double.parseDouble(hr))));
								queryObj.put("$and", currQL);
								worseTot += worseHR = dBcollection.find(queryObj ).count();
								scale++;
							} catch (Exception e) {}
						}
						
						int worseRMSSD = -1;
						queryObj.clear();
						if(rmssd!=null) {
							try {
								BasicDBList currQL = new BasicDBList();
								currQL.addAll(baseQuery);
								currQL.add(new BasicDBObject("rMSSD", new BasicDBObject("$lt" , Double.parseDouble(rmssd))));
								queryObj.put("$and", currQL);
								worseTot += worseRMSSD = dBcollection.find(queryObj ).count();
								scale++;
							} catch (Exception e) {}
						}
						
						int worseSTD = -1;
						queryObj.clear();
						if(sdnn!=null) {
							try {
								BasicDBList currQL = new BasicDBList();
								currQL.addAll(baseQuery);
								currQL.add(new BasicDBObject("STD", new BasicDBObject("$lt" , Double.parseDouble(sdnn))));
								queryObj.put("$and", currQL);
								worseTot += worseSTD = dBcollection.find(queryObj ).count();
								scale++;
							} catch (Exception e) {}
						}
						
//						int better = dBcollection.find(queryObj ).count();
						if(scale>0 && total>0) {
							return Float.toString(100.0f*worseTot/total/scale);
						}
						return "error: no query";
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "error: " + e.getMessage();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "";
		
	}
	
	public static class OveralScore {
		public int total, worseHR, worseRMSSD, worseSTD;
		public float overalSore/*, hrScore, rmssdScore, stdScore*/;

		public OveralScore(int total, int worseHR, int worseRMSSD, int worseSTD, float overalSore) {
			super();
			this.total = total;
			this.worseHR = worseHR;
			this.worseRMSSD = worseRMSSD;
			this.worseSTD = worseSTD;
			this.overalSore = overalSore;
		}
		
	}
	@GET
	@Path("overalscore/json/{db}/{collection}/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDetailOveralRating(@PathParam("db") String db, @PathParam("collection") String collection,
			@QueryParam("age") String age,
			@QueryParam("hr") String hr,
			@QueryParam("rmssd") String rmssd,
			@QueryParam("sdnn") String sdnn,
			@QueryParam("gender") String gender,
			@QueryParam("activity") String activity,
			@QueryParam("appuuid") String appUUID,
			@QueryParam("code") String code)
			
	{
				
		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				BasicDBObject queryObj = new BasicDBObject();
				if(age!=null) {
					try {
						
						Double ageD = null;
						try {
							if (age != null) {
								ageD = Double.parseDouble(age);
								// TODO remove when db grove
								if (ageD < 20) {
									ageD = 20d;
								}
								if (ageD > 60) {
									ageD = 60d;
								}
							}
						}catch(Exception e) {						
						}
						
						BasicDBList baseQuery = new BasicDBList();
						
						if(ageD!=null) {
							baseQuery.add(new BasicDBObject("age", new BasicDBObject("$gt" , ageD-5)));
							baseQuery.add(new BasicDBObject("age", new BasicDBObject("$lt" , ageD+5)));
						}
						if(gender!=null) {
							baseQuery.add(new BasicDBObject("gender", gender));
						}
						if(activity!=null) {
							baseQuery.add(new BasicDBObject("activity", activity));
						}
						if(code!=null) {
							baseQuery.add(new BasicDBObject("code", code));
						}
						if(appUUID!=null) {
							baseQuery.add(new BasicDBObject("app_uuid", new BasicDBObject("$ne" , appUUID)));
						}
						
						queryObj.put("$and", baseQuery);
						
						int total = dBcollection.find(queryObj ).count();
						
						int scale = 0, worseTot = 0;
						
						int worseHR = -1;
						queryObj.clear();
						if(hr!=null) {
							try {
								BasicDBList currQL = new BasicDBList();
								currQL.addAll(baseQuery);
								currQL.add(new BasicDBObject("HR", new BasicDBObject("$gt" , Double.parseDouble(hr))));
								queryObj.put("$and", currQL);
								worseTot += worseHR = dBcollection.find(queryObj ).count();
								scale++;
							} catch (Exception e) {}
						}
						
						int worseRMSSD = -1;
						queryObj.clear();
						if(rmssd!=null) {
							try {
								BasicDBList currQL = new BasicDBList();
								currQL.addAll(baseQuery);
								currQL.add(new BasicDBObject("rMSSD", new BasicDBObject("$lt" , Double.parseDouble(rmssd))));
								queryObj.put("$and", currQL);
								worseTot += worseRMSSD = dBcollection.find(queryObj ).count();
								scale++;
							} catch (Exception e) {}
						}
						
						int worseSTD = -1;
						queryObj.clear();
						if(sdnn!=null) {
							try {
								BasicDBList currQL = new BasicDBList();
								currQL.addAll(baseQuery);
								currQL.add(new BasicDBObject("STD", new BasicDBObject("$lt" , Double.parseDouble(sdnn))));
								queryObj.put("$and", currQL);
								worseTot += worseSTD = dBcollection.find(queryObj ).count();
								scale++;
							} catch (Exception e) {}
						}
						
//						int better = dBcollection.find(queryObj ).count();
						 
						if(total>0 && scale>0) {
							float overalSore = 100.0f*worseTot/total/scale;
							return getGSON(true).toJson(new OveralScore(total, worseHR, worseRMSSD, worseSTD, overalSore));
//							return Float.toString(overalSore);
						}
						else {
							return getGSON(true).toJson(new OveralScore(total, worseHR, worseRMSSD, worseSTD, -1));
//							return "{ \"error\": \"bad query, use: age, gender, activity, hr, rmssd, sdnn. Activity can be one of strings: {Resting: Work, low activity; Unspecified; Post-Exercising; Exercising; Pre-Exercising}, rest of parameters are numerical \" }";
						} 
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
				return "null";

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//				return "error: " + e.getMessage();
				return "{ \"error\": \"" + e.getMessage() + "\" }";
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "";
		
	}
	
	//// excel
	@GET
	@Path("get/excel/{db}/{collection}/")
	@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
	public String getMongoDataExcel(@PathParam("db") String db, @PathParam("collection") String collection,
			@DefaultValue("100") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("csv") @QueryParam("separator") String tsv, @QueryParam("sort") String sort) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "{ \"error\" : \"not authorized\" }";
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBCursor cursor; // =
									// dBcollection.find().skip(offset).limit(limit);

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find().sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find().sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find().skip(offset).limit(limit);
				}

				String separator = (tsv != null && tsv.equalsIgnoreCase("tsv") ? "\t" : ",");

				return DBCursorToString(cursor, separator);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return "null";
	}
	
	@GET
	@Path("get/excelfile/{db}/{collection}/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM + "; charset=UTF-8")
	public Response getMongoDataExcelFile(@PathParam("db") String db, @PathParam("collection") String collection,
			@DefaultValue("100") @QueryParam("limit") int limit, 
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("csv") @QueryParam("separator") String tsv, 
			@QueryParam("sort") String sort) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return Response.status(401).build();
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBCursor cursor; // =
									// dBcollection.find().skip(offset).limit(limit);

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find().sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find().sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find().skip(offset).limit(limit);
				}

				String separator = (tsv != null && tsv.equalsIgnoreCase("tsv") ? "\t" : ",");
				
				final String responseStr = DBCursorToString(cursor, separator);
				
				return Response
		                .ok(stringToStreamingOut(responseStr), MediaType.APPLICATION_OCTET_STREAM)
		                .header("content-disposition","attachment; filename = " + collection + "." + (separator.equals(",")?"csv":"tsv" ) )
		                .build();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		return Response.ok("no data found")
		.type(MediaType.TEXT_PLAIN).
		build();
	}
	
	@GET
	@Path("query/excel/{db}/{collection}/{query}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8") //  MediaType.TEXT_PLAIN
	@Consumes(MediaType.APPLICATION_JSON)
	public String getQueryExcelMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@PathParam("query") String query,
			@DefaultValue("100") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("false") @QueryParam("pretty") boolean pretty, @QueryParam("sort") String sort,
			@DefaultValue("csv") @QueryParam("separator") String tsv,
			@DefaultValue("false") @QueryParam("b64") boolean b64) {
		
		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return "unauthorized";
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject doc = (DBObject) JSON.parse(query);
				DBCursor cursor; 

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find(doc).sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find(doc).sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find(doc).skip(offset).limit(limit);
				}

				String separator = (tsv != null && tsv.equalsIgnoreCase("tsv") ? "\t" : ",");
				
				return DBCursorToString(cursor, separator);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}

		return "null";
		
	}
	
	@GET
	@Path("query/excelfile/{db}/{collection}/{query}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM + "; charset=UTF-8") //  MediaType.TEXT_PLAIN
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getQueryExcelFileMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@PathParam("query") String query,
			@DefaultValue("100") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("false") @QueryParam("pretty") boolean pretty, @QueryParam("sort") String sort,
			@DefaultValue("csv") @QueryParam("separator") String tsv,
			@DefaultValue("false") @QueryParam("b64") boolean b64) {
		
		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);
				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return Response.ok("error: unauthorized") 
								.type(MediaType.TEXT_PLAIN).
								build();
					}
				}
				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject doc = (DBObject) JSON.parse(query);
				DBCursor cursor; 

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find(doc).sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find(doc).sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find(doc).skip(offset).limit(limit);
				}

				String separator = (tsv != null && tsv.equalsIgnoreCase("tsv") ? "\t" : ",");
				
				String responseStr =  DBCursorToString(cursor, separator);
				
				return Response
		                .ok(stringToStreamingOut(responseStr), MediaType.APPLICATION_OCTET_STREAM)
		                .header("content-disposition","attachment; filename = " + collection + "." + (separator.equals(",")?"csv":"tsv" ) )
		                .build();

			} catch (Exception e) {
				e.printStackTrace();
				return Response.ok("error: " + e.getMessage())
						.type(MediaType.TEXT_PLAIN).
						build();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}

		return Response.ok("no data found")
				.type(MediaType.TEXT_PLAIN).
				build();
		
	}
	
	@POST
	@Path("query/excelfile/{db}/{collection}/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postQueryExcelMongoData(@PathParam("db") String db, @PathParam("collection") String collection,
			@DefaultValue("100") @QueryParam("limit") int limit, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("false") @QueryParam("pretty") boolean pretty, @QueryParam("sort") String sort,
			@DefaultValue("csv") @QueryParam("separator") String tsv,
			String query) {

		Mongo mongo = null;

		try {
			mongo = new Mongo(hostIP);
		} catch (Exception e) {
			System.err.println("error new Mongo( " + hostIP + " ): " + e.getMessage());
		}

		if (mongo != null) {
			try {
				DB mongoDB = mongo.getDB(db);

				if (us != null && pwd != null) {
					boolean auth = mongoDB.authenticate(us, pwd.toCharArray());
//					System.out.println("authenticate for " + us + ": " + auth);
					if (!auth) {
						return Response.ok("{ \"error\" : \"not authorized\" }")
								.type(MediaType.APPLICATION_JSON).
								build();
					}
				}

				DBCollection dBcollection = mongoDB.getCollection(collection);
				DBObject doc = (DBObject) JSON.parse(query);

				if (dBcollection == null || doc == null) {
					return Response.ok("no data found")
							.type(MediaType.TEXT_PLAIN).
							build();
				}

				DBCursor cursor;

				if (sort != null && sort.equalsIgnoreCase("desc")) {
					cursor = dBcollection.find(doc).sort(new BasicDBObject("_id", -1)).skip(offset).limit(limit);
				} else if (sort != null && sort.equalsIgnoreCase("asc")) {
					cursor = dBcollection.find(doc).sort(new BasicDBObject("_id", 1)).skip(offset).limit(limit);
				} else {
					cursor = dBcollection.find(doc).skip(offset).limit(limit);
				}

				String separator = (tsv != null && tsv.equalsIgnoreCase("tsv") ? "\t" : ",");

				
				final String responseStr = DBCursorToString(cursor, separator);
				
				return Response
		                .ok(stringToStreamingOut(responseStr), MediaType.APPLICATION_OCTET_STREAM)
		                .header("content-disposition","attachment; filename = " + collection + "." + (separator.equals(",")?"csv":"tsv" ) )
		                .build();


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					mongo.close();
				} catch (Exception e) {
				}
			}
		}
		
		return Response.ok("no data found")
				.type(MediaType.TEXT_PLAIN).
				build();
	}
	
	private Gson getGSON(boolean pretty) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ObjectId.class, new MongoObjectIdSerializer());
		if (pretty) {
			return gsonBuilder.setPrettyPrinting().create();
		} else {
			return gsonBuilder.create();
		}
	}
	
	private StreamingOutput stringToStreamingOut(final String responseStr) {
		
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				try {
					byte[] data = responseStr.toString().getBytes(Charset.defaultCharset());
                    output.write(data);
                    output.flush();
				} catch(Exception e) {
					throw new WebApplicationException();
				}
			}
		};
		
	}
	
	private String DBCursorToString(DBCursor cursor, String separator) {

		boolean collectionFirst = true;

		StringBuffer sb = new StringBuffer();

		try {
			Set<String> keySet = null; //.collectionElement.keySet();
			while (cursor.hasNext()) {

				DBObject collectionElement = cursor.next();
				boolean first = true;

//				Set<String> keySet = collectionElement.keySet();

				if (collectionFirst) {
					keySet = collectionElement.keySet();
					for (String key : keySet)
						if (first) {
							// System.out.print(key);
							sb.append( key.indexOf(separator)>=0 ? "\"" + key + "\"" : key);
							first = !first;
						} else {
							// System.out.print("," + key);
							sb.append(separator + (key.indexOf(separator)>=0 ? "\"" + key + "\"" : key) );
						}

					collectionFirst = !collectionFirst;
					// System.out.println("");
					sb.append("\r\n");
				}

				first = true;
				for (String key : keySet) {
					
					Object o = collectionElement.get(key);
					if (o==null) {
//						continue;
						o = "";
					}
					String curr = o.toString();
					
					if (first) {
						sb.append(curr.indexOf(separator)>=0 ? "\"" + curr + "\"" : curr);
						first = !first;
					} else {
						sb.append(separator + (curr.indexOf(separator)>=0 ? "\"" + curr + "\"" : curr) );
					}
					
				}

				// System.out.println("");
				sb.append("\r\n");

			}
		} finally {
			cursor.close();
		}

		return sb.toString();
	}

}
