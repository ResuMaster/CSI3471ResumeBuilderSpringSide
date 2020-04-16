package to.us.resume_builder.dbc;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {
	private static final String LATEX = "\\documentclass{article}\r\n" + "\\begin{document}\r\n" + "Hello World\r\n"
			+ "\\end{document}";

	public static void main(String[] args) throws IOException, InterruptedException {
		PDFFacade facade = PDFFacade.getPDFHandle();

		// Test read
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("./hello.pdf"))) {
			var v = facade.getPDF(LATEX);
			bos.write(v);
			var v2 = facade.uploadPDF(LATEX);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
