package to.us.resume_builder.spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import mockit.Expectations;
import mockit.Mocked;
import to.us.resume_builder.ResumeExporter;

class PDFControllerTest {

	@Mocked
	private ResumeExporter bldr;
	private static PDFController pc;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		pc = new PDFController();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		pc = null;
	}

	@Test
	void testExportFail() {
		try {
			new Expectations() {
				{
					bldr.export((Path) any, anyString);
					result = false;
				}
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ResponseEntity<InputStreamResource> rss = pc.postPdf("", true);
		assertEquals(rss.getStatusCode(), HttpStatus.BAD_REQUEST);
	}
}
