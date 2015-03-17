/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also avialable at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.rest;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import de.dfki.asr.atlas.business.ImportManager;
import de.dfki.asr.atlas.business.JCRAssetManager;
import de.dfki.asr.atlas.business.NumberBasedLookup;
import de.dfki.asr.atlas.cdi.provider.ExporterProducer;
import de.dfki.asr.atlas.convert.ExportContext;
import de.dfki.asr.atlas.convert.ExportOperation;
import de.dfki.asr.atlas.convert.ValueOfBasedListSplitter;
import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Folder;
import de.dfki.asr.atlas.model.ImportOperation;
import static de.dfki.asr.atlas.rest.LocationBuilder.locationOf;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

@Path("/asset")
@Api(value = "/asset", description = "Asset Operations")
public class AssetRESTService {

	@Context
	UriInfo uriInfo;

	@Inject
	ImportManager importManager;

	@Inject
	JCRAssetManager assetManager;

	@Inject
	ExporterProducer exporterFactory;

	@PUT
	@Path("/{assetName}")
	@Consumes({"application/zip", "application/x-zip-compressed"})
	@ApiOperation(
			value = "Upload asset to storage.",
			notes = "The asset should be within a zip file."
					+ " If you choose a new asset name a new asset is created."
					+ " If you choose an existing a new revision of the existing asset is added."
					+ " The Response contains the URI to the transactions associated with the uploaded asset.",
			response = String.class
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Internal Server Error.")
	})
	public Response uploadZipAssetToScratchStorage(
			@ApiParam(value="Asset as a zip file", required = true, allowableValues = "A zip file")
			InputStream in,
			@ApiParam(value="Asset name", required = true, allowableValues = "([A-Za-z][A-Za-z0-9_-]*)")
			@PathParam(value = "assetName") String assetName)
			throws RepositoryException, JMSException {
		if (!isValidAssetName(assetName)) {
			return createBadAssetNameResponse(assetName);
		}
		ImportOperation op = createImportOperation(in, assetName, "zip");
		return Response.created(locationOf(TransactionRESTService.class).add(op).uri()).build();
	}

	@PUT
	@Path("/{assetName}")
	@Consumes("model/vnd.collada+x")
	@ApiOperation(
			value = "Upload asset from collada file to storage.",
			notes = " If you choose a new asset name a new asset is created."
					+ " If you choose an existing a new revision of the existing asset is added."
					+ " The Response contains the URI to the transactions associated with the uploaded asset.",
			response = String.class
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Internal Server Error.")
	})
	public Response uploadColladaToScratchStorage(
			@ApiParam(value="Asset as a collada file", required = true, allowableValues = "A collada file")
			InputStream in,
			@ApiParam(value="Asset name", required = true, allowableValues = "([A-Za-z][A-Za-z0-9_-]*)")
			@PathParam(value = "assetName") String assetName) throws RepositoryException, JMSException {
		if (!isValidAssetName(assetName)) {
			return createBadAssetNameResponse(assetName);
		}
		ImportOperation op = createImportOperation(in, assetName, "dae");
		return Response.created(locationOf(TransactionRESTService.class).add(op).uri()).build();
	}

	private ImportOperation createImportOperation(InputStream in, String assetName, String fileType) throws RepositoryException, JMSException {
		ImportOperation importOperation = importManager.createImportOperation(assetName);
		importOperation.setFileType(fileType);
		importManager.saveSourceFileToScratchSpace(importOperation, in);
		importManager.enqueueWork(importOperation);
		return importOperation;
	}

	private boolean isValidAssetName(String assetName) {
		if (assetName == null) {
			return false;
		}
		String regex = "[A-Za-z][A-Za-z0-9_-]*";
		return assetName.matches(regex);
	}

	private Response createBadAssetNameResponse(String assetName) {
		String msg = "The given asset name '" + assetName + "' is invalid. Valid asset names must:\n";
		msg += "\t- not be an empty string\n";
		msg += "\t- only contain the following characters: A-Z, a-z, 0-9, _, -\n";
		msg += "\t- not contain any whitespace characters\n";
		return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Retrieve asset list",
			notes = "Retrieves a list of assets from the server."
					+ " The List contains URI to the assets."
					+ " The List comes in json format.",
			response = String.class,
			responseContainer = "List"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Internal Server Error")
	})
	public List<String> retrieveAssetList() throws RepositoryException {
		List<Asset> assetList = assetManager.getAssetListing();
		List<String> assetUrlList = new ArrayList<>(assetList.size());
		for (Asset asset : assetList) {
			assetUrlList.add(LocationBuilder.locationOf(uriInfo).add(asset).uri().toString());
		}
		return assetUrlList;
	}

	@GET
	@Path("/{assetName : ([A-Za-z][A-Za-z0-9_-]*)}{subassetPath: (/[^.]+?)?}{extension : (\\.\\w+)?}")
	@ApiOperation(
			value = "Get encoded asset by asset name and (optional) path.",
			notes = "Subassets are adressed via its assetpath."
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Entity not found"),
		@ApiResponse(code = 406, message = "Content type is not acceptable"),
		@ApiResponse(code = 500, message = "Internal Server Error")
	})
	public Object getEncodedAsset(
			@ApiParam(value = "Asset name", required = true, allowableValues = "([A-Za-z][A-Za-z0-9_-]*)")
			@PathParam("assetName") String name,
			@ApiParam(value = "File extension", allowableValues = "(\\.\\w+)?")
			@PathParam("extension") String fileExtension,
			@ApiParam(value = "Path to the subasset", allowableValues = "(/[^.]+?)?")
			@PathParam("subassetPath") String subassetPath,
			@ApiParam(value = "Include child assets", allowableValues = "true or false")
			@QueryParam("includeChildren") @DefaultValue("true") boolean includeChildren,
			@Context Request request) throws ExportContentNegotiationException {
		Asset asset = assetManager.findLatestRevisionForAsset(name);
		Folder startFolder = findStartFolderFromPath(asset.getRootFolder(), subassetPath);
		LocationBuilder builder = locationOf(uriInfo.getBaseUriBuilder()).add(AssetRESTService.class);
		ExportContext context = new ExportContext(assetManager, asset, startFolder, fileExtension, builder, includeChildren);
		Variant selectedVariant = contentNegotiation(context, request);
		ExportOperation<?> exporter = exporterFactory.getExporterForVariant(selectedVariant);
		return Response.ok(exporter.export(context), selectedVariant).build();
	}

	private Folder findStartFolderFromPath(Folder rootFolder, String subassetPath) {
		ValueOfBasedListSplitter<Integer> splitter = new ValueOfBasedListSplitter("/", Integer.class);
		List<Integer> address = splitter.split(subassetPath);
		NumberBasedLookup lookup = new NumberBasedLookup();
		return lookup.lookup(address, rootFolder);
	}

	private Variant contentNegotiation(ExportContext context, Request request) throws ExportContentNegotiationException {
		List<Variant> possibleVariants = exporterFactory.getVariants(context);
		if (possibleVariants.isEmpty() && !"".equals(context.getRequestedFileExtension())) {
	    // maybe the user chose an incompatible fileExtension?
			// let's see if there's variants if we ignore the extension, and tell them to the user.
			context = new ExportContext(context.getAssetManager(),
					context.getSourceAsset(),
					context.getStartingFolder(),
					"", // empty file extension
					locationOf(uriInfo.getBaseUriBuilder()).add(AssetRESTService.class),
					context.getIncludeChildren());
			possibleVariants = exporterFactory.getVariants(context);
			throw new ExportContentNegotiationException(context, possibleVariants);
		}
		Variant selectedVariant = request.selectVariant(possibleVariants);
		if (selectedVariant == null) {
			throw new ExportContentNegotiationException(context, possibleVariants);
		}
		return selectedVariant;
	}

}
