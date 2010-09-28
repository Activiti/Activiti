package org.activiti.rest.api.cycle.dto;

import java.util.List;

public class ArtifactLinkDto {

	String targetId;
	String targetRevision;
	String targetElementId;
	String linkType;
	String targetUrl;
	String description;
	List<String> comments;
	String contentType;
	String previewUrl;
	String label;

	/**
	 * TODO documentation
	 * 
	 * @return
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * TODO documentation
	 * 
	 * @param targetId
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * TODO documentation
	 * 
	 * @return
	 */
	public String getTargetRevision() {
		return targetRevision;
	}

	/**
	 * TODO documentation
	 * 
	 * @param targetRevision
	 */
	public void setTargetRevision(String targetRevision) {
		this.targetRevision = targetRevision;
	}

	/**
	 * TODO documentation
	 * 
	 * @return
	 */
	public String getTargetElementId() {
		return targetElementId;
	}

	/**
	 * TODO documentation
	 * 
	 * @param targetElementId
	 */
	public void setTargetElementId(String targetElementId) {
		this.targetElementId = targetElementId;
	}

	/**
	 * TODO documentation
	 * 
	 * @return
	 */
	public String getLinkType() {
		return linkType;
	}

	/**
	 * TODO documentation
	 * 
	 * @param linkType
	 */
	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	/**
	 * Returns the URL or deep link that points to the artifact in the cycle
	 * instance
	 * 
	 * @return the URL or deep link that points to the artifact in the cycle
	 *         instance
	 */
	public String getTargetUrl() {
		return targetUrl;
	}

	/**
	 * Sets the URL or deep link that points to the artifact in the cycle
	 * instance
	 * 
	 * @param targetUrl
	 *            the URL or deep link that points to the artifact in the cycle
	 *            instance
	 */
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	/**
	 * Returns the description of this artifact link. 
	 * @return the description of this artifact link
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of this artifact link.
	 * @param description the description of this artifact link
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the content type of the artifact that is linked by this artifact
	 * link. This can be used by the UI to show an icon.
	 * 
	 * @return the content type of the artifact
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the artifact that is linked by this artifact
	 * link. This can be used by the UI to show an icon.
	 * 
	 * @param contentType
	 *            the content type of the artifact
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Returns the URL of a preview image of the linked artifact. Null, if no
	 * preview image is available.
	 * 
	 * @return the URL of a preview image of the linked artifact
	 */
	public String getPreviewUrl() {
		return previewUrl;
	}

	/**
	 * Sets the URL of a preview image of the linked artifact.
	 * 
	 * @param previewUrl
	 *            the URL of a preview image of the linked artifact
	 */
	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	/**
	 * Returns the display name or label of the artifact link.
	 * 
	 * @return the display name or label of the artifact link
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the display name or label of the artifact link.
	 * 
	 * @param label
	 *            the display name or label of the artifact link
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "{\"" + ArtifactLinkDto.class.getName() + "\":[{\"targetId\":\"" + this.targetId + "\",\"targetRevision\":\"" + this.targetRevision+ "\",\"targetElementId\":\"" + this.targetElementId+ "\",\"linkType\":\"" + this.linkType + "\",\"targetUrl\":\"" + this.targetUrl + "\",\"description\":\""+ this.description +"\",\"contentType\":\""+this.contentType+"\",\"previewUrl\":\""+this.getPreviewUrl()+"\",\"label\":\""+this.label+"\"}]}";
				
	}
}
