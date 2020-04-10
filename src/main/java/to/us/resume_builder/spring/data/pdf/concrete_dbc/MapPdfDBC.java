package to.us.resume_builder.spring.data.pdf.concrete_dbc;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.core.io.InputStreamResource;

import to.us.resume_builder.spring.data.pdf.abstract_dbc.PdfDBC;
import to.us.resume_builder.spring.data.user.abstract_dbc.DeleteResult;

public class MapPdfDBC implements PdfDBC {
	private static final Logger LOG = Logger.getLogger(MapPdfDBC.class.getName());

	private static final Map<String, Long> DB = new HashMap<String, Long>();

	@Override
	public ByteArrayInputStream getPDF(String name, long pdfID) {
		try {
			// Get resource
		} catch (Exception ex) {
			LOG.severe("Issue: " + ex.getMessage());
		}
		return null;
	}

	@Override
	public long addPdf(String name, InputStreamResource pdf) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DeleteResult deletePdfFromDB(String name, long pdfID) {
		return null;
	}
}
