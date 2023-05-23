package org.activiti.core.el.juel.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import org.activiti.core.el.juel.test.TestCase;
import org.activiti.core.el.juel.tree.impl.Builder;
import org.junit.jupiter.api.Test;

public class NodePrinterTest extends TestCase {

	@Test
    public void testDump() throws IOException {
		Tree tree = new Builder(Builder.Feature.METHOD_INVOCATIONS).build("${foo.bar[baz] + foobar}");
		StringWriter writer = new StringWriter();
		NodePrinter.dump(new PrintWriter(writer), tree.getRoot());
		String[] expected = {
				"+- ${...}",
				"   |",
				"   +- '+'",
				"      |",
				"      +- [...]",
				"      |  |",
				"      |  +- . bar",
				"      |  |  |",
				"      |  |  +- foo",
				"      |  |",
				"      |  +- baz",
				"      |",
				"      +- foobar",
				null
		};
		BufferedReader reader = new BufferedReader(new StringReader(writer.toString()));
		for (String line : expected) {
			assertEquals(line, reader.readLine());
		}
	}

}
