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
import de.dfki.asr.atlas.business.ImportOperationManager;
import de.dfki.asr.atlas.model.AssetTransaction;
import de.dfki.asr.atlas.model.ImportOperation;
import javax.inject.Inject;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/transaction")
@Api(value = "/transaction", description = "Transaction operations")
public class TransactionRESTService {

	@Inject
	private ImportOperationManager importOperationManager;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
			value = "Get a transaction.",
			notes = "If the transaction status is finished, the transaction will be removed"
			+ "and all further requests to this transaction will resolve in a 404 Error.",
			response = AssetTransaction.class
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Transaction not found"),
		@ApiResponse(code = 500, message = "Internal Server Error")
	})
	public AssetTransaction handleGetTransaction(
			@ApiParam(value = "Transaction id", required = true)
			@PathParam("id") String transactionId)
			throws PathNotFoundException, RepositoryException {
		ImportOperation op = importOperationManager.findImportOperation(transactionId);
		if (op.getStatus() == ImportOperation.Status.FINISHED) {
			importOperationManager.removeImportOperation(op);
		}
		return new AssetTransaction(op);
	}
}
