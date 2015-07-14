package org.ednovo.gooru.core.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CollectionItem implements Serializable, Comparable<CollectionItem> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7365762989410060090L;

	private String collectionItemId;
	private Collection collection;
	private Content content;
	private Resource resource;
	private String itemType;
	private Integer itemSequence;
	private String narration;
	private String narrationType;
	private String start;
	private String stop;
	private Date associationDate;
	private User associatedUser;
	private Rating rating;
	private AssessmentQuestion question;
	private String documentid;
	private String documentkey;
	private List<StandardFo> standards;
	private Set<String> course;
	private Integer resourceCount;
	private String status;
	private String estimatedTime;
	private Boolean showAnswerByQuestions;
	private Boolean showHints;
	private Boolean showAnswerEnd;
	private Integer position;
	private String uri;
	private String title;

	public String getCollectionItemId() {
		return collectionItemId;
	}

	public Date getAssociationDate() {
		return associationDate;
	}

	public void setAssociationDate(Date associationDate) {
		this.associationDate = associationDate;
	}

	public User getAssociatedUser() {
		return associatedUser;
	}

	public void setAssociatedUser(User associatedUser) {
		this.associatedUser = associatedUser;
	}

	public void setCollectionItemId(String collectionItemId) {
		this.collectionItemId = collectionItemId;
	}

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public Integer getItemSequence() {
		return itemSequence;
	}

	public void setItemSequence(Integer itemSequence) {
		this.itemSequence = itemSequence;
	}

	public String getNarration() {
		return narration;
	}

	public void setNarration(String narration) {
		this.narration = narration;
	}

	public String getNarrationType() {
		return narrationType;
	}

	public void setNarrationType(String narrationType) {
		this.narrationType = narrationType;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getStop() {
		return stop;
	}

	public void setStop(String stop) {
		this.stop = stop;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public Rating getRating() {
		return rating;
	}

	public void setDocumentid(String documentid) {
		this.documentid = documentid;
	}

	public String getDocumentid() {
		return documentid;
	}

	public void setDocumentkey(String documentkey) {
		this.documentkey = documentkey;
	}

	public String getDocumentkey() {
		return documentkey;
	}

	public void setStandards(List<StandardFo> standards) {
		this.standards = standards;
	}

	public List<StandardFo> getStandards() {
		return standards;
	}

	@Override
	public int compareTo(CollectionItem otherItem) {
		if (otherItem != null && getItemSequence() != null && otherItem.getItemSequence() != null && !getCollectionItemId().equals(otherItem.getCollectionItemId())) {
			if (getItemSequence().equals(otherItem.getItemSequence())) {
				return 0;
			}
			return getItemSequence().compareTo(otherItem.getItemSequence());
		}
		return 0;
	}

	public void setCourse(Set<String> course) {
		this.course = course;
	}

	public Set<String> getCourse() {
		return course;
	}

	public void setResourceCount(Integer resourceCount) {
		this.resourceCount = resourceCount;
	}

	public Integer getResourceCount() {
		return resourceCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setShowAnswerByQuestions(Boolean showAnswerByQuestions) {
		this.showAnswerByQuestions = showAnswerByQuestions;
	}

	public Boolean getShowAnswerByQuestions() {
		return showAnswerByQuestions;
	}

	public void setShowHints(Boolean showHints) {
		this.showHints = showHints;
	}

	public Boolean getShowHints() {
		return showHints;
	}

	public void setShowAnswerEnd(Boolean showAnswerEnd) {
		this.showAnswerEnd = showAnswerEnd;
	}

	public Boolean getShowAnswerEnd() {
		return showAnswerEnd;
	}

	public void setEstimatedTime(String estimatedTime) {
		this.estimatedTime = estimatedTime;
	}

	public String getEstimatedTime() {
		return estimatedTime;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AssessmentQuestion getQuestion() {
		return question;
	}

	public void setQuestion(AssessmentQuestion question) {
		this.question = question;
	}

}
