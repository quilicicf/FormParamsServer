/**
 * Copyright 2010-2014 Restlet S.A.S. All rights reserved.
 * 
 * Restlet and APISpark are registered trademarks of Restlet S.A.S.
 */


package fr.quilici.tests.form.server;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PersonServerResource extends ServerResource {

    ObjectMapper mapper = new ObjectMapper();

    @Post
    public Map<String, List<String>> postPerson(Representation entity) {
        String queryValue = getQueryValue("@test");
        if (queryValue != null) {
            System.out.println(String.format("Query param @test found: %s", queryValue));
        }

        Map<String, List<String>> result = new HashMap<>();
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)
                    || MediaType.APPLICATION_WWW_FORM.equals(entity.getMediaType(), true)) {
                // 1/ Create a factory for disk-based file items
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1000240);

                // 2/ Create a new file upload handler
                RestletFileUpload upload = new RestletFileUpload(factory);
                List<FileItem> items;
                int filesCount = 1;
                try {
                    // 3/ Request is parsed by the handler which generates a list of FileItems
                    items = upload.parseRequest(getRequest());

                    File file = null;
                    for (final Iterator<FileItem> it = items.iterator(); it.hasNext(); ) {
                        FileItem fi = it.next();
                        String name = fi.getName();
                        String fieldName = fi.getFieldName();
                        if (name == null) {
                            String value = new String(fi.get(), "UTF-8");
                            System.out.println(String.format("Reading entry: (%s, %s)", fieldName, value));
                            addEntry(result, fieldName, value);
                        } else {
                            String tempDir = System.getProperty("java.io.tmpdir");
                            Path filePath = Paths.get(tempDir).resolve(fieldName + filesCount++ + ".txt");
                            file = filePath.toFile();
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            fi.getInputStream();
                            fi.write(file);
                            System.out.println(String.format("Wrote file entry for %s to %s", fieldName, filePath.toString()));
                            addEntry(result, fieldName, name + ":=>" + filePath.toString());
                        }
                    }
                    return result;
                } catch (Exception e) {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
                }
            } else {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        String.format("The media type is neither %s nor %s.",
                                MediaType.MULTIPART_FORM_DATA.getName(),
                                MediaType.APPLICATION_WWW_FORM.getName()));
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "The input entity is null.");
        }
    }

    private void addEntry(Map<String, List<String>> result, String fieldName, String value) {
        if (result.containsKey(fieldName)) {
            result.get(fieldName).add(value);
        } else {
            result.put(fieldName, Arrays.asList(value));
        }
    }
}
