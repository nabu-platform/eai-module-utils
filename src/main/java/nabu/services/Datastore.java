package nabu.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import be.nabu.libs.datastore.DatastoreFactory;
import be.nabu.libs.datastore.api.DeletableDatastore;
import be.nabu.libs.datastore.api.UpdatableDatastore;
import be.nabu.libs.datastore.api.WritableDatastore;

@WebService
@Path("/datastore")
public class Datastore {

	@GET
	@Path("/{uri}")
	public InputStream retrieve(@WebParam(name="uri") @PathParam("uri") URI uri) throws IOException {
		return DatastoreFactory.getInstance().getDatastore().retrieve(uri);
	}
	
	@POST
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public URI store(@WebParam(name="contentType") @QueryParam("contentType") String contentType, @WebParam(name="name") @QueryParam("name") String name, @WebParam(name="data") InputStream input) throws IOException {
		return ((WritableDatastore) DatastoreFactory.getInstance().getDatastore()).store(input, name, contentType);
	}
	
	@PUT
	@Path("/{uri}")
	public void update(@WebParam(name="uri") @PathParam("uri") URI uri, @WebParam(name="data") InputStream input) throws IOException {
		((UpdatableDatastore) DatastoreFactory.getInstance().getDatastore()).update(uri, input);
	}
	
	@DELETE
	@Path("/{uri}")
	public void delete(@WebParam(name="uri") @PathParam("uri") URI uri) throws IOException {
		((DeletableDatastore) DatastoreFactory.getInstance().getDatastore()).delete(uri);
	}
	
}
