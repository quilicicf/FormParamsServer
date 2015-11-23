/**
 * Copyright 2010-2014 Restlet S.A.S. All rights reserved.
 * 
 * Restlet and APISpark are registered trademarks of Restlet S.A.S.
 */


package fr.quilici.tests.form.server;

import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;
import static org.restlet.data.MediaType.MULTIPART_FORM_DATA;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;

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
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnyServerResource extends ServerResource {
    
    private static Logger LOGGER = LoggerFactory.getLogger(AnyServerResource.class);

    @Post
    public Map<String, List<String>> handlePost(Representation entity) {

        if (entity != null) {
            LOGGER.info("Media type: " + entity.getMediaType());
            if (APPLICATION_WWW_FORM.equals(entity.getMediaType(), true)) {
                return handleFormUrlEncoded(entity);
            } else if (MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                return handleMultipartFormData();
            } else {
                ResourceException e = new ResourceException(CLIENT_ERROR_BAD_REQUEST,
                        format("The media type is neither %s nor %s.",
                                MULTIPART_FORM_DATA.getName(),
                                APPLICATION_WWW_FORM.getName()));
                LOGGER.error("Exception thrown.", e);
                throw e;
            }
        } else {
            throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "The input entity is null.");
        }
    }

    private Map<String, List<String>> handleFormUrlEncoded(Representation entity) {
        Map<String, List<String>> result = new HashMap<>();
        try {
            String[] parts = entity.getText().split("&");
            for(String part: parts) {
                String[] parametersPair = part.split("=");
                if (parametersPair.length != 2) {
                    throw new RuntimeException("Erroneous list of parameter");
                }

                String key = parametersPair[0];
                String value = parametersPair[1];

                LOGGER.info(format("Reading entry: (%s, %s)", key, value));
                addEntry(result, decode(key, UTF_8.toString()), decode(value, UTF_8.toString()));
            }
        } catch (Exception e) {
            LOGGER.error("Exception thrown.", e);
            throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, e);
        }
        return result;
    }

    private Map<String, List<String>> handleMultipartFormData() {
        Map<String, List<String>> result = new HashMap<>();

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

                    LOGGER.info(format("Reading entry: (%s, %s)", fieldName, value));
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

                    LOGGER.info(format("Wrote file entry for %s to %s", fieldName, filePath.toString()));
                    addEntry(result, fieldName, name + ":=>" + filePath.toString());
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("Exception thrown.", e);
            throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, e);
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
