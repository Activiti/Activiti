package org.activiti.examples;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,property = "@class")
public class Content {

    private String body;
    private boolean approved;
    private List<String> tags;

    @JsonCreator
    public Content(@JsonProperty("body")String body, @JsonProperty("approved")boolean approved, @JsonProperty("tags")List<String> tags){
        this.body = body;
        this.approved = approved;
        this.tags = tags;
        if(this.tags == null){
            this.tags = new ArrayList<>();
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Content{" +
                "body='" + body + '\'' +
                ", approved=" + approved +
                ", tags=" + tags +
                '}';
    }
}
