/*
 * This file is part of ATLAS. It is subject to the license terms in
 * the LICENSE file found in the top-level directory of this distribution.
 * (Also available at http://www.apache.org/licenses/LICENSE-2.0.txt)
 * You may not use this file except in compliance with the License.
 */
package de.dfki.asr.atlas.business;

import de.dfki.asr.atlas.model.Asset;
import de.dfki.asr.atlas.model.Blob;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.persistence.EntityNotFoundException;

@Named
@RequestScoped
public class JCRAssetManager extends JCRBusinessClass implements AssetManager {
	private static final long serialVersionUID = 3664684767618391453L;

	@Override
	public Asset findAsset(String assetName, String revisionId) {
		try {
			Node assetNode = jcrSession.getNode("/assets/" + assetName + "/scene/" + revisionId);
			Asset asset = jcromMapper.fromNode(Asset.class, assetNode);
			asset.setName(assetName);
			asset.parseScene();
			return asset;
		} catch (PathNotFoundException pe) {
			throw new EntityNotFoundException(assetName);
		} catch (IOException | RepositoryException ex) {
			log.debug(ex.getMessage());
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	@Override
	public Asset findLatestRevisionForAsset(String assetName) {
		try {
			Node revisionList = jcrSession.getNode("/assets/" + assetName + "/scene/");
			NodeIterator it = revisionList.getNodes();
			Node child = it.nextNode();
			//TODO: Find the latest revision and return it, for now just return the first one in the list
			return findAsset(assetName, child.getName());

		} catch (PathNotFoundException pe) {
			throw new EntityNotFoundException(assetName);
		} catch (RepositoryException ex) {
			log.debug(ex.getMessage());
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	@Override
	public Blob getBlobOfAsset(String assetName, String hash) {
		try {
			String blobPath = "/assets/" + assetName + "/blobs/" + hash;
			Node blobNode = jcrSession.getNode(blobPath);
			Node contentNode = blobNode.getNode("jcr:content");
			InputStream data = contentNode.getProperty("jcr:data").getBinary().getStream();
			return new Blob(hash, data);
		} catch (PathNotFoundException pe) {
			throw new EntityNotFoundException(assetName);
		} catch (RepositoryException ex) {
			log.debug(ex.getMessage());
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	@Override
	public List<Asset> getAssetListing() {
		try {
			Node assetsNode = jcrSession.getNode("/assets/");
			NodeIterator assetIterator = assetsNode.getNodes();
			ArrayList<Asset> assetList = new ArrayList<>();
			while (assetIterator.hasNext()) {
				Node assetNode = assetIterator.nextNode();
				if (assetNode.hasNode("scene") && assetNode.getNode("scene").getNodes().hasNext()) {
					//We only want assets that have at least 1 fully imported revision available
					assetList.add(jcromMapper.fromNode(Asset.class, assetNode));
				}
			}
			return assetList;
		} catch (RepositoryException ex) {
			log.debug(ex.getMessage());
			throw new IllegalArgumentException(ex.getMessage());
		}
	}
}
