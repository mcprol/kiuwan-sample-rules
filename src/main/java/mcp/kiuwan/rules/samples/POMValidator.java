// MIT License
//
// Copyright (c) 2018 Marcos Cacabelos Prol
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package mcp.kiuwan.rules.samples;

import org.apache.log4j.Logger;

import com.als.core.RuleContext;
import com.als.core.RuleViolation;
import com.optimyth.qaking.xml.XmlNode;
import com.optimyth.qaking.xml.ast.XmlDocument;
import com.optimyth.xml.rules.XmlRule;

/**
 * .
 */
public class POMValidator extends XmlRule {
	private final static Logger logger = Logger.getLogger(POMValidator.class);

	private final static String POM_FILE_NAME = "pom.xml";
	private final static String DEPENDENCY_TYPE_NAME = "dependency";
	private final static String ARTIFACTID_TYPE_NAME = "artifactId";
	private final static String GROUPID_TYPE_NAME = "groupId";
	private final static String VERSION_TYPE_NAME = "version";



	@Override 
	public void initialize(RuleContext ctx) {
		super.initialize(ctx);
	}

	@Override 
	protected void visit(XmlDocument root, final RuleContext ctx) {
		if (!POM_FILE_NAME.equalsIgnoreCase(ctx.getSourceCodeFilename().getName())) {
			// there is not a POM file. this avoid run this rule on another XML file.
			return;		
		}

		searchArtifacts(root, DEPENDENCY_TYPE_NAME, ctx);
	}


	private void searchArtifacts(XmlDocument root, String typeName, RuleContext ctx) {
		if (typeName == null || typeName.isEmpty()) return;

		root.onElements(e -> {
			String groupId = null;
			String artifactId = null;
			String version = null;

			if (typeName.equals(e.getTypeName())) {
				for (XmlNode child: e.children()) {
					switch (child.getTypeName()) {
					case GROUPID_TYPE_NAME:
						groupId = child.getTextContent();
						break;
					case ARTIFACTID_TYPE_NAME:
						artifactId = child.getTextContent();
						break;
					case VERSION_TYPE_NAME:
						version = child.getTextContent();
						break;
					default : 
						//nothing to do
					}
				}
				
				if (artifactId == null) {
					logger.warn("'artifactId' has not been specified so the rule cannot process this element " + e.getCode());
				} else if (version == null) {
					logger.warn("There is no version specified for the artifact " + groupId + ":" +  artifactId); 				
				} else {					
					// which is the last version
					if (groupId != null) {
						String lastVersion = getLastTrunkRelease(groupId, artifactId);
						if (lastVersion != null && !version.equalsIgnoreCase(lastVersion)) {
							RuleViolation rv = new RuleViolation(this, e.getBeginLine(), ctx.getSourceCodeFilename());
							
							String fragment = e.getName() + "::" + groupId + "::" + artifactId + "::" + version;
							rv.setCodeViolated(fragment);
							rv.addExplanation("last version is: " + lastVersion);
							
							logger.debug(rv.toString());
							ctx.addRuleViolation(rv);						
						}
					}

				}
			}
		});

	}

	// looks in your external repository, the last versionn available for this dependency.
	// hardcoded for test purposes.
	private String getLastTrunkRelease(String groupId, String artifactId) {
		return "1.0.1"; 
	}
}



