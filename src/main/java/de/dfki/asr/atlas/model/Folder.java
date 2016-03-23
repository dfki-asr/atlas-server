package de.dfki.asr.atlas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class Folder implements Serializable {

	private static final long serialVersionUID = -1637094906282127745L;

	@JsonProperty
	protected String name;

	@JsonProperty
	protected String type;

	@JsonManagedReference
	protected List<Folder> children = new ArrayList<>();

	@JsonBackReference
	@Getter @Setter
	protected Folder parent;

	@JsonProperty
	protected Map<String, String> blobs = new HashMap<>();

	@JsonProperty
	protected Map<String, String> attributes = new HashMap<>();

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setChildren(List<Folder> children) {
		this.children = children;
	}

	public void setBlobs(Map<String, String>  blobs) {
		this.blobs = blobs;
	}

	public Map<String, String> getBlobs() {
		return blobs;
	}

	public void setAttributes(HashMap<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<Folder> getChildFolders() {
		return children;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public String getHashOfBlobWithType(String type){
		return blobs.get(type);
	}

	public List<String> getAllBlobHashes(){
		return new ArrayList<>(blobs.values());
	}

	public String getTypeOfBlob(String blobHash){
		Set<Map.Entry<String, String>> entrySet = blobs.entrySet();
		for (Map.Entry<String, String> entry : entrySet) {
			if (entry.getValue().equals(blobHash)) {
				return entry.getKey();
			}
		}
		return "BLOB_NOT_FOUND";
	}

	public List<Integer> getAddress() {
		if (parent == null) return new LinkedList<>();
		List<Integer> addressOfParent = parent.getAddress();
		int addressInParent = parent.getChildFolders().indexOf(this);
		addressOfParent.add(addressInParent);
		return addressOfParent;
	}
}
