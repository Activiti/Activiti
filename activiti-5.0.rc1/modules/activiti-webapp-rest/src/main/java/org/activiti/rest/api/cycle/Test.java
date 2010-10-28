package org.activiti.rest.api.cycle;

import java.util.ArrayList;
import java.util.List;

import org.activiti.rest.api.cycle.dto.ArtifactLinkDto;
import org.activiti.rest.api.cycle.dto.ContentView;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XStream xStream = new XStream(new JettisonMappedXmlDriver());
		xStream.setMode(XStream.NO_REFERENCES);

		List<ArtifactLinkDto> artifactLinkDtos = new ArrayList<ArtifactLinkDto>();

		for (int i = 0; i < 10; i++) {
			ArtifactLinkDto dto = new ArtifactLinkDto();
			
			dto.setContentType("word");
			dto.setLinkType("linkType");
			dto.setPreviewUrl("http://www.showhttprequest.com/?previewUrl=ture&artifact=" + i);
			dto.setTargetElementId("targetElementId");
			dto.setTargetId("targetId");
			dto.setTargetRevision("targetRevision");
			dto.setTargetUrl("http://www.showhttprequest.com/?targetUrl=true&artifact=" + i);
			dto.setLabel("Artifact Link No "+ i);
			dto.setDescription("This is a description for " + dto.getLabel());

			artifactLinkDtos.add(dto);
		}

		String json = xStream.toXML(artifactLinkDtos);

		System.out.println("Items (JSON):");
		System.out.println("-------------");
		System.out.println(json);
		
		List<ArtifactLinkDto> deSerializedArtifactLinkDtos = (ArrayList<ArtifactLinkDto>) xStream
				.fromXML(json);
		
		System.out.println();
		
		System.out.println("Items (toString):");
		System.out.println("-----------------");
		for(ArtifactLinkDto item : deSerializedArtifactLinkDtos) {
			System.out.println(item);
		}

	}
}
